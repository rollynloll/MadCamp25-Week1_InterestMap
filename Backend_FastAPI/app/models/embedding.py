"""
DB: user_embeddings
- id (UUID, PK)
- user_id (UUID, FK -> users.id, NOT NULL)
- source (VARCHAR(32), NOT NULL, default="profile_data")  # 어떤 입력에서 만든 임베딩인지
- model (VARCHAR(80), NOT NULL)                           # 임베딩 모델명
- vector (vector(1536), NOT NULL)                         # pgvector 타입
- text_snapshot (TEXT, NULL)                              # 임베딩 생성에 쓴 텍스트(디버깅/재현용)
- updated_at (timestamptz, NOT NULL, default=now(), onupdate=now())
"""

import uuid

from sqlalchemy import String, DateTime, func, ForeignKey, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from pgvector.sqlalchemy import Vector
from app.db.base import Base


class UserEmbedding(Base):
    __tablename__ = "user_embeddings"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)

    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("users.id"), nullable=False
    )

    source: Mapped[str] = mapped_column(String(32), nullable=False, default="profile_data")
    model: Mapped[str] = mapped_column(String(80), nullable=False)

    # NOTE: 임베딩 차원은 사용하는 모델에 맞게 변경
    vector: Mapped[list[float]] = mapped_column(Vector(1536), nullable=False)

    text_snapshot: Mapped[str | None] = mapped_column(Text, nullable=True)

    updated_at: Mapped[DateTime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False
    )
