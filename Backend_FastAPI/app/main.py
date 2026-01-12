from fastapi import FastAPI, HTTPException, UploadFile, File, Form, Depends, Request, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from collections import Counter
import json
from typing import List, Optional, Dict, Any
import asyncio
import hashlib
import uuid
from datetime import datetime
import logging
from pathlib import Path
from sqlalchemy import text, select, func, inspect, delete
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession
from app.schemas import (
    UserCreateRequest, UserUpdateRequest, UserResponse,
    GroupCreateRequest, GroupResponse, GroupDetailResponse, GroupEmbeddingResponse,
    UserEmbeddingResponse, AddMemberRequest,
    PhotoUploadResponse, TagAnalysisRequest, TagAnalysisResponse,
    ImageAnalysisRequest, ImageAnalysisResponse, ImageAnalysisResult, ImageKeyword,
    GenerateEmbeddingRequest, GenerateEmbeddingResponse,
    TextEmbeddingRequest, BatchPhotoUploadResponse
)
from app.router import router as spec_router
from app.db.session import engine, get_db
from app.db.base import Base
import app.models  # ensure models are registered for metadata
from app.models.user import User
from app.models.photo import UserPhoto
from app.models.group import Group, GroupMember
from app.services.embedding.captioning import caption_image
from app.services.embedding.composer import build_final_text, compute_source_hash
from app.services.embedding.embedding_log import log_embedding_io
from app.services.embedding.interest import infer_interest_tags
from app.services.embedding.openai_embed import embed_text, EMBEDDING_DIM
from app.services.embedding.repo import (
    create_embedding,
    deactivate_embeddings,
    get_active_embedding,
    get_recent_captions,
    upsert_image_caption,
)
from app.services.embedding.translation import translate_to_korean

LOG_FORMAT = "%(asctime)s | %(levelname)s | %(name)s | %(message)s"
DATE_FORMAT = "%Y-%m-%d %H:%M:%S"


def _configure_logging() -> None:
    logging.basicConfig(level=logging.INFO, format=LOG_FORMAT, datefmt=DATE_FORMAT)
    formatter = logging.Formatter(LOG_FORMAT, datefmt=DATE_FORMAT)
    for logger_name in ("uvicorn", "uvicorn.error", "uvicorn.access"):
        logger = logging.getLogger(logger_name)
        for handler in logger.handlers:
            handler.setFormatter(formatter)


_configure_logging()

app = FastAPI(
    title="InterestMap API",
    version="1.0.0",
    description="""
    ## InterestMap - 사진 기반 관심사 매칭 플랫폼
    
    사용자의 프로필 사진을 AI로 분석하여 관심사를 추출하고, 
    유사한 관심사를 가진 사람들과 그룹을 매칭해주는 서비스입니다.
    
    ### 주요 기능
    * 🔐 카카오 로그인 인증
    * 📸 사진 업로드 및 AI 분석
    * 🤖 AI 기반 관심사 추출
    * 👥 그룹 생성 및 참여
    * 💬 그룹 채팅
    * 🗺️ Interest Map 시각화
    
    ### API 구조
    * `/auth/*` - 인증 관련
    * `/me/*` - 사용자 정보 및 프로필
    * `/groups/*` - 그룹 관리
    * `/api/*` - Legacy 엔드포인트
    """,
    contact={
        "name": "InterestMap Team",
        "email": "support@interestmap.com"
    },
    license_info={
        "name": "MIT License",
    },
    openapi_tags=[
        {
            "name": "auth",
            "description": "인증 및 로그인 관련 API"
        },
        {
            "name": "me",
            "description": "내 정보 및 프로필 관리"
        },
        {
            "name": "groups",
            "description": "그룹 생성, 조회, 참여"
        },
        {
            "name": "messages",
            "description": "그룹 메시지 및 채팅"
        },
        {
            "name": "legacy",
            "description": "레거시 API (마이그레이션 예정)"
        }
    ]
)

@app.middleware("http")
async def log_request_path(request: Request, call_next):
    logger = logging.getLogger("uvicorn.error")
    logger.info("Request %s %s", request.method, request.url.path)
    return await call_next(request)

BASE_DIR = Path(__file__).resolve().parent.parent
UPLOAD_ROOT = BASE_DIR / "uploads"
UPLOAD_ROOT.mkdir(parents=True, exist_ok=True)
app.mount("/uploads", StaticFiles(directory=UPLOAD_ROOT), name="uploads")

# CORS 설정 (Android 앱에서 접근 가능하도록)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 개발 중에는 모든 origin 허용
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Spec-based endpoints grouped in router.py
app.include_router(spec_router)

# In-memory 데이터베이스 (실제로는 PostgreSQL 사용)
users_db: Dict[str, dict] = {}
photos_db: Dict[str, dict] = {}
groups_db: Dict[str, dict] = {}
user_provider_map: Dict[str, str] = {}  # (provider, provider_user_id) -> user_id


