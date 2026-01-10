from datetime import datetime, timezone
import uuid

from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.embedding import UserEmbedding
from app.models.image_caption import ImageCaption
from app.models.photo import UserPhoto


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
    limit: int = 5,
) -> list[str]:
    result = await db.execute(
        select(ImageCaption.caption_ko)
        .join(UserPhoto, ImageCaption.image_id == UserPhoto.id)
        .where(UserPhoto.user_id == user_id)
        .order_by(ImageCaption.created_at.desc())
        .limit(limit)
    )
    return [row[0] for row in result.all()]


async def get_active_embedding(
    db: AsyncSession,
    user_id: uuid.UUID,
) -> UserEmbedding | None:
    result = await db.execute(
        select(UserEmbedding)
        .where(UserEmbedding.user_id == user_id, UserEmbedding.is_active == True)  # noqa: E712
        .limit(1)
    )
    return result.scalar_one_or_none()


async def deactivate_embeddings(db: AsyncSession, user_id: uuid.UUID) -> None:
    await db.execute(
        update(UserEmbedding)
        .where(UserEmbedding.user_id == user_id, UserEmbedding.is_active == True)  # noqa: E712
        .values(is_active=False, updated_at=datetime.now(timezone.utc))
    )


async def create_embedding(
    db: AsyncSession,
    user_id: uuid.UUID,
    embedding_type: str,
    model_name: str,
    model_version: str | None,
    embedding: list[float],
    source_hash: str,
) -> UserEmbedding:
    entity = UserEmbedding(
        user_id=user_id,
        embedding_type=embedding_type,
        model_name=model_name,
        model_version=model_version,
        embedding=embedding,
        source_hash=source_hash,
        is_active=True,
    )
    db.add(entity)
    return entity
