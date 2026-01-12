"""
DB: users
- id (UUID, PK)
- provider (VARCHAR(32), NOT NULL)             # ex) "kakao"
- provider_user_id (VARCHAR(128), NOT NULL)    # kakao user id
- nickname (VARCHAR(64), NULL)
- profile_image_url (VARCHAR(512), NULL)
- profile_data (JSONB, NOT NULL, default={})
- embedding (JSONB, NULL)
- embedding_updated_at (timestamptz, NULL)
- created_at (timestamptz, NOT NULL, default=now())
- updated_at (timestamptz, NOT NULL, default=now(), onupdate=now())

Constraints / Indexes
- UNIQUE(provider, provider_user_id)
"""

import uuid

from sqlalchemy import String, DateTime, func, UniqueConstraint
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base


class User(Base):
    __tablename__ = "users"
    __table_args__ = (
        UniqueConstraint("provider", "provider_user_id", name="uq_users_provider_pid"),
    )

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)

    provider: Mapped[str] = mapped_column(String(32), nullable=False)
    provider_user_id: Mapped[str] = mapped_column(String(128), nullable=False)

    nickname: Mapped[str | None] = mapped_column(String(64), nullable=True)
    profile_image_url: Mapped[str | None] = mapped_column(String(512), nullable=True)

    profile_data: Mapped[dict] = mapped_column(JSONB, nullable=False, default=dict)
    embedding: Mapped[list[float] | None] = mapped_column(JSONB, nullable=True)
    embedding_updated_at: Mapped[DateTime | None] = mapped_column(
        DateTime(timezone=True), nullable=True
    )

    created_at: Mapped[DateTime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )
    updated_at: Mapped[DateTime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False
    )