async def _ensure_pgvector_extension() -> bool:
    logger = logging.getLogger("uvicorn.error")
    try:
        autocommit_engine = engine.execution_options(isolation_level="AUTOCOMMIT")
        async with autocommit_engine.connect() as conn:
            await conn.execute(text("CREATE EXTENSION IF NOT EXISTS vector"))
    except Exception as exc:
        logger.warning("pgvector extension not created: %s", exc)
        return False
    async with engine.connect() as conn:
        result = await conn.execute(text("SELECT 1 FROM pg_extension WHERE extname='vector'"))
        return result.first() is not None


async def _ensure_photo_hash_index() -> None:
    async with engine.begin() as conn:
        def _has_column(sync_conn) -> bool:
            insp = inspect(sync_conn)
            if "user_photos" not in insp.get_table_names():
                return False
            cols = {col["name"] for col in insp.get_columns("user_photos")}
            return "content_hash" in cols

        has_hash = await conn.run_sync(_has_column)
        if not has_hash:
            await conn.execute(text("ALTER TABLE user_photos ADD COLUMN content_hash VARCHAR(64)"))
        await conn.execute(
            text(
                "CREATE UNIQUE INDEX IF NOT EXISTS uq_user_photos_user_hash "
                "ON user_photos (user_id, content_hash) "
                "WHERE content_hash IS NOT NULL"
            )
        )


@app.on_event("startup")
async def init_db_schema() -> None:
    logger = logging.getLogger("uvicorn.error")
    vector_ready = await _ensure_pgvector_extension()
    async with engine.begin() as conn:
        tables = list(Base.metadata.sorted_tables)
        if not vector_ready:
            tables = [table for table in tables if table.name != "user_embeddings"]
            logger.warning("Skipping user_embeddings table; pgvector is unavailable.")
        await conn.run_sync(lambda sync_conn: Base.metadata.create_all(sync_conn, tables=tables))
    await _ensure_photo_hash_index()


def _cache_user(user: User, is_new_user: bool = False) -> dict:
    user_data = {
        "id": str(user.id),
        "provider": user.provider,
        "provider_user_id": user.provider_user_id,
        "nickname": user.nickname,
        "profile_image_url": user.profile_image_url,
        "profile_data": user.profile_data or {},
        "is_new_user": is_new_user,
        "created_at": user.created_at.isoformat() if user.created_at else None,
        "updated_at": user.updated_at.isoformat() if user.updated_at else None,
    }
    users_db[user_data["id"]] = user_data
    user_provider_map[f"{user.provider}:{user.provider_user_id}"] = user_data["id"]
    return user_data


async def _get_user_by_id(db: AsyncSession, user_id: str) -> User:
    try:
        user_uuid = uuid.UUID(user_id)
    except ValueError as exc:
        raise HTTPException(status_code=404, detail="User not found") from exc
    result = await db.execute(select(User).where(User.id == user_uuid))
    user = result.scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


async def _ensure_user_cached(db: AsyncSession, user_id: str) -> dict:
    if user_id in users_db:
        return users_db[user_id]
    user = await _get_user_by_id(db, user_id)
    return _cache_user(user)


async def _get_group_by_id(db: AsyncSession, group_id: str) -> Group:
    try:
        group_uuid = uuid.UUID(group_id)
    except ValueError as exc:
        raise HTTPException(status_code=404, detail="Group not found") from exc
    result = await db.execute(select(Group).where(Group.id == group_uuid))
    group = result.scalar_one_or_none()
    if not group:
        raise HTTPException(status_code=404, detail="Group not found")
    return group


async def _get_group_member_ids(db: AsyncSession, group_id: uuid.UUID) -> list[uuid.UUID]:
    result = await db.execute(
        select(GroupMember.user_id).where(GroupMember.group_id == group_id)
    )
    return [row[0] for row in result.all()]


def _group_response(group: Group, member_ids: list[uuid.UUID]) -> dict:
    return {
        "id": str(group.id),
        "name": group.name,
        "creator_id": str(group.created_by) if group.created_by else "",
        "description": group.description,
        "member_ids": [str(member_id) for member_id in member_ids],
        "created_at": group.created_at.isoformat() if group.created_at else "",
    }


def _embedding_vector_or_zero(embedding: list[float] | None) -> list[float]:
    if embedding is None:
        return [0.0] * EMBEDDING_DIM
    try:
        if len(embedding) == 0:
            return [0.0] * EMBEDDING_DIM
    except TypeError:
        return [0.0] * EMBEDDING_DIM
    return list(embedding)


def _build_file_url(request: Request, file_path: str) -> str:
    base_url = str(request.base_url).rstrip("/")
    return f"{base_url}{file_path}"


def _photo_response(photo: UserPhoto, request: Request) -> dict:
    file_path = photo.url
    return {
        "id": str(photo.id),
        "user_id": str(photo.user_id),
        "file_path": file_path,
        "file_url": _build_file_url(request, file_path),
        "uploaded_at": photo.created_at.isoformat() if photo.created_at else None,
    }

@app.get("/")
def read_root():
    return {"message": "Hello FastAPI"}

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "InterestMap Backend",
        "version": "1.0.0"
    }

# ==================== User APIs ====================

@app.post("/api/users", response_model=UserResponse)
async def create_user(request: UserCreateRequest, db: AsyncSession = Depends(get_db)):
    """카카오 로그인 후 사용자 생성 또는 조회"""
    stmt = select(User).where(
        User.provider == request.provider,
        User.provider_user_id == request.provider_user_id,
    )
    result = await db.execute(stmt)
    user = result.scalar_one_or_none()
    if user:
        if request.nickname is not None:
            user.nickname = request.nickname
        if request.profile_image_url is not None:
            user.profile_image_url = request.profile_image_url
        if request.profile_data is not None:
            merged = dict(user.profile_data or {})
            merged.update(request.profile_data)
            user.profile_data = merged
        await db.commit()
        await db.refresh(user)
        return _cache_user(user, is_new_user=False)

    user = User(
        provider=request.provider,
        provider_user_id=request.provider_user_id,
        nickname=request.nickname,
        profile_image_url=request.profile_image_url,
        profile_data=request.profile_data or {},
    )
    db.add(user)
    try:
        await db.commit()
    except IntegrityError:
        await db.rollback()
        result = await db.execute(stmt)
        user = result.scalar_one()
        return _cache_user(user, is_new_user=False)
    else:
        await db.refresh(user)
        return _cache_user(user, is_new_user=True)

@app.get("/api/users/{user_id}", response_model=UserResponse)
async def get_user(user_id: str, db: AsyncSession = Depends(get_db)):
    """사용자 정보 조회"""
    user = await _get_user_by_id(db, user_id)
    return _cache_user(user)

@app.put("/api/users/{user_id}", response_model=UserResponse)
async def update_user(user_id: str, request: UserUpdateRequest, db: AsyncSession = Depends(get_db)):
    """사용자 프로필 업데이트"""
    user = await _get_user_by_id(db, user_id)

    image_count = None
    if request.profile_data:
        image_count = request.profile_data.get("image_count")
    if isinstance(image_count, int) and image_count > 0:
        result = await db.execute(
            select(func.count()).select_from(UserPhoto).where(UserPhoto.user_id == user.id)
        )
        photo_count = result.scalar() or 0
        if photo_count == 0:
            logging.getLogger("uvicorn.error").info(
                "Profile update without photos user_id=%s image_count=%s",
                user_id,
                image_count,
            )

    if request.nickname is not None:
        user.nickname = request.nickname
    if request.profile_image_url is not None:
        user.profile_image_url = request.profile_image_url
    if request.profile_data is not None:
        merged = dict(user.profile_data or {})
        merged.update(request.profile_data)
        user.profile_data = merged

    await db.commit()
    await db.refresh(user)
    return _cache_user(user)

# ==================== Photo APIs ====================

@app.post("/api/photos", response_model=PhotoUploadResponse)
async def upload_photo(
    request: Request,
    user_id: str = Form(...),
    file: UploadFile = File(...),
    db: AsyncSession = Depends(get_db),
):
    """사진 업로드"""
    logger = logging.getLogger("uvicorn.error")
    user = await _get_user_by_id(db, user_id)

    safe_name = Path(file.filename or "upload").name
    photo_id = uuid.uuid4()
    user_dir = UPLOAD_ROOT / user_id
    user_dir.mkdir(parents=True, exist_ok=True)
    disk_path = user_dir / f"{photo_id}_{safe_name}"
    hasher = hashlib.sha256()
    with disk_path.open("wb") as buffer:
        while True:
            chunk = file.file.read(1024 * 1024)
            if not chunk:
                break
            buffer.write(chunk)
            hasher.update(chunk)
    content_hash = hasher.hexdigest()

    file_path = f"/uploads/{user_id}/{disk_path.name}"
    file_url = _build_file_url(request, file_path)

    if content_hash:
        existing_result = await db.execute(
            select(UserPhoto).where(
                UserPhoto.user_id == user.id,
                UserPhoto.content_hash == content_hash,
            )
        )
        existing_photo = existing_result.scalar_one_or_none()
        if existing_photo:
            disk_path.unlink(missing_ok=True)
            existing_url = _build_file_url(request, existing_photo.url)
            if user.profile_image_url != existing_url:
                user.profile_image_url = existing_url
                await db.commit()
                await db.refresh(user)
                _cache_user(user)
            logger.info("Duplicate photo ignored user_id=%s hash=%s", user_id, content_hash)
            return _photo_response(existing_photo, request)

    result = await db.execute(
        select(func.max(UserPhoto.sort_order)).where(UserPhoto.user_id == user.id)
    )
    max_sort = result.scalar()
    is_primary = max_sort is None
    sort_order = (max_sort or 0) + 10

    photo = UserPhoto(
        id=photo_id,
        user_id=user.id,
        url=file_path,
        content_hash=content_hash,
        sort_order=sort_order,
        is_primary=is_primary,
    )
    db.add(photo)
    user.profile_image_url = file_url

    caption_raw_en = f"an uploaded image ({disk_path.name})"
    model_name = "fallback"
    model_version = "fallback"
    try:
        caption_task = asyncio.to_thread(caption_image, str(disk_path))
        caption_raw_en, model_name, model_version = await asyncio.wait_for(caption_task, timeout=15)
    except asyncio.TimeoutError:
        logger.warning("Captioning timed out for %s", disk_path.name)
    except Exception as exc:
        logger.warning("Captioning failed for %s: %s", disk_path.name, exc)

    try:
        translate_task = translate_to_korean(caption_raw_en)
        caption_ko = await asyncio.wait_for(translate_task, timeout=15)
    except asyncio.TimeoutError:
        logger.warning("Translation timed out for %s", disk_path.name)
        caption_ko = caption_raw_en
    except Exception as exc:
        logger.warning("Translation failed for %s: %s", disk_path.name, exc)
        caption_ko = caption_raw_en

    interest_tags: list[str] = []
    try:
        interest_task = infer_interest_tags(caption_ko)
        interest_tags = await asyncio.wait_for(interest_task, timeout=10)
    except asyncio.TimeoutError:
        logger.warning("Interest inference timed out for %s", disk_path.name)
    except Exception as exc:
        logger.warning("Interest inference failed for %s: %s", disk_path.name, exc)

    if interest_tags:
        caption_ko = f"{caption_ko} | 취미 추정: {', '.join(interest_tags)}"

    await upsert_image_caption(
        db,
        image_id=photo_id,
        caption_raw_en=caption_raw_en,
        caption_ko=caption_ko,
        model_name=model_name,
        model_version=model_version,
    )

    await db.commit()
    await db.refresh(photo)
    await db.refresh(user)
    _cache_user(user)
    logger.info("Photo uploaded user_id=%s file=%s", user_id, disk_path.name)
    return _photo_response(photo, request)

@app.post("/api/photos/batch", response_model=BatchPhotoUploadResponse, tags=["photos"])
async def upload_photos_batch(
    request: Request,
    user_id: str = Form(...),
    files: List[UploadFile] = File(...),
    selected_tags: List[str] = Form([]),
    selected_tags_json: str | None = Form(None),
    db: AsyncSession = Depends(get_db),
):
    """다중 사진 업로드 (배치)"""
    logger = logging.getLogger("uvicorn.error")
    user = await _get_user_by_id(db, user_id)
    
    uploaded_photos = []
    batch_captions: list[str] = []
    suggested_tag_counts: Counter[str] = Counter()
    incoming_tags: list[str] = list(selected_tags)
    if selected_tags_json:
        try:
            parsed = json.loads(selected_tags_json)
            if isinstance(parsed, list):
                incoming_tags.extend(str(item) for item in parsed)
        except json.JSONDecodeError as exc:
            logger.warning("Invalid selected_tags_json: %s", exc)
    
    # 현재 최대 sort_order 조회
    result = await db.execute(
        select(func.max(UserPhoto.sort_order)).where(UserPhoto.user_id == user.id)
    )
    max_sort = result.scalar() or 0
    is_first_photo = max_sort == 0
    
    user_dir = UPLOAD_ROOT / user_id
    user_dir.mkdir(parents=True, exist_ok=True)
    
    for idx, file in enumerate(files):
        safe_name = Path(file.filename or f"upload_{idx}").name
        photo_id = uuid.uuid4()
        disk_path = user_dir / f"{photo_id}_{safe_name}"
        hasher = hashlib.sha256()
        
        with disk_path.open("wb") as buffer:
            while True:
                chunk = file.file.read(1024 * 1024)
                if not chunk:
                    break
                buffer.write(chunk)
                hasher.update(chunk)
        
        content_hash = hasher.hexdigest()
        file_path = f"/uploads/{user_id}/{disk_path.name}"
        file_url = _build_file_url(request, file_path)
        
        # 중복 확인
        if content_hash:
            existing_result = await db.execute(
                select(UserPhoto).where(
                    UserPhoto.user_id == user.id,
                    UserPhoto.content_hash == content_hash,
                )
            )
            existing_photo = existing_result.scalar_one_or_none()
            if existing_photo:
                disk_path.unlink(missing_ok=True)
                logger.info("Duplicate photo ignored user_id=%s hash=%s", user_id, content_hash)
                uploaded_photos.append(_photo_response(existing_photo, request))
                continue
        
        # 새 사진 저장
        sort_order = max_sort + (idx + 1) * 10
        is_primary = is_first_photo and idx == 0
        
        photo = UserPhoto(
            id=photo_id,
            user_id=user.id,
            url=file_path,
            content_hash=content_hash,
            sort_order=sort_order,
            is_primary=is_primary,
        )
        db.add(photo)
        
        # 첫 번째 사진을 프로필 이미지로 설정
        if is_primary:
            user.profile_image_url = file_url
        
        # 캡셔닝을 완료한 뒤 응답 (DB에 저장까지 완료)
        caption_raw_en = f"an uploaded image ({disk_path.name})"
        model_name = "fallback"
        model_version = "fallback"
        try:
            caption_task = asyncio.to_thread(caption_image, str(disk_path))
            caption_raw_en, model_name, model_version = await asyncio.wait_for(caption_task, timeout=30)
        except asyncio.TimeoutError:
            logger.warning("Captioning timed out for %s", disk_path.name)
        except Exception as exc:
            logger.warning("Captioning failed for %s: %s", disk_path.name, exc)

        try:
            translate_task = translate_to_korean(caption_raw_en)
            caption_ko = await asyncio.wait_for(translate_task, timeout=15)
        except asyncio.TimeoutError:
            logger.warning("Translation timed out for %s", disk_path.name)
            caption_ko = caption_raw_en
        except Exception as exc:
            logger.warning("Translation failed for %s: %s", disk_path.name, exc)
            caption_ko = caption_raw_en

        interest_tags: list[str] = []
        try:
            interest_task = infer_interest_tags(caption_ko)
            interest_tags = await asyncio.wait_for(interest_task, timeout=10)
        except asyncio.TimeoutError:
            logger.warning("Interest inference timed out for %s", disk_path.name)
        except Exception as exc:
            logger.warning("Interest inference failed for %s: %s", disk_path.name, exc)

        if interest_tags:
            suggested_tag_counts.update(interest_tags)
            caption_ko = f"{caption_ko} | 취미 추정: {', '.join(interest_tags)}"

        batch_captions.append(caption_ko)
        await upsert_image_caption(
            db,
            image_id=photo_id,
            caption_raw_en=caption_raw_en,
            caption_ko=caption_ko,
            model_name=model_name,
            model_version=model_version,
        )
        
        uploaded_photos.append(_photo_response(photo, request))
        logger.info("Photo uploaded user_id=%s file=%s", user_id, disk_path.name)
    
    await db.commit()
    
    # 모든 사진 refresh
    for photo_resp in uploaded_photos:
        photo_id = uuid.UUID(photo_resp["id"])
        result = await db.execute(select(UserPhoto).where(UserPhoto.id == photo_id))
        photo = result.scalar_one_or_none()
        if photo:
            await db.refresh(photo)
    
    await db.refresh(user)
    _cache_user(user)
    
    selected_tags: set[str] = set(str(item) for item in incoming_tags if item)
    if user.profile_data:
        for key in ("tags", "interests", "photo_interests", "hobbies", "selected_tags"):
            value = user.profile_data.get(key)
            if isinstance(value, list):
                selected_tags.update(str(item) for item in value)

    suggested_tags = [
        tag for tag, _count in suggested_tag_counts.most_common()
        if tag not in selected_tags
    ][:5]

    embedding: list[float] | None = None
    map_position: dict[str, float] | None = None
    if batch_captions or selected_tags:
        user_description = None
        if user.profile_data:
            user_description = user.profile_data.get("bio") or user.profile_data.get("description")
        unique_captions: list[str] = []
        seen: set[str] = set()
        for caption in batch_captions:
            if caption and caption not in seen:
                seen.add(caption)
                unique_captions.append(caption)
        image_captions_for_embedding = unique_captions
        final_text = build_final_text(
            selected_tags=list(selected_tags)[:10],
            user_description=user_description,
            image_captions=image_captions_for_embedding,
        )
        source_hash = compute_source_hash(final_text)
        embedding, model_name, model_version = await embed_text(final_text)
        await deactivate_embeddings(db, user.id)
        new_embedding = await create_embedding(
            db,
            user_id=user.id,
            embedding_type="profile_v1",
            model_name=model_name,
            model_version=model_version,
            embedding=embedding,
            source_hash=source_hash,
        )
        await db.commit()
        await db.refresh(new_embedding)
        log_embedding_io(
            user_name=user.nickname,
            user_id=str(user.id),
            input_text=final_text,
            image_captions=image_captions_for_embedding,
            image_tags=suggested_tags,
            embedding=embedding,
            model_name=model_name,
            model_version=model_version,
        )
        map_position = {
            "x": float(embedding[0]) if embedding else 0.0,
            "y": float(embedding[1]) if embedding else 0.0,
        }

    logger.info("Batch upload completed user_id=%s count=%d", user_id, len(uploaded_photos))
    return BatchPhotoUploadResponse(
        photos=uploaded_photos,
        suggested_tags=suggested_tags,
        image_tags=suggested_tags,
        embedding=embedding,
        map_position=map_position,
    )

@app.get("/api/photos/user/{user_id}", response_model=List[PhotoUploadResponse])
async def get_user_photos(
    user_id: str,
    request: Request,
    db: AsyncSession = Depends(get_db),
):
    """사용자의 사진 목록 조회"""
    user = await _get_user_by_id(db, user_id)
    result = await db.execute(
        select(UserPhoto).where(UserPhoto.user_id == user.id).order_by(UserPhoto.sort_order)
    )
    photos = result.scalars().all()
    return [_photo_response(photo, request) for photo in photos]

# ==================== Group APIs ====================

@app.post("/api/groups", response_model=GroupResponse)
async def create_group(request: GroupCreateRequest, db: AsyncSession = Depends(get_db)):
    """그룹 생성"""
    creator = await _get_user_by_id(db, request.creator_id)
    group = Group(
        name=request.name,
        description=request.description,
        created_by=creator.id,
    )
    db.add(group)
    try:
        await db.flush()
        db.add(
            GroupMember(
                group_id=group.id,
                user_id=creator.id,
                role="owner",
            )
        )
        await db.commit()
    except IntegrityError:
        await db.rollback()
        raise HTTPException(status_code=409, detail="Group already exists")
    await db.refresh(group)
    return _group_response(group, [creator.id])

@app.get("/api/groups/{group_id}", response_model=GroupResponse)
async def get_group(group_id: str, db: AsyncSession = Depends(get_db)):
    """그룹 정보 조회"""
    group = await _get_group_by_id(db, group_id)
    member_ids = await _get_group_member_ids(db, group.id)
    return _group_response(group, member_ids)

