"""
DB: notion_group_members
- group_id (UUID, PK, FK -> groups.id)
- notion_user_id (UUID, PK, FK -> notion_users.id)
- role (VARCHAR(20), NOT NULL, default="member")
- joined_at (timestamptz, NOT NULL, default=now())
"""

import uuid

from sqlalchemy import DateTime, ForeignKey, String, func
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base


class NotionGroupMember(Base):
    __tablename__ = "notion_group_members"

    group_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("groups.id"), primary_key=True
    )
    notion_user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("notion_users.id"), primary_key=True
    )

    role: Mapped[str] = mapped_column(String(20), nullable=False, default="member")
    joined_at: Mapped[DateTime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )
