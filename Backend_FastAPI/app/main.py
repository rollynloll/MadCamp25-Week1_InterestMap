from fastapi import FastAPI, HTTPException, UploadFile, File, Form, Depends, Request, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from collections import Counter
import json
from typing import List, Optional, Dict, Any
import asyncio
import hashlib
import uuid
from datetime import datetime, timezone
import logging
from pathlib import Path
from urllib.parse import urlparse
from sqlalchemy import text, select, func, inspect, delete, update
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession
from app.schemas import (
    UserCreateRequest, UserUpdateRequest, UserResponse,
    GroupCreateRequest, GroupResponse, GroupDetailResponse, GroupEmbeddingResponse,
    GroupSearchItem, GroupSearchResponse,
    UserEmbeddingResponse, GraphNodePositionResponse, AddMemberRequest,
    SubgroupCreateRequest, SubgroupItemResponse,
    PhotoUploadResponse,
    ImageAnalysisRequest, ImageAnalysisResponse, ImageAnalysisResult, ImageKeyword,
    GenerateEmbeddingRequest, GenerateEmbeddingResponse,
    TextEmbeddingRequest, BatchPhotoUploadResponse,
    PublicMessageCreateRequest, PublicMessageItem
)
from app.auth.router import router as auth_router
from app.groups.router import router as groups_router
from app.me.router import router as me_router
from app.db.session import engine, get_db, AsyncSessionLocal
from app.db.base import Base
from app.core.config import settings
import app.models  # ensure models are registered for metadata
from app.models.user import User
from app.models.notion_user import NotionUser
from app.models.photo import UserPhoto
from app.models.group import Group, GroupMember
from app.models.notion_group_member import NotionGroupMember
from app.models.message import GroupMessage
from app.services.embedding.captioning import caption_image, is_blip_ready
from app.services.embedding.composer import build_final_text
from app.services.embedding.embedding_log import log_embedding_io
from app.services.embedding.group_map import GroupMapInput, build_group_map_positions
from app.services.embedding.interest import infer_interest_tags
from app.services.embedding.openai_embed import embed_text, EMBEDDING_DIM, MODEL_NAME, MODEL_VERSION
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


def _master_user_ids() -> set[str]:
    raw = settings.MASTER_USER_IDS or ""
    return {value.strip() for value in raw.split(",") if value.strip()}


def _is_master_user(user_id: str) -> bool:
    return user_id in _master_user_ids()