@app.get("/api/groups/user/{user_id}", response_model=List[GroupResponse])
async def get_user_groups(user_id: str, db: AsyncSession = Depends(get_db)):
    """사용자가 속한 그룹 목록 조회"""
    user = await _get_user_by_id(db, user_id)
    result = await db.execute(
        select(Group)
        .join(GroupMember, GroupMember.group_id == Group.id)
        .where(GroupMember.user_id == user.id)
    )
    group_by_id = {group.id: group for group in result.scalars().all()}

    creator_result = await db.execute(
        select(Group).where(Group.created_by == user.id)
    )
    for group in creator_result.scalars().all():
        group_by_id.setdefault(group.id, group)

    existing_membership = await db.execute(
        select(GroupMember.group_id).where(GroupMember.user_id == user.id)
    )
    membership_ids = {row[0] for row in existing_membership.all()}
    missing_memberships = [
        group_id for group_id in group_by_id.keys() if group_id not in membership_ids
    ]
    if missing_memberships:
        for group_id in missing_memberships:
            db.add(GroupMember(group_id=group_id, user_id=user.id, role="owner"))
        await db.commit()

    responses: list[dict] = []
    for group in group_by_id.values():
        member_ids = await _get_group_member_ids(db, group.id)
        responses.append(_group_response(group, member_ids))
    return responses

@app.post("/api/groups/{group_id}/members", response_model=GroupResponse)
async def add_group_member(
    group_id: str,
    request: AddMemberRequest,
    db: AsyncSession = Depends(get_db),
):
    """그룹에 멤버 추가"""
    group = await _get_group_by_id(db, group_id)
    user = await _get_user_by_id(db, request.user_id)
    result = await db.execute(
        select(GroupMember).where(
            GroupMember.group_id == group.id,
            GroupMember.user_id == user.id,
        )
    )
    existing = result.scalar_one_or_none()
    if not existing:
        db.add(GroupMember(group_id=group.id, user_id=user.id, role="member"))
        await db.commit()
    member_ids = await _get_group_member_ids(db, group.id)
    return _group_response(group, member_ids)

@app.delete("/api/groups/{group_id}/members/{user_id}")
async def remove_group_member(group_id: str, user_id: str, db: AsyncSession = Depends(get_db)):
    """그룹에서 멤버 제거"""
    group = await _get_group_by_id(db, group_id)
    user = await _get_user_by_id(db, user_id)
    await db.execute(
        delete(GroupMember).where(
            GroupMember.group_id == group.id,
            GroupMember.user_id == user.id,
        )
    )
    await db.commit()
    return {"message": "Member removed successfully"}


@app.get("/api/groups/{group_id}/detail", response_model=GroupDetailResponse)
async def get_group_detail(group_id: str, db: AsyncSession = Depends(get_db)):
    group = await _get_group_by_id(db, group_id)
    result = await db.execute(
        select(func.count()).select_from(GroupMember).where(GroupMember.group_id == group.id)
    )
    member_count = result.scalar() or 0
    created_at = group.created_at.isoformat() if group.created_at else ""
    return GroupDetailResponse(
        id=str(group.id),
        name=group.name,
        description=group.description,
        iconType="default",
        memberCount=member_count,
        isPublic=True,
        createdByUserId=str(group.created_by) if group.created_by else "",
        createdAt=created_at,
        updatedAt=created_at,
        profileImageUrl=None,
        activityStatus="오늘 활동",
    )


@app.get("/api/users/{user_id}/embedding", response_model=UserEmbeddingResponse)
async def get_user_embedding(user_id: str, db: AsyncSession = Depends(get_db)):
    user = await _get_user_by_id(db, user_id)
    active = await get_active_embedding(db, user.id)
    vector = _embedding_vector_or_zero(active.embedding if active else None)
    return UserEmbeddingResponse(
        userId=str(user.id),
        userName=user.nickname or "",
        profileImageUrl=user.profile_image_url,
        embeddingVector=vector,
        activityStatus="활동중",
    )


@app.get("/api/groups/{group_id}/embeddings", response_model=GroupEmbeddingResponse)
async def get_group_embeddings(
    group_id: str,
    current_user_id: str | None = Query(None),
    db: AsyncSession = Depends(get_db),
):
    group = await _get_group_by_id(db, group_id)
    member_ids = await _get_group_member_ids(db, group.id)
    if not member_ids:
        raise HTTPException(status_code=404, detail="Group has no members")

    current_uuid: uuid.UUID | None = None
    if current_user_id:
        try:
            candidate = uuid.UUID(current_user_id)
        except ValueError:
            candidate = None
        if candidate and candidate in member_ids:
            current_uuid = candidate
    if current_uuid is None:
        if group.created_by and group.created_by in member_ids:
            current_uuid = group.created_by
        else:
            current_uuid = member_ids[0]

    result = await db.execute(select(User).where(User.id.in_(member_ids)))
    users = {user.id: user for user in result.scalars().all()}

    async def _build_embedding(user_id: uuid.UUID) -> UserEmbeddingResponse:
        user = users.get(user_id)
        if not user:
            return UserEmbeddingResponse(
                userId=str(user_id),
                userName="",
                profileImageUrl=None,
                embeddingVector=_embedding_vector_or_zero(None),
            )
        active = await get_active_embedding(db, user.id)
        vector = _embedding_vector_or_zero(active.embedding if active else None)
        return UserEmbeddingResponse(
            userId=str(user.id),
            userName=user.nickname or "",
            profileImageUrl=user.profile_image_url,
            embeddingVector=vector,
            activityStatus="활동중",
        )

    current_embedding = await _build_embedding(current_uuid)
    other_embeddings = []
    for member_id in member_ids:
        if member_id == current_uuid:
            continue
        other_embeddings.append(await _build_embedding(member_id))

    return GroupEmbeddingResponse(
        groupId=str(group.id),
        currentUserId=str(current_uuid),
        currentUserEmbedding=current_embedding,
        otherUserEmbeddings=other_embeddings,
    )

# ==================== Tag/Analysis APIs ====================

@app.post("/api/analyze/tags", response_model=TagAnalysisResponse)
async def analyze_images_for_tags(
    request: TagAnalysisRequest,
    db: AsyncSession = Depends(get_db),
):
    """이미지를 분석하여 태그 생성 (Mock 구현)"""
    await _ensure_user_cached(db, request.user_id)

    # Mock 태그 반환 (실제로는 AI 모델 사용)
    mock_tags = ["여행", "음식", "카페", "운동", "독서", "영화", "음악", "게임"]
    mock_categories = ["라이프스타일", "취미", "문화"]
    mock_interests = ["아웃도어", "실내활동", "예술"]

    return {
        "tags": mock_tags[:5],
        "categories": mock_categories,
        "interests": mock_interests[:3]
    }

@app.post("/api/analyze/images", response_model=ImageAnalysisResponse)
async def analyze_images(
    request: ImageAnalysisRequest,
    db: AsyncSession = Depends(get_db),
):
    """
    여러 이미지를 분석하여 캡셔닝 + 키워드 추출
    실제로는 BLIP, CLIP 등의 모델 사용
    """
    await _ensure_user_cached(db, request.user_id)

    # Mock 이미지 분석 결과
    mock_captions = [
        "사람이 카페에서 커피를 마시고 있다",
        "아름다운 산 풍경과 하이킹하는 사람들",
        "맛있는 파스타 요리가 테이블에 놓여있다",
        "해변에서 일몰을 감상하는 모습",
        "책을 읽으며 편안하게 휴식하는 장면"
    ]

    mock_keywords_pool = [
        ["카페", "커피", "실내", "휴식", "음료"],
        ["등산", "자연", "산", "아웃도어", "운동"],
        ["음식", "파스타", "맛집", "이탈리안", "요리"],
        ["여행", "바다", "일몰", "휴가", "해변"],
        ["독서", "책", "힐링", "인테리어", "취미"]
    ]

    import random
    results = []
    all_keywords_dict = {}

    for i, image_url in enumerate(request.image_urls):
        caption_idx = i % len(mock_captions)
        keywords_idx = i % len(mock_keywords_pool)

        keywords = []
        for keyword in mock_keywords_pool[keywords_idx]:
            confidence = random.uniform(0.7, 0.95)
            keywords.append(ImageKeyword(
                keyword=keyword,
                confidence=confidence,
                category=random.choice(["hobby", "interest", "lifestyle", "food", "travel"])
            ))

            # 키워드 빈도수 카운트
            if keyword not in all_keywords_dict:
                all_keywords_dict[keyword] = {"count": 0, "total_confidence": 0.0}
            all_keywords_dict[keyword]["count"] += 1
            all_keywords_dict[keyword]["total_confidence"] += confidence

        results.append(ImageAnalysisResult(
            image_url=image_url,
            caption=mock_captions[caption_idx],
            keywords=keywords
        ))

    # TOP-5 추천 태그 계산 (빈도수 * 평균 confidence)
    keyword_scores = []
    for keyword, data in all_keywords_dict.items():
        avg_confidence = data["total_confidence"] / data["count"]
        score = data["count"] * avg_confidence
        keyword_scores.append((keyword, score, avg_confidence))

    keyword_scores.sort(key=lambda x: x[1], reverse=True)
    recommended_tags = [kw[0] for kw in keyword_scores[:5]]

    all_keywords = [
        ImageKeyword(keyword=kw[0], confidence=kw[2], category="recommended")
        for kw in keyword_scores[:10]
    ]

    return ImageAnalysisResponse(
        user_id=request.user_id,
        results=results,
        recommended_tags=recommended_tags,
        all_keywords=all_keywords
    )

