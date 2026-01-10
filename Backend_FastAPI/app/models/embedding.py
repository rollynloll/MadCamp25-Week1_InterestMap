"""
DB: user_embeddings
- id (UUID, PK)
- user_id (UUID, FK -> users.id, NOT NULL)
- embedding_type (VARCHAR(32), NOT NULL)        # profile_v1 등
- model_name (VARCHAR(80), NOT NULL)            # text-embedding-3-small
- model_version (VARCHAR(40), NULL)
- embedding (vector(1024), NOT NULL)
- source_hash (CHAR(64), NOT NULL)              # SHA-256 of final_text
- is_active (BOOL, NOT NULL, default=true)
- created_at (timestamptz, NOT NULL, default=now())
- updated_at (timestamptz, NOT NULL, default=now(), onupdate=now())
"""

import uuid

from sqlalchemy import Boolean, DateTime, ForeignKey, Index, String, func
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from pgvector.sqlalchemy import Vector
from app.db.base import Base


class UserEmbedding(Base):
    __tablename__ = "user_embeddings"
    __table_args__ = (
        Index("ix_user_embeddings_user_active", "user_id", "is_active"),
        Index("ix_user_embeddings_hash", "source_hash"),
    )

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)

    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("users.id"), nullable=False
    )

    embedding_type: Mapped[str] = mapped_column(String(32), nullable=False, default="profile_v1")
    model_name: Mapped[str] = mapped_column(String(80), nullable=False)
    model_version: Mapped[str | None] = mapped_column(String(40), nullable=True)

    # NOTE: 임베딩 차원은 사용하는 모델에 맞게 변경
    embedding: Mapped[list[float]] = mapped_column(Vector(1024), nullable=False)

    source_hash: Mapped[str] = mapped_column(String(64), nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, nullable=False, default=True)

    created_at: Mapped[DateTime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    updated_at: Mapped[DateTime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False
    )