def _is_spectator_user(user: User) -> bool:
    return user.provider == "test"


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
    * `/groups/*` - 그룹/채팅
    * `/api/*` - 앱 클라이언트용 사용자/사진/그룹/분석/임베딩
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
            "name": "users",
            "description": "사용자 생성/조회/수정"
        },
        {
            "name": "photos",
            "description": "사진 업로드 및 조회"
        },
        {
            "name": "analysis",
            "description": "이미지 분석/추천 태그"
        },
        {
            "name": "embedding",
            "description": "임베딩 생성 및 조회"
        },
        {
            "name": "system",
            "description": "헬스 체크 등 시스템 엔드포인트"
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

# DB-backed auth/me/groups endpoints
app.include_router(auth_router)
app.include_router(me_router)
app.include_router(groups_router)

# In-memory 데이터베이스 (실제로는 PostgreSQL 사용)
users_db: Dict[str, dict] = {}
photos_db: Dict[str, dict] = {}
groups_db: Dict[str, dict] = {}
user_provider_map: Dict[str, str] = {}  # (provider, provider_user_id) -> user_id


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


async def _ensure_user_embedding_columns() -> None:
    async with engine.begin() as conn:
        def _missing_columns(sync_conn) -> list[tuple[str, str]]:
            insp = inspect(sync_conn)
            if "users" not in insp.get_table_names():
                return []
            cols = {col["name"] for col in insp.get_columns("users")}
            missing: list[tuple[str, str]] = []
            if "embedding" not in cols:
                missing.append(("embedding", "JSONB"))
            if "embedding_updated_at" not in cols:
                missing.append(("embedding_updated_at", "TIMESTAMPTZ"))
            return missing

        missing = await conn.run_sync(_missing_columns)
        for column_name, ddl in missing:
            await conn.execute(text(f"ALTER TABLE users ADD COLUMN {column_name} {ddl}"))

async def _backfill_user_embeddings() -> None:
    logger = logging.getLogger("uvicorn.error")
    async with engine.begin() as conn:
        def _has_user_embeddings(sync_conn) -> bool:
            insp = inspect(sync_conn)
            return "user_embeddings" in insp.get_table_names()

        has_table = await conn.run_sync(_has_user_embeddings)
        if not has_table:
            return
        try:
            await conn.execute(
                text(
                    "UPDATE users "
                    "SET embedding = to_jsonb(ue.embedding::real[]), "
                    "    embedding_updated_at = ue.updated_at "
                    "FROM user_embeddings ue "
                    "WHERE ue.user_id = users.id "
                    "  AND ue.is_active = true "
                    "  AND users.embedding IS NULL"
                )
            )
        except Exception as exc:
            logger.warning("Embedding backfill skipped: %s", exc)


@app.on_event("startup")
async def init_db_schema() -> None:
    async with engine.begin() as conn:
        await conn.run_sync(lambda sync_conn: Base.metadata.create_all(sync_conn))
    await _ensure_user_embedding_columns()
    await _backfill_user_embeddings()
    await _ensure_photo_hash_index()


def _cache_user(user: User, is_new_user: bool = False) -> dict:
    profile_data = user.profile_data or {}
    if "is_profile_complete" not in profile_data:
        profile_data = dict(profile_data)
        profile_data["is_profile_complete"] = bool(
            profile_data.get("interests")
            or profile_data.get("photo_interests")
            or profile_data.get("image_count")
            or user.profile_image_url
        )
        user.profile_data = profile_data
    user_data = {
        "id": str(user.id),
        "provider": user.provider,
        "provider_user_id": user.provider_user_id,
        "nickname": user.nickname,
        "profile_image_url": _normalize_upload_url(user.profile_image_url),
        "profile_data": profile_data,
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


async def _get_notion_user_by_id(db: AsyncSession, user_id: str) -> NotionUser:
    try:
        user_uuid = uuid.UUID(user_id)
    except ValueError as exc:
        raise HTTPException(status_code=404, detail="Notion user not found") from exc
    result = await db.execute(select(NotionUser).where(NotionUser.id == user_uuid))
    user = result.scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="Notion user not found")
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


async def _get_notion_member_ids(db: AsyncSession, group_id: uuid.UUID) -> list[uuid.UUID]:
    result = await db.execute(
        select(NotionGroupMember.notion_user_id).where(
            NotionGroupMember.group_id == group_id
        )
    )
    return [row[0] for row in result.all()]


async def _get_all_group_member_ids(db: AsyncSession, group_id: uuid.UUID) -> list[uuid.UUID]:
    user_ids = await _get_group_member_ids(db, group_id)
    notion_ids = await _get_notion_member_ids(db, group_id)
    return user_ids + notion_ids


def _group_response(group: Group, member_ids: list[uuid.UUID]) -> dict:
    profile = group.group_profile or {}
    raw_tags = profile.get("tags") or profile.get("interests") or []
    tags = [str(tag) for tag in raw_tags] if isinstance(raw_tags, list) else []
    region = profile.get("region") or ""
    image_url = _normalize_upload_url(profile.get("image_url")) or ""
    icon_type = profile.get("icon_type") or ""
    is_public = bool(profile.get("is_public", True))
    return {
        "id": str(group.id),
        "name": group.name,
        "creator_id": str(group.created_by) if group.created_by else "",
        "description": group.description,
        "member_ids": [str(member_id) for member_id in member_ids],
        "created_at": group.created_at.isoformat() if group.created_at else "",
        "tags": tags,
        "region": region,
        "image_url": image_url,
        "icon_type": icon_type,
        "is_public": is_public,
    }


def _to_float_vector(embedding: list[float] | None) -> list[float] | None:
    if not embedding:
        return None
    try:
        return [float(value) for value in embedding]
    except (TypeError, ValueError):
        return None


async def _collect_group_member_embeddings(
    db: AsyncSession,
    user_ids: list[uuid.UUID],
    notion_ids: list[uuid.UUID],
) -> list[list[float]]:
    vectors: list[list[float]] = []
    if user_ids:
        result = await db.execute(select(User.embedding).where(User.id.in_(user_ids)))
        for raw in result.scalars().all():
            vector = _to_float_vector(raw)
            if vector:
                vectors.append(vector)
    if notion_ids:
        result = await db.execute(select(NotionUser.embedding).where(NotionUser.id.in_(notion_ids)))
        for raw in result.scalars().all():
            vector = _to_float_vector(raw)
            if vector:
                vectors.append(vector)
    return vectors


def _average_vectors(vectors: list[list[float]]) -> list[float] | None:
    if not vectors:
        return None
    dim = len(vectors[0])
    if dim == 0:
        return None
    accumulator = [0.0] * dim
    count = 0
    for vector in vectors:
        if len(vector) != dim:
            continue
        for i, value in enumerate(vector):
            accumulator[i] += value
        count += 1
    if count == 0:
        return None
    return [value / count for value in accumulator]


async def _recompute_group_embedding(db: AsyncSession, group: Group) -> None:
    try:
        user_ids = await _get_group_member_ids(db, group.id)
        notion_ids = await _get_notion_member_ids(db, group.id)
        vectors = await _collect_group_member_embeddings(db, user_ids, notion_ids)
        average = _average_vectors(vectors)
        timestamp = datetime.now(timezone.utc)
        values = {
            "embedding": average,
            "embedding_updated_at": timestamp,
        }
        await db.execute(update(Group).where(Group.id == group.id).values(**values))
        await db.commit()
    except Exception as exc:
        logging.getLogger("uvicorn.error").warning(
            "Failed to recompute embedding for group_id=%s: %s", group.id, exc
        )


def _build_subgroup_name(parent: Group, index: int) -> str:
    short_id = str(parent.id).split("-")[0]
    return f"{parent.name} · 소그룹 {index + 1} ({short_id})"


def _embedding_vector_or_zero(embedding: list[float] | None) -> list[float]:
    if embedding is None:
        return [0.0] * EMBEDDING_DIM
    try:
        if len(embedding) == 0:
            return [0.0] * EMBEDDING_DIM
    except TypeError:
        return [0.0] * EMBEDDING_DIM
    return list(embedding)


def _cosine_similarity(a: list[float] | None, b: list[float] | None) -> float:
    if not a or not b:
        return 0.0
    dot = 0.0
    norm_a = 0.0
    norm_b = 0.0
    for x, y in zip(a, b):
        dot += x * y
        norm_a += x * x
        norm_b += y * y
    if norm_a <= 0.0 or norm_b <= 0.0:
        return 0.0
    return dot / (norm_a**0.5 * norm_b**0.5)


def _normalize_upload_url(value: str | None) -> str | None:
    if not value:
        return value
    parsed = urlparse(value)
    if parsed.scheme and parsed.netloc:
        return parsed.path if parsed.path.startswith("/uploads/") else value
    if value.startswith("/uploads/"):
        return value
    if value.startswith("uploads/"):
        return f"/{value}"
    return value


def _build_file_url(request: Request, file_path: str) -> str:
    return _normalize_upload_url(file_path) or ""


def _photo_response(photo: UserPhoto, request: Request) -> dict:
    file_path = photo.url
    return {
        "id": str(photo.id),
        "user_id": str(photo.user_id),
        "file_path": _normalize_upload_url(file_path) or "",
        "file_url": _build_file_url(request, file_path),
        "uploaded_at": photo.created_at.isoformat() if photo.created_at else None,
    }


async def _get_primary_photo_url(db: AsyncSession, user_id: uuid.UUID) -> str | None:
    result = await db.execute(
        select(UserPhoto.url).where(
            UserPhoto.user_id == user_id,
            UserPhoto.is_primary == True,  # noqa: E712
        )
    )
    row = result.first()
    return row[0] if row else None


async def _generate_caption_data(
    disk_path: Path,
    timeout_caption: int = 30,
    timeout_translate: int = 15,
    timeout_interest: int = 10,
) -> tuple[str, str, str, str, list[str]]:
    logger = logging.getLogger("uvicorn.error")
    caption_raw_en = f"an uploaded image ({disk_path.name})"
    caption_ko = caption_raw_en
    model_name = "fallback"
    model_version = "fallback"
    if not is_blip_ready():
        timeout_caption = max(timeout_caption, 180)
        logger.info("Captioning warm-up detected, extending timeout to %ss", timeout_caption)

    try:
        logger.info("Captioning start image=%s", disk_path.name)
        caption_task = asyncio.to_thread(caption_image, str(disk_path))
        caption_raw_en, model_name, model_version = await asyncio.wait_for(
            caption_task, timeout=timeout_caption
        )
        logger.info("Captioning done image=%s", disk_path.name)
    except asyncio.TimeoutError:
        logger.warning("Captioning timed out for %s", disk_path.name)
    except Exception as exc:
        logger.warning("Captioning failed for %s: %s", disk_path.name, exc)

    try:
        translate_task = translate_to_korean(caption_raw_en)
        caption_ko = await asyncio.wait_for(translate_task, timeout=timeout_translate)
    except asyncio.TimeoutError:
        logger.warning("Translation timed out for %s", disk_path.name)
        caption_ko = caption_raw_en
    except Exception as exc:
        logger.warning("Translation failed for %s: %s", disk_path.name, exc)
        caption_ko = caption_raw_en

    interest_tags: list[str] = []
    try:
        interest_task = infer_interest_tags(caption_ko)
        interest_tags = await asyncio.wait_for(interest_task, timeout=timeout_interest)
    except asyncio.TimeoutError:
        logger.warning("Interest inference timed out for %s", disk_path.name)
    except Exception as exc:
        logger.warning("Interest inference failed for %s: %s", disk_path.name, exc)

    if interest_tags:
        caption_ko = f"{caption_ko} | 취미 추정: {', '.join(interest_tags)}"

    return caption_raw_en, caption_ko, model_name, model_version, interest_tags


async def _process_photo_captions(
    user_id: str,
    photo_jobs: list[tuple[uuid.UUID, Path]],
    incoming_tags: list[str],
    compute_embedding: bool,
) -> None:
    if not photo_jobs:
        return
    logger = logging.getLogger("uvicorn.error")
    try:
        async with AsyncSessionLocal() as session:
            try:
                user_uuid = uuid.UUID(user_id)
            except ValueError:
                logger.warning("Background captioning invalid user_id=%s", user_id)
                return
            result = await session.execute(
                select(User.id, User.nickname, User.profile_data).where(User.id == user_uuid)
            )
            row = result.one_or_none()
            if not row:
                logger.warning("Background captioning missing user_id=%s", user_id)
                return
            _, user_nickname, user_profile_data = row
            batch_captions: list[str] = []
            suggested_tag_counts: Counter[str] = Counter()

            for photo_id, disk_path in photo_jobs:
                (
                    caption_raw_en,
                    caption_ko,
                    caption_model_name,
                    caption_model_version,
                    interest_tags,
                ) = await _generate_caption_data(disk_path)
                if interest_tags:
                    suggested_tag_counts.update(interest_tags)
                batch_captions.append(caption_ko)
                await upsert_image_caption(
                    session,
                    image_id=photo_id,
                    caption_raw_en=caption_raw_en,
                    caption_ko=caption_ko,
                    model_name=caption_model_name,
                    model_version=caption_model_version,
                )

            await session.commit()

            if not compute_embedding:
                return

            selected_tags: set[str] = set(str(item) for item in incoming_tags if item)
            if user_profile_data:
                for key in ("tags", "interests", "photo_interests", "hobbies", "selected_tags"):
                    value = user_profile_data.get(key)
                    if isinstance(value, list):
                        selected_tags.update(str(item) for item in value if item)

            suggested_tags = [
                tag for tag, _count in suggested_tag_counts.most_common()
                if tag not in selected_tags
            ][:5]
            profile_data = dict(user_profile_data or {})
            profile_data["suggested_tags"] = suggested_tags
            await session.execute(
                update(User)
                .where(User.id == user_uuid)
                .values(profile_data=profile_data)
            )
            await session.commit()

            if batch_captions or selected_tags:
                user_description = None
                if user_profile_data:
                    user_description = user_profile_data.get("bio") or user_profile_data.get("description")
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
                try:
                    embedding, model_name, model_version = await embed_text(final_text)
                except Exception as exc:
                    logger.warning(
                        "Embedding generation failed user_id=%s error=%s",
                        user_id,
                        exc,
                    )
                else:
                    await deactivate_embeddings(session, user_uuid)
                    await create_embedding(
                        session,
                        user_id=user_uuid,
                        embedding=embedding,
                    )
                    await session.commit()
                    log_embedding_io(
                        user_name=user_nickname,
                        user_id=str(user_uuid),
                        input_text=final_text,
                        image_captions=image_captions_for_embedding,
                        image_tags=suggested_tags,
                        embedding=embedding,
                        model_name=model_name,
                        model_version=model_version,
                    )
            if compute_embedding:
                profile_data = dict(profile_data)
                profile_data["captioning_status"] = "done"
                await session.execute(
                    update(User)
                    .where(User.id == user_uuid)
                    .values(profile_data=profile_data)
                )
                await session.commit()
            logger.info(
                "Background captioning completed user_id=%s count=%d",
                user_id,
                len(photo_jobs),
            )
    except Exception as exc:
        logger.warning("Background captioning failed user_id=%s error=%s", user_id, exc)
        if compute_embedding:
            await _set_captioning_status(user_id, "failed")


async def _set_captioning_status(user_id: str, status: str) -> None:
    logger = logging.getLogger("uvicorn.error")
    try:
        async with AsyncSessionLocal() as session:
            try:
                user_uuid = uuid.UUID(user_id)
            except ValueError:
                logger.warning("Invalid user_id for captioning status: %s", user_id)
                return
            result = await session.execute(
                select(User.profile_data).where(User.id == user_uuid)
            )
            row = result.one_or_none()
            if not row:
                logger.warning("Missing user for captioning status: %s", user_id)
                return
            profile_data = dict(row[0] or {})
            profile_data["captioning_status"] = status
            await session.execute(
                update(User)
                .where(User.id == user_uuid)
                .values(profile_data=profile_data)
            )
            await session.commit()
    except Exception as exc:
        logger.warning(
            "Failed to update captioning status user_id=%s status=%s error=%s",
            user_id,
            status,
            exc,
        )

@app.get("/", tags=["system"])
def read_root():
    return {"message": "Hello FastAPI"}

@app.get("/health", tags=["system"])
def health_check():
    return {
        "status": "healthy",
        "service": "InterestMap Backend",
        "version": "1.0.0"
    }

# ==================== User APIs ====================

@app.post("/api/users", response_model=UserResponse, tags=["users"])
async def create_user(request: UserCreateRequest, db: AsyncSession = Depends(get_db)):
    """카카오 로그인 후 사용자 생성 또는 조회"""
    stmt = select(User).where(
        User.provider == request.provider,
        User.provider_user_id == request.provider_user_id,
    )
    result = await db.execute(stmt)
    user = result.scalar_one_or_none()
    if user:
        if request.nickname is not None and (not user.nickname or user.nickname.startswith("kakao_")):
            user.nickname = request.nickname
        if request.profile_image_url is not None:
            user.profile_image_url = _normalize_upload_url(request.profile_image_url)
        if request.profile_data is not None:
            merged = dict(user.profile_data or {})
            merged.update(request.profile_data)
            user.profile_data = merged
        await db.commit()
        await db.refresh(user)
        return _cache_user(user, is_new_user=False)

    profile_data = dict(request.profile_data or {})
    profile_data.setdefault("is_profile_complete", False)
    user = User(
        provider=request.provider,
        provider_user_id=request.provider_user_id,
        nickname=request.nickname,
        profile_image_url=_normalize_upload_url(request.profile_image_url),
        profile_data=profile_data,
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

@app.get("/api/users/{user_id}", response_model=UserResponse, tags=["users"])
async def get_user(user_id: str, db: AsyncSession = Depends(get_db)):
    """사용자 정보 조회"""
    user = await _get_user_by_id(db, user_id)
    return _cache_user(user)

@app.put("/api/users/{user_id}", response_model=UserResponse, tags=["users"])
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
        user.profile_image_url = _normalize_upload_url(request.profile_image_url)
    if request.profile_data is not None:
        merged = dict(user.profile_data or {})
        merged.update(request.profile_data)
        user.profile_data = merged

    await db.commit()
    await db.refresh(user)
    return _cache_user(user)

# ==================== Photo APIs ====================

@app.post("/api/users/{user_id}/profile-image", response_model=UserResponse, tags=["users"])
async def upload_user_profile_image(
    request: Request,
    user_id: str,
    file: UploadFile = File(...),
    db: AsyncSession = Depends(get_db),
):
    """프로필 사진 전용 업로드 (갤러리/UserPhoto에 추가하지 않음)"""
    user = await _get_user_by_id(db, user_id)

    # 1. 파일 저장
    safe_name = Path(file.filename or "profile.jpg").name
    photo_uuid = uuid.uuid4()
    user_dir = UPLOAD_ROOT / user_id
    user_dir.mkdir(parents=True, exist_ok=True)
    
    disk_path = user_dir / f"profile_{photo_uuid}_{safe_name}"
    
    with disk_path.open("wb") as buffer:
        while True:
            chunk = file.file.read(1024 * 1024)
            if not chunk:
                break
            buffer.write(chunk)
            
    # 2. URL 생성 및 DB 업데이트
    file_path = f"/uploads/{user_id}/{disk_path.name}"
    full_url = _build_file_url(request, file_path)
    
    user.profile_image_url = full_url
    
    await db.commit()
    await db.refresh(user)
    return _cache_user(user)

@app.post("/api/photos", response_model=PhotoUploadResponse, tags=["photos"])
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

    await db.commit()
    await db.refresh(photo)
    await db.refresh(user)
    _cache_user(user)
    logger.info("Photo uploaded user_id=%s file=%s", user_id, disk_path.name)
    asyncio.create_task(
        _process_photo_captions(
            user_id=user_id,
            photo_jobs=[(photo_id, disk_path)],
            incoming_tags=[],
            compute_embedding=False,
        )
    )
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
    photo_jobs: list[tuple[uuid.UUID, Path]] = []
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
        photo_jobs.append((photo_id, disk_path))
        
        uploaded_photos.append(_photo_response(photo, request))
        logger.info("Photo uploaded user_id=%s file=%s", user_id, disk_path.name)
    
    if photo_jobs:
        profile_data = dict(user.profile_data or {})
        profile_data["captioning_status"] = "processing"
        user.profile_data = profile_data

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
    
    if photo_jobs:
        asyncio.create_task(
            _process_photo_captions(
                user_id=user_id,
                photo_jobs=photo_jobs,
                incoming_tags=incoming_tags,
                compute_embedding=True,
            )
        )

    logger.info("Batch upload completed user_id=%s count=%d", user_id, len(uploaded_photos))
    return BatchPhotoUploadResponse(
        photos=uploaded_photos,
        suggested_tags=[],
        image_tags=[],
        embedding=None,
        map_position=None,
    )

@app.get("/api/photos/user/{user_id}", response_model=List[PhotoUploadResponse], tags=["photos"])
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

@app.post("/api/groups", response_model=GroupResponse, tags=["groups"])
async def create_group(request: GroupCreateRequest, db: AsyncSession = Depends(get_db)):
    """그룹 생성"""
    creator = await _get_user_by_id(db, request.creator_id)
    group_profile = {
        "tags": request.tags,
        "region": request.region or "",
        "image_url": _normalize_upload_url(request.image_url) or "",
        "icon_type": request.icon_type or "",
        "is_public": request.is_public,
    }
    group = Group(
        name=request.name,
        description=request.description,
        created_by=creator.id,
        group_profile=group_profile,
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
    await _recompute_group_embedding(db, group)
    return _group_response(group, [creator.id])

@app.get("/api/groups", response_model=List[GroupResponse], tags=["groups"])
async def list_groups(db: AsyncSession = Depends(get_db)):
    """모든 그룹 목록 조회"""
    result = await db.execute(select(Group).where(Group.is_subgroup == False))  # noqa: E712
    groups = result.scalars().all()
    responses: list[dict] = []
    for group in groups:
        member_ids = await _get_all_group_member_ids(db, group.id)
        responses.append(_group_response(group, member_ids))
    return responses

@app.get("/api/groups/user/{user_id}", response_model=List[GroupResponse], tags=["groups"])
async def get_user_groups(user_id: str, db: AsyncSession = Depends(get_db)):
    """사용자가 속한 그룹 목록 조회"""
    if _is_master_user(user_id):
        result = await db.execute(select(Group).where(Group.is_subgroup == False))  # noqa: E712
        groups = result.scalars().all()
        responses: list[dict] = []
        for group in groups:
            member_ids = await _get_all_group_member_ids(db, group.id)
            responses.append(_group_response(group, member_ids))
        return responses

    user = await _get_user_by_id(db, user_id)
    if _is_spectator_user(user):
        result = await db.execute(select(Group).where(Group.is_subgroup == False))  # noqa: E712
        groups = result.scalars().all()
        responses: list[dict] = []
        for group in groups:
            member_ids = await _get_all_group_member_ids(db, group.id)
            responses.append(_group_response(group, member_ids))
        return responses
    result = await db.execute(
        select(Group)
        .join(GroupMember, GroupMember.group_id == Group.id)
        .where(GroupMember.user_id == user.id, Group.is_subgroup == False)  # noqa: E712
    )
    group_by_id = {group.id: group for group in result.scalars().all()}

    responses: list[dict] = []
    for group in group_by_id.values():
        member_ids = await _get_all_group_member_ids(db, group.id)
        responses.append(_group_response(group, member_ids))
    return responses


@app.get(
    "/api/groups/search",
    response_model=GroupSearchResponse,
    tags=["groups"],
)
async def search_groups(
    current_user_id: str | None = Query(None),
    limit: int = Query(40, ge=1, le=200),
    db: AsyncSession = Depends(get_db),
):
    """추천 순으로 그룹을 가져오되 사용자가 속한 그룹은 제외"""
    user_uuid: uuid.UUID | None = None
    user_embedding: list[float] | None = None
    if current_user_id:
        try:
            user = await _get_user_by_id(db, current_user_id)
            user_uuid = uuid.UUID(current_user_id)
            user_embedding = _to_float_vector(user.embedding)
        except HTTPException:
            user_uuid = None
            user_embedding = None

    group_result = await db.execute(select(Group).where(Group.is_subgroup == False))  # noqa: E712
    groups = group_result.scalars().all()
    group_ids = [group.id for group in groups]

    exclude_group_ids: set[uuid.UUID] = set()
    if user_uuid:
        member_result = await db.execute(
            select(GroupMember.group_id).where(GroupMember.user_id == user_uuid)
        )
        exclude_group_ids = {row[0] for row in member_result.all()}

    member_counts: dict[uuid.UUID, int] = {}
    if group_ids:
        user_counts = await db.execute(
            select(GroupMember.group_id, func.count())
            .where(GroupMember.group_id.in_(group_ids))
            .group_by(GroupMember.group_id)
        )
        member_counts.update({row[0]: row[1] for row in user_counts.all()})

        notion_counts = await db.execute(
            select(NotionGroupMember.group_id, func.count())
            .where(NotionGroupMember.group_id.in_(group_ids))
            .group_by(NotionGroupMember.group_id)
        )
        for group_id, count in notion_counts.all():
            member_counts[group_id] = member_counts.get(group_id, 0) + count

    items: list[GroupSearchItem] = []
    for group in groups:
        if group.id in exclude_group_ids:
            continue
        profile = group.group_profile or {}
        if not bool(profile.get("is_public", True)):
            continue

        if group.embedding is None and group.embedding_updated_at is None:
            await _recompute_group_embedding(db, group)
            await db.refresh(group)

        group_vector = _to_float_vector(group.embedding if group.embedding else None)
        match_score = _cosine_similarity(user_embedding, group_vector)

        raw_tags = profile.get("tags") or profile.get("interests") or []
        tags = [str(tag) for tag in raw_tags] if isinstance(raw_tags, list) else []
        region = profile.get("region") or ""
        image_url = _normalize_upload_url(profile.get("image_url")) or ""
        icon_type = profile.get("icon_type") or ""
        member_count = member_counts.get(group.id, 0)

        items.append(
            GroupSearchItem(
                id=str(group.id),
                name=group.name,
                description=group.description,
                memberCount=member_count,
                tags=tags,
                region=region,
                imageUrl=image_url,
                iconType=icon_type,
                isPublic=bool(profile.get("is_public", True)),
                matchScore=match_score,
            )
        )

    items.sort(key=lambda item: item.matchScore, reverse=True)
    if limit and len(items) > limit:
        items = items[:limit]
    return GroupSearchResponse(items=items)

@app.post("/api/groups/{group_id}/members", response_model=GroupResponse, tags=["groups"])
async def add_group_member(
    group_id: str,
    request: AddMemberRequest,
    db: AsyncSession = Depends(get_db),
):
    """그룹에 멤버 추가"""
    group = await _get_group_by_id(db, group_id)
    user = await _get_user_by_id(db, request.user_id)
    if _is_spectator_user(user):
        member_ids = await _get_all_group_member_ids(db, group.id)
        return _group_response(group, member_ids)
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
        await _recompute_group_embedding(db, group)
    member_ids = await _get_all_group_member_ids(db, group.id)
    return _group_response(group, member_ids)


@app.post(
    "/api/groups/{group_id}/subgroups",
    response_model=List[SubgroupItemResponse],
    tags=["groups"],
)
async def create_subgroups(
    group_id: str,
    request: SubgroupCreateRequest,
    db: AsyncSession = Depends(get_db),
):
    group = await _get_group_by_id(db, group_id)
    if group.is_subgroup:
        raise HTTPException(status_code=400, detail="Cannot create subgroups from a subgroup")
    if not request.clusters:
        raise HTTPException(status_code=400, detail="Clusters are required")

    cluster_member_ids: dict[int, list[uuid.UUID]] = {}
    all_member_ids: set[uuid.UUID] = set()
    for cluster in request.clusters:
        ids: list[uuid.UUID] = []
        for raw_id in cluster.member_ids:
            try:
                member_id = uuid.UUID(raw_id)
            except ValueError:
                continue
            ids.append(member_id)
            all_member_ids.add(member_id)
        cluster_member_ids[cluster.index] = ids

    user_ids: set[uuid.UUID] = set()
    notion_ids: set[uuid.UUID] = set()
    if all_member_ids:
        user_result = await db.execute(select(User.id).where(User.id.in_(all_member_ids)))
        user_ids = {row[0] for row in user_result.all()}
        notion_result = await db.execute(
            select(NotionUser.id).where(NotionUser.id.in_(all_member_ids))
        )
        notion_ids = {row[0] for row in notion_result.all()}

    responses: list[SubgroupItemResponse] = []
    for cluster in request.clusters:
        subgroup_result = await db.execute(
            select(Group).where(
                Group.parent_group_id == group.id,
                Group.subgroup_index == cluster.index,
                Group.is_subgroup == True,  # noqa: E712
            )
        )
        subgroup = subgroup_result.scalar_one_or_none()

        if subgroup is None:
            profile = dict(group.group_profile or {})
            profile["is_public"] = False
            subgroup = Group(
                name=_build_subgroup_name(group, cluster.index),
                description=f"{group.name} 소그룹 {cluster.index + 1}",
                created_by=group.created_by,
                group_profile=profile,
                is_subgroup=True,
                parent_group_id=group.id,
                subgroup_index=cluster.index,
            )
            db.add(subgroup)
            await db.flush()

        await db.execute(
            delete(GroupMember).where(GroupMember.group_id == subgroup.id)
        )
        await db.execute(
            delete(NotionGroupMember).where(NotionGroupMember.group_id == subgroup.id)
        )

        members = cluster_member_ids.get(cluster.index, [])
        for member_id in members:
            if member_id in user_ids:
                db.add(GroupMember(group_id=subgroup.id, user_id=member_id, role="member"))
            elif member_id in notion_ids:
                db.add(
                    NotionGroupMember(
                        group_id=subgroup.id,
                        notion_user_id=member_id,
                        role="member",
                    )
                )

        responses.append(
            SubgroupItemResponse(
                id=str(subgroup.id),
                name=subgroup.name,
                cluster_index=cluster.index,
                member_ids=[str(member_id) for member_id in members],
            )
        )

    await db.commit()
    return responses

@app.delete("/api/groups/{group_id}/members/{user_id}", tags=["groups"])
async def remove_group_member(group_id: str, user_id: str, db: AsyncSession = Depends(get_db)):
    """그룹에서 멤버 제거"""
    group = await _get_group_by_id(db, group_id)
    user = await _get_user_by_id(db, user_id)
    logging.getLogger("uvicorn.error").info(
        "Remove group member requested user_id=%s group_id=%s",
        user.id,
        group.id,
    )
    await db.execute(
        delete(GroupMember).where(
            GroupMember.group_id == group.id,
            GroupMember.user_id == user.id,
        )
    )
    await db.commit()
    await _recompute_group_embedding(db, group)
    logging.getLogger("uvicorn.error").info(
        "Remove group member completed user_id=%s group_id=%s",
        user.id,
        group.id,
    )
    return {"message": "Member removed successfully"}


@app.get("/api/groups/{group_id}/messages", response_model=List[PublicMessageItem], tags=["messages"])
async def list_group_messages_public(
    group_id: str,
    limit: int = Query(default=50, ge=1, le=200),
    db: AsyncSession = Depends(get_db),
):
    group = await _get_group_by_id(db, group_id)

    query = (
        select(GroupMessage, User, NotionUser, UserPhoto.url)
        .outerjoin(User, GroupMessage.sender_id == User.id)
        .outerjoin(NotionUser, GroupMessage.notion_user_id == NotionUser.id)
        .outerjoin(
            UserPhoto,
            (UserPhoto.user_id == User.id) & (UserPhoto.is_primary == True),  # noqa: E712
        )
        .where(GroupMessage.group_id == group.id)
        .order_by(GroupMessage.created_at.desc())
        .limit(limit)
    )
    rows = (await db.execute(query)).all()

    items: list[PublicMessageItem] = []
    for message, sender, notion_sender, primary_url in rows:
        content = message.content or {}
        sender_id = sender.id if sender else (notion_sender.id if notion_sender else None)
        sender_name = sender.nickname if sender else (notion_sender.nickname if notion_sender else "알 수 없음")
        sender_photo = primary_url if sender else (notion_sender.profile_image_url if notion_sender else None)
        items.append(
            PublicMessageItem(
                id=str(message.id),
                group_id=str(message.group_id),
                user_id=str(sender_id) if sender_id else "",
                nickname=sender_name,
                primary_photo_url=_normalize_upload_url(sender_photo),
                text=content.get("text"),
                image_url=_normalize_upload_url(content.get("image_url")),
                sent_at=message.created_at,
            )
        )

    return items


@app.post("/api/groups/{group_id}/messages", response_model=PublicMessageItem, status_code=201, tags=["messages"])
async def create_group_message_public(
    group_id: str,
    payload: PublicMessageCreateRequest,
    db: AsyncSession = Depends(get_db),
):
    group = await _get_group_by_id(db, group_id)
    user: User | None = None
    notion_user: NotionUser | None = None
    try:
        user = await _get_user_by_id(db, payload.user_id)
    except HTTPException:
        notion_user = await _get_notion_user_by_id(db, payload.user_id)

    if user:
        if _is_spectator_user(user):
            raise HTTPException(status_code=403, detail="Spectator users cannot send messages")
        existing = await db.execute(
            select(GroupMember).where(
                GroupMember.group_id == group.id,
                GroupMember.user_id == user.id,
            )
        )
        if existing.scalar_one_or_none() is None:
            db.add(GroupMember(group_id=group.id, user_id=user.id, role="member"))
            await db.commit()
    else:
        existing = await db.execute(
            select(NotionGroupMember).where(
                NotionGroupMember.group_id == group.id,
                NotionGroupMember.notion_user_id == notion_user.id,
            )
        )
        if existing.scalar_one_or_none() is None:
            db.add(
                NotionGroupMember(
                    group_id=group.id,
                    notion_user_id=notion_user.id,
                    role="member",
                )
            )
            await db.commit()

    message = GroupMessage(
        group_id=group.id,
        sender_id=user.id if user else None,
        notion_user_id=notion_user.id if notion_user else None,
        content={"text": payload.text},
    )
    db.add(message)
    await db.commit()
    await db.refresh(message)

    primary_url = await _get_primary_photo_url(db, user.id) if user else None
    sender_id = user.id if user else notion_user.id
    sender_name = user.nickname if user else notion_user.nickname
    sender_photo = primary_url if user else notion_user.profile_image_url
    return PublicMessageItem(
        id=str(message.id),
        group_id=str(message.group_id),
        user_id=str(sender_id),
        nickname=sender_name,
        primary_photo_url=_normalize_upload_url(sender_photo),
        text=payload.text,
        image_url=None,
        sent_at=message.created_at,
    )


@app.post("/api/groups/{group_id}/photos", response_model=PublicMessageItem, status_code=201, tags=["messages"])
async def create_group_image_message_public(
    request: Request,
    group_id: str,
    user_id: str = Form(...),
    file: UploadFile = File(...),
    db: AsyncSession = Depends(get_db),
):
    group = await _get_group_by_id(db, group_id)
    user: User | None = None
    notion_user: NotionUser | None = None
    try:
        user = await _get_user_by_id(db, user_id)
    except HTTPException:
        notion_user = await _get_notion_user_by_id(db, user_id)

    if user:
        if _is_spectator_user(user):
            raise HTTPException(status_code=403, detail="Spectator users cannot send messages")
        existing = await db.execute(
            select(GroupMember).where(
                GroupMember.group_id == group.id,
                GroupMember.user_id == user.id,
            )
        )
        if existing.scalar_one_or_none() is None:
            db.add(GroupMember(group_id=group.id, user_id=user.id, role="member"))
            await db.commit()
    else:
        existing = await db.execute(
            select(NotionGroupMember).where(
                NotionGroupMember.group_id == group.id,
                NotionGroupMember.notion_user_id == notion_user.id,
            )
        )
        if existing.scalar_one_or_none() is None:
            db.add(
                NotionGroupMember(
                    group_id=group.id,
                    notion_user_id=notion_user.id,
                    role="member",
                )
            )
            await db.commit()

    safe_name = Path(file.filename or "group_message").name
    group_dir = UPLOAD_ROOT / "groups" / group_id / "messages"
    group_dir.mkdir(parents=True, exist_ok=True)
    disk_path = group_dir / f"{uuid.uuid4()}_{safe_name}"

    with disk_path.open("wb") as buffer:
        while True:
            chunk = file.file.read(1024 * 1024)
            if not chunk:
                break
            buffer.write(chunk)

    file_path = f"/uploads/groups/{group_id}/messages/{disk_path.name}"
    file_url = _build_file_url(request, file_path)

    message = GroupMessage(
        group_id=group.id,
        sender_id=user.id if user else None,
        notion_user_id=notion_user.id if notion_user else None,
        content={"image_url": file_url},
    )
    db.add(message)
    await db.commit()
    await db.refresh(message)

    primary_url = await _get_primary_photo_url(db, user.id) if user else None
    sender_id = user.id if user else notion_user.id
    sender_name = user.nickname if user else notion_user.nickname
    sender_photo = primary_url if user else notion_user.profile_image_url
    return PublicMessageItem(
        id=str(message.id),
        group_id=str(message.group_id),
        user_id=str(sender_id),
        nickname=sender_name,
        primary_photo_url=_normalize_upload_url(sender_photo),
        text=None,
        image_url=file_url,
        sent_at=message.created_at,
    )


@app.get("/api/groups/{group_id}/detail", response_model=GroupDetailResponse, tags=["groups"])
async def get_group_detail(group_id: str, db: AsyncSession = Depends(get_db)):
    group = await _get_group_by_id(db, group_id)
    result = await db.execute(
        select(func.count()).select_from(GroupMember).where(GroupMember.group_id == group.id)
    )
    member_count = result.scalar() or 0
    notion_result = await db.execute(
        select(func.count())
        .select_from(NotionGroupMember)
        .where(NotionGroupMember.group_id == group.id)
    )
    member_count += notion_result.scalar() or 0
    created_at = group.created_at.isoformat() if group.created_at else ""
    updated_at = created_at
    profile = group.group_profile or {}
    image_url = _normalize_upload_url(profile.get("image_url")) or ""
    icon_type = profile.get("icon_type") or ""
    is_public = bool(profile.get("is_public", True))
    return GroupDetailResponse(
        id=str(group.id),
        name=group.name,
        description=group.description,
        iconType=icon_type,
        memberCount=member_count,
        isPublic=is_public,
        createdByUserId=str(group.created_by) if group.created_by else "",
        createdAt=created_at,
        updatedAt=updated_at,
        profileImageUrl=image_url or None,
        activityStatus="오늘 활동",
    )


@app.post("/api/groups/{group_id}/profile-image", response_model=GroupResponse, tags=["groups"])
async def upload_group_profile_image(
    request: Request,
    group_id: str,
    file: UploadFile = File(...),
    db: AsyncSession = Depends(get_db),
):
    group = await _get_group_by_id(db, group_id)
    safe_name = Path(file.filename or "group_profile").name
    group_dir = UPLOAD_ROOT / "groups" / group_id
    group_dir.mkdir(parents=True, exist_ok=True)
    disk_path = group_dir / f"{uuid.uuid4()}_{safe_name}"

    with disk_path.open("wb") as buffer:
        while True:
            chunk = file.file.read(1024 * 1024)
            if not chunk:
                break
            buffer.write(chunk)

    file_path = f"/uploads/groups/{group_id}/{disk_path.name}"
    file_url = _build_file_url(request, file_path)

    profile = dict(group.group_profile or {})
    profile["image_url"] = file_url
    group.group_profile = profile
    await db.commit()
    await db.refresh(group)

    member_ids = await _get_all_group_member_ids(db, group.id)
    return _group_response(group, member_ids)


@app.get("/api/users/{user_id}/embedding", response_model=UserEmbeddingResponse, tags=["embedding"])
async def get_user_embedding(user_id: str, db: AsyncSession = Depends(get_db)):
    user = await _get_user_by_id(db, user_id)
    active = await get_active_embedding(db, user.id)
    vector = _embedding_vector_or_zero(active.embedding if active else None)
    return UserEmbeddingResponse(
        userId=str(user.id),
        userName=user.nickname or "",
        profileImageUrl=_normalize_upload_url(user.profile_image_url),
        embeddingVector=vector,
        activityStatus="활동중",
    )


@app.get("/api/groups/{group_id}/embeddings", response_model=GroupEmbeddingResponse, tags=["groups"])
async def get_group_embeddings(
    group_id: str,
    current_user_id: str | None = Query(None),
    db: AsyncSession = Depends(get_db),
):
    group = await _get_group_by_id(db, group_id)
    member_ids = await _get_group_member_ids(db, group.id)
    notion_member_ids = await _get_notion_member_ids(db, group.id)
    if not member_ids and not notion_member_ids:
        if not current_user_id:
            raise HTTPException(status_code=404, detail="Group has no members")
        try:
            current_uuid = uuid.UUID(current_user_id)
        except ValueError:
            current_uuid = None
        if current_uuid is None:
            raise HTTPException(status_code=404, detail="Group has no members")
        current_user = await _get_user_by_id(db, str(current_uuid))
        current_embedding = UserEmbeddingResponse(
            userId=str(current_user.id),
            userName=current_user.nickname or "",
            profileImageUrl=_normalize_upload_url(current_user.profile_image_url),
            embeddingVector=_embedding_vector_or_zero(
                current_user.embedding if current_user.embedding else None
            ),
            activityStatus="활동중",
        )
        node_positions = [
            GraphNodePositionResponse(
                userId=str(current_user.id),
                x=195.0,
                y=260.0,
                distance=0.0,
                similarityScore=1.0,
            )
        ]
        return GroupEmbeddingResponse(
            groupId=str(group.id),
            currentUserId=str(current_user.id),
            currentUserEmbedding=current_embedding,
            otherUserEmbeddings=[],
            nodePositions=node_positions,
        )

    current_uuid: uuid.UUID | None = None
    if current_user_id:
        try:
            candidate = uuid.UUID(current_user_id)
        except ValueError:
            candidate = None
        if candidate and (candidate in member_ids or candidate in notion_member_ids):
            current_uuid = candidate
        elif candidate:
            current_uuid = candidate
    if current_uuid is None:
        if group.created_by and group.created_by in member_ids:
            current_uuid = group.created_by
        else:
            current_uuid = member_ids[0] if member_ids else None
    if current_uuid is None and notion_member_ids:
        current_uuid = notion_member_ids[0]

    if current_uuid is None:
        raise HTTPException(status_code=404, detail="Group has no user members")

    result = await db.execute(select(User).where(User.id.in_(member_ids)))
    users = {user.id: user for user in result.scalars().all()}

    notion_users: dict[uuid.UUID, NotionUser] = {}
    if notion_member_ids:
        notion_result = await db.execute(
            select(NotionUser).where(NotionUser.id.in_(notion_member_ids))
        )
        notion_users = {user.id: user for user in notion_result.scalars().all()}

    def _build_embedding(user_id: uuid.UUID) -> UserEmbeddingResponse:
        user = users.get(user_id)
        if not user:
            return UserEmbeddingResponse(
                userId=str(user_id),
                userName="",
                profileImageUrl=None,
                embeddingVector=_embedding_vector_or_zero(None),
            )
        vector = _embedding_vector_or_zero(user.embedding if user.embedding else None)
        return UserEmbeddingResponse(
            userId=str(user.id),
            userName=user.nickname or "",
            profileImageUrl=_normalize_upload_url(user.profile_image_url),
            embeddingVector=vector,
            activityStatus="활동중",
        )

    async def _build_current_embedding(user_id: uuid.UUID) -> UserEmbeddingResponse:
        user = users.get(user_id)
        if user:
            vector = _embedding_vector_or_zero(user.embedding if user.embedding else None)
            return UserEmbeddingResponse(
                userId=str(user.id),
                userName=user.nickname or "",
                profileImageUrl=_normalize_upload_url(user.profile_image_url),
                embeddingVector=vector,
                activityStatus="활동중",
            )
        notion_user = notion_users.get(user_id)
        if notion_user:
            vector = _embedding_vector_or_zero(
                notion_user.embedding if notion_user.embedding else None
            )
            return UserEmbeddingResponse(
                userId=str(notion_user.id),
                userName=notion_user.nickname or "",
                profileImageUrl=_normalize_upload_url(notion_user.profile_image_url),
                embeddingVector=vector,
                activityStatus="활동중",
            )
        fetched_user = await _get_user_by_id(db, str(user_id))
        vector = _embedding_vector_or_zero(
            fetched_user.embedding if fetched_user.embedding else None
        )
        return UserEmbeddingResponse(
            userId=str(fetched_user.id),
            userName=fetched_user.nickname or "",
            profileImageUrl=_normalize_upload_url(fetched_user.profile_image_url),
            embeddingVector=vector,
            activityStatus="활동중",
        )

    member_inputs = []
    for member_id in member_ids:
        user = users.get(member_id)
        member_inputs.append(
            GroupMapInput(
                user_id=str(member_id),
                embedding=list(user.embedding) if user and user.embedding else None,
                updated_at=user.embedding_updated_at if user else None,
            )
        )
    for member_id in notion_member_ids:
        user = notion_users.get(member_id)
        member_inputs.append(
            GroupMapInput(
                user_id=str(member_id),
                embedding=list(user.embedding) if user and user.embedding else None,
                updated_at=user.embedding_updated_at if user else None,
            )
        )

    positions = build_group_map_positions(str(group.id), member_inputs)

    current_embedding = await _build_current_embedding(current_uuid)
    other_embeddings = []
    for member_id in member_ids:
        if member_id == current_uuid:
            continue
        other_embeddings.append(_build_embedding(member_id))
    for member_id in notion_member_ids:
        if member_id == current_uuid:
            continue
        user = notion_users.get(member_id)
        if not user:
            continue
        vector = _embedding_vector_or_zero(user.embedding if user.embedding else None)
        other_embeddings.append(
            UserEmbeddingResponse(
                userId=str(user.id),
                userName=user.nickname or "",
                profileImageUrl=_normalize_upload_url(user.profile_image_url),
                embeddingVector=vector,
                activityStatus="활동중",
            )
        )

    current_user = users.get(current_uuid)
    current_notion_user = notion_users.get(current_uuid)
    current_vector_source = None
    if current_user:
        current_vector_source = current_user.embedding
    elif current_notion_user:
        current_vector_source = current_notion_user.embedding
    current_vector = list(current_vector_source) if current_vector_source else None

    node_positions = []
    for member_id in member_ids + notion_member_ids:
        user = users.get(member_id)
        notion_user = notion_users.get(member_id)
        vector_source = user.embedding if user else (notion_user.embedding if notion_user else None)
        vector = list(vector_source) if vector_source else None
        similarity = _cosine_similarity(current_vector, vector)
        distance = 1.0 - similarity
        pos = positions.get(str(member_id))
        if pos is None:
            pos = (195.0, 260.0)
        node_positions.append(
            GraphNodePositionResponse(
                userId=str(member_id),
                x=pos[0],
                y=pos[1],
                distance=distance,
                similarityScore=similarity,
            )
        )
    if current_uuid not in member_ids and current_uuid not in notion_member_ids:
        node_positions.append(
            GraphNodePositionResponse(
                userId=str(current_uuid),
                x=195.0,
                y=260.0,
                distance=0.0,
                similarityScore=1.0,
            )
        )

    return GroupEmbeddingResponse(
        groupId=str(group.id),
        currentUserId=str(current_uuid),
        currentUserEmbedding=current_embedding,
        otherUserEmbeddings=other_embeddings,
        nodePositions=node_positions,
    )

# ==================== Tag/Analysis APIs ====================

@app.post("/api/analyze/images", response_model=ImageAnalysisResponse, tags=["analysis"])
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
    embedding, model_name, model_version = await embed_text(final_text)
    await deactivate_embeddings(db, user.id)
    await create_embedding(
        db,
        user_id=user.id,
        embedding=embedding,
    )
    await db.commit()

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
    embedding, model_name, model_version = await embed_text(final_text)
    await deactivate_embeddings(db, user.id)
    await create_embedding(
        db,
        user_id=user.id,
        embedding=embedding,
    )
    await db.commit()

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