@app.post("/api/generate-embedding", response_model=GenerateEmbeddingResponse, tags=["embedding"])
async def generate_embedding(
    request: GenerateEmbeddingRequest,
    db: AsyncSession = Depends(get_db),
):
    """
    사용자 프로필 전체를 임베딩 벡터로 변환
    PDF 명세 기반 임베딩 파이프라인
    """
    user = await _get_user_by_id(db, request.user_id)

    selected_tags = (request.tags or []) + (request.image_keywords or [])
    if not selected_tags and user.profile_data:
        for key in ("tags", "interests", "photo_interests", "hobbies", "selected_tags"):
            value = user.profile_data.get(key)
            if isinstance(value, list):
                selected_tags.extend([str(item) for item in value])
    selected_tags = selected_tags[:10]
    user_description = request.bio or (user.profile_data.get("bio") if user.profile_data else None)
    image_captions = await get_recent_captions(db, user.id, limit=5)
    if not image_captions:
        logging.getLogger("uvicorn.error").warning(
            "Embedding input has no image captions user_id=%s",
            request.user_id,
        )

    final_text = build_final_text(
        selected_tags=selected_tags,
        user_description=user_description,
        image_captions=image_captions,
    )
    source_hash = compute_source_hash(final_text)

    try:
        active = await get_active_embedding(db, user.id)
    except Exception as exc:
        raise HTTPException(status_code=503, detail="Embedding storage unavailable") from exc
    if active and active.source_hash == source_hash:
        embedding = active.embedding
        model_name = active.model_name
        model_version = active.model_version
    else:
        embedding, model_name, model_version = await embed_text(final_text)
        await deactivate_embeddings(db, user.id)
        new_embedding = await create_embedding(
            db,
            user_id=user.id,
            embedding_type="profile_v1",
            model_name=model_name,
            model_version=model_version,
            embedding=embedding,
            source_hash=source_hash,
        )
        await db.commit()
        await db.refresh(new_embedding)

    user_name = user.nickname or request.nickname
    log_embedding_io(
        user_name=user_name,
        user_id=str(user.id),
        input_text=final_text,
        image_captions=image_captions,
        image_tags=selected_tags[:5],
        embedding=embedding,
        model_name=model_name,
        model_version=model_version,
    )

    map_x = float(embedding[0]) if embedding else 0.0
    map_y = float(embedding[1]) if embedding else 0.0

    return GenerateEmbeddingResponse(
        user_id=request.user_id,
        embedding=embedding,
        map_position={"x": map_x, "y": map_y},
    )


@app.post("/api/embedding/text", response_model=GenerateEmbeddingResponse, tags=["embedding"])
async def generate_embedding_from_text(
    request: TextEmbeddingRequest,
    db: AsyncSession = Depends(get_db),
):
    """텍스트만으로 임베딩 생성"""
    user = await _get_user_by_id(db, request.user_id)
    final_text = request.text
    source_hash = compute_source_hash(final_text)

    embedding, model_name, model_version = await embed_text(final_text)
    await deactivate_embeddings(db, user.id)
    new_embedding = await create_embedding(
        db,
        user_id=user.id,
        embedding_type="profile_v1",
        model_name=model_name,
        model_version=model_version,
        embedding=embedding,
        source_hash=source_hash,
    )
    await db.commit()
    await db.refresh(new_embedding)

    log_embedding_io(
        user_name=user.nickname,
        user_id=str(user.id),
        input_text=final_text,
        image_captions=[],
        image_tags=[],
        embedding=embedding,
        model_name=model_name,
        model_version=model_version,
    )

    map_x = float(embedding[0]) if embedding else 0.0
    map_y = float(embedding[1]) if embedding else 0.0

    return GenerateEmbeddingResponse(
        user_id=request.user_id,
        embedding=embedding,
        map_position={"x": map_x, "y": map_y},
    )

# ==================== Test APIs (개발용) ====================

@app.post("/api/users/test")
async def create_test_user(user_data: dict):
    return {
        "id": "test-user-123",
        "provider": user_data.get("provider", "kakao"),
        "provider_user_id": user_data.get("provider_user_id", "12345"),
        "nickname": user_data.get("nickname", "Test User"),
        "profile_image_url": user_data.get("profile_image_url"),
        "profile_data": {},
        "created_at": "2026-01-09T14:00:00Z",
        "updated_at": "2026-01-09T14:00:00Z"
    }

@app.get("/api/users/test/{user_id}")
async def get_test_user(user_id: str):
    return {
        "id": user_id,
        "provider": "kakao",
        "provider_user_id": "12345",
        "nickname": "Test User",
        "profile_image_url": None,
        "profile_data": {},
        "created_at": "2026-01-09T14:00:00Z",
        "updated_at": "2026-01-09T14:00:00Z"
    }
