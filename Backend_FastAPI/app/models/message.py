"""
DB: group_messages
- id (UUID, PK)
- group_id (UUID, FK -> groups.id)
- sender_id (UUID, FK -> users.id, NULLABLE)
- notion_user_id (UUID, FK -> notion_users.id, NULLABLE)
- content (JSONB, NOT NULL)
- created_at (timestamptz, NOT NULL, default=now())
"""

import uuid

from sqlalchemy import DateTime, ForeignKey, Index, func
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base


class GroupMessage(Base):
    __tablename__ = "group_messages"
    __table_args__ = (
        Index("ix_group_messages_group_created", "group_id", "created_at"),
    )

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)

    group_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("groups.id"), nullable=False
    )
    sender_id: Mapped[uuid.UUID | None] = mapped_column(
        UUID(as_uuid=True), ForeignKey("users.id"), nullable=True
    )
    notion_user_id: Mapped[uuid.UUID | None] = mapped_column(
        UUID(as_uuid=True), ForeignKey("notion_users.id"), nullable=True
    )

    content: Mapped[dict] = mapped_column(JSONB, nullable=False, default=dict)

    created_at: Mapped[DateTime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )
