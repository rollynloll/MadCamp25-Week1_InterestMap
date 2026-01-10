"""
DB: image_captions
- image_id (UUID, PK, FK -> user_photos.id)
- caption_raw_en (TEXT, NOT NULL)
- caption_ko (TEXT, NOT NULL)
- model_name (VARCHAR(80), NOT NULL, default="blip-base")
- model_version (VARCHAR(40), NULL)
- created_at (timestamptz, NOT NULL, default=now())
"""

import uuid

from sqlalchemy import DateTime, ForeignKey, String, Text, func
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base


class ImageCaption(Base):
    __tablename__ = "image_captions"

    image_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("user_photos.id", ondelete="CASCADE"),
        primary_key=True,
    )

    caption_raw_en: Mapped[str] = mapped_column(Text, nullable=False)
    caption_ko: Mapped[str] = mapped_column(Text, nullable=False)

    model_name: Mapped[str] = mapped_column(String(80), nullable=False, default="blip-base")
    model_version: Mapped[str | None] = mapped_column(String(40), nullable=True)

    created_at: Mapped[DateTime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )
