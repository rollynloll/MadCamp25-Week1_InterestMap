from dataclasses import dataclass
from datetime import datetime, timezone
import logging
import uuid

from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.image_caption import ImageCaption
from app.models.photo import UserPhoto
from app.models.user import User


@dataclass
class EmbeddingState:
    embedding: list[float]
    updated_at: datetime | None


async def upsert_image_caption(
    db: AsyncSession,
    image_id: uuid.UUID,
    caption_raw_en: str,
    caption_ko: str,
    model_name: str,
    model_version: str | None,
) -> None:
    existing = await db.get(ImageCaption, image_id)
    if existing:
        existing.caption_raw_en = caption_raw_en
        existing.caption_ko = caption_ko
        existing.model_name = model_name
        existing.model_version = model_version
        return

    db.add(
        ImageCaption(
            image_id=image_id,
            caption_raw_en=caption_raw_en,
            caption_ko=caption_ko,
            model_name=model_name,
            model_version=model_version,
        )
    )


async def get_recent_captions(
    db: AsyncSession,
    user_id: uuid.UUID,
    limit: int | None = None,
) -> list[str]:
    query = (
        select(ImageCaption.caption_ko)
        .join(UserPhoto, ImageCaption.image_id == UserPhoto.id)
        .where(UserPhoto.user_id == user_id)
        .order_by(ImageCaption.created_at.desc())
    )
    if limit is not None:
        query = query.limit(limit)
    result = await db.execute(query)
    return [row[0] for row in result.all()]


async def get_active_embedding(
    db: AsyncSession,
    user_id: uuid.UUID,
) -> EmbeddingState | None:
    try:
        result = await db.execute(
            select(User.embedding, User.embedding_updated_at)
            .where(User.id == user_id)
            .limit(1)
        )
        row = result.one_or_none()
        if not row or row[0] is None:
            return None
        return EmbeddingState(
            embedding=list(row[0]),
            updated_at=row[1],
        )
    except Exception as exc:
        logging.getLogger("uvicorn.error").warning(
            "Failed to load active embedding for user_id=%s: %s",
            user_id,
            exc,
        )
        return None


async def deactivate_embeddings(db: AsyncSession, user_id: uuid.UUID) -> None:
    await db.execute(
        update(User)
        .where(User.id == user_id)
        .values(
            embedding=None,
            embedding_updated_at=None,
        )
    )


async def create_embedding(
    db: AsyncSession,
    user_id: uuid.UUID,
    embedding: list[float],
) -> EmbeddingState:
    now = datetime.now(timezone.utc)
    await db.execute(
        update(User)
        .where(User.id == user_id)
        .values(
            embedding=embedding,
            embedding_updated_at=now,
        )
    )
    return EmbeddingState(
        embedding=embedding,
        updated_at=now,
    )
