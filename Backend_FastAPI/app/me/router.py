from __future__ import annotations

from datetime import datetime, timezone
import json
import uuid

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import delete, func, select, update
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.deps import get_current_user
from app.db.session import get_db
from app.models.embedding import UserEmbedding
from app.models.photo import UserPhoto
from app.models.user import User
from app.schemas import (
    EmbeddingResponse,
    MeEmbedding,
    MePhoto,
    MeResponse,
    MeUpdateRequest,
    OkResponse,
    PhotoCreateRequest,
    PhotoOrderRequest,
)

router = APIRouter(tags=["me"])


async def _get_primary_photo_url(db: AsyncSession, user_id: uuid.UUID) -> str | None:
    result = await db.execute(
        select(UserPhoto.url).where(
            UserPhoto.user_id == user_id,
            UserPhoto.is_primary == True,  # noqa: E712
        )
    )
    return result.scalar_one_or_none()


@router.get("/me", response_model=MeResponse)
async def get_me(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    photos_result = await db.execute(
        select(UserPhoto)
        .where(UserPhoto.user_id == current_user.id)
        .order_by(UserPhoto.sort_order.asc())
    )
    photos = photos_result.scalars().all()

    embedding_result = await db.execute(
        select(UserEmbedding).where(UserEmbedding.user_id == current_user.id)
    )
    embedding = embedding_result.scalar_one_or_none()

    embedding_payload = None
    if embedding:
        embedding_payload = MeEmbedding(
            status="ready",
            model=embedding.model,
            updated_at=embedding.updated_at,
        )
    else:
        embedding_payload = MeEmbedding(status="missing")

    primary_photo_url = await _get_primary_photo_url(db, current_user.id)

    return MeResponse(
        id=str(current_user.id),
        nickname=current_user.nickname,
        profile_image_url=current_user.profile_image_url,
        primary_photo_url=primary_photo_url,
        profile_data=current_user.profile_data or {},
        photos=[
            MePhoto(
                id=str(photo.id),
                url=photo.url,
                sort_order=photo.sort_order,
                is_primary=photo.is_primary,
                created_at=photo.created_at,
            )
            for photo in photos
        ],
        embedding=embedding_payload,
    )


@router.patch("/me", response_model=OkResponse)
async def update_me(
    payload: MeUpdateRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    if payload.nickname is not None:
        current_user.nickname = payload.nickname

    if payload.profile_data is not None:
        current_data = dict(current_user.profile_data or {})
        current_data.update(payload.profile_data)
        current_user.profile_data = current_data

    await db.commit()
    return OkResponse(ok=True)


@router.post("/me/photos", response_model=MePhoto, status_code=201)
async def add_photo(
    payload: PhotoCreateRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(func.max(UserPhoto.sort_order)).where(UserPhoto.user_id == current_user.id)
    )
    max_order = result.scalar_one_or_none() or 0
    new_sort_order = max_order + 10

    if payload.make_primary:
        await db.execute(
            update(UserPhoto)
            .where(UserPhoto.user_id == current_user.id)
            .values(is_primary=False)
        )

    photo = UserPhoto(
        user_id=current_user.id,
        url=payload.url,
        sort_order=new_sort_order,
        is_primary=payload.make_primary,
    )
    db.add(photo)

    try:
        await db.commit()
    except IntegrityError:
        await db.rollback()
        raise HTTPException(
            status_code=409,
            detail={
                "error": {
                    "code": "PHOTO_URL_DUPLICATE",
                    "message": "이미 등록된 이미지 URL입니다.",
                }
            },
        )

    await db.refresh(photo)
    return MePhoto(
        id=str(photo.id),
        url=photo.url,
        sort_order=photo.sort_order,
        is_primary=photo.is_primary,
        created_at=photo.created_at,
    )


@router.patch("/me/photos/order", response_model=OkResponse)
async def update_photo_order(
    payload: PhotoOrderRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    if not payload.orders:
        return OkResponse(ok=True)

    photo_ids = [uuid.UUID(item.id) for item in payload.orders]
    result = await db.execute(
        select(UserPhoto.id)
        .where(UserPhoto.user_id == current_user.id)
        .where(UserPhoto.id.in_(photo_ids))
    )
    existing_ids = {row[0] for row in result.all()}

    if len(existing_ids) != len(photo_ids):
        raise HTTPException(status_code=404, detail="Photo not found")

    for item in payload.orders:
        await db.execute(
            update(UserPhoto)
            .where(UserPhoto.id == uuid.UUID(item.id))
            .where(UserPhoto.user_id == current_user.id)
            .values(sort_order=item.sort_order)
        )

    await db.commit()
    return OkResponse(ok=True)


@router.post("/me/photos/{photo_id}/primary", response_model=dict)
async def set_primary_photo(
    photo_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(UserPhoto).where(
            UserPhoto.id == photo_id,
            UserPhoto.user_id == current_user.id,
        )
    )
    photo = result.scalar_one_or_none()
    if not photo:
        raise HTTPException(status_code=404, detail="Photo not found")

    await db.execute(
        update(UserPhoto)
        .where(UserPhoto.user_id == current_user.id)
        .values(is_primary=False)
    )
    photo.is_primary = True

    await db.commit()
    primary_photo_url = await _get_primary_photo_url(db, current_user.id)

    return {"ok": True, "primary_photo_url": primary_photo_url}


@router.delete("/me/photos/{photo_id}", response_model=OkResponse)
async def delete_photo(
    photo_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        delete(UserPhoto)
        .where(UserPhoto.id == photo_id)
        .where(UserPhoto.user_id == current_user.id)
    )
    if result.rowcount == 0:
        raise HTTPException(status_code=404, detail="Photo not found")

    await db.commit()
    return OkResponse(ok=True)


@router.post("/me/embedding/rebuild", response_model=EmbeddingResponse)
async def rebuild_embedding(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(UserEmbedding).where(UserEmbedding.user_id == current_user.id)
    )
    embedding = result.scalar_one_or_none()

    text_snapshot = json.dumps(current_user.profile_data or {}, ensure_ascii=True, sort_keys=True)
    vector = [0.0] * 1536

    if embedding:
        embedding.model = "text-embedding-3-large"
        embedding.source = "profile_data"
        embedding.vector = vector
        embedding.text_snapshot = text_snapshot
    else:
        embedding = UserEmbedding(
            user_id=current_user.id,
            source="profile_data",
            model="text-embedding-3-large",
            vector=vector,
            text_snapshot=text_snapshot,
        )
        db.add(embedding)

    await db.commit()
    await db.refresh(embedding)

    return EmbeddingResponse(
        ok=True,
        embedding=MeEmbedding(
            status="ready",
            model=embedding.model,
            updated_at=embedding.updated_at or datetime.now(timezone.utc),
        ),
    )
