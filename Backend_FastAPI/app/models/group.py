"""
DB: groups
- id (UUID, PK)
- name (VARCHAR(80), NOT NULL, UNIQUE)
- description (VARCHAR(255), NULL)
- created_by (UUID, FK -> users.id, NULL)   # seed group이면 NULL도 가능
- group_profile (JSONB, NOT NULL, default={})  # 그룹 취향/성격 데이터
- is_subgroup (BOOLEAN, NOT NULL, default=false)
- parent_group_id (UUID, FK -> groups.id, NULL)
- subgroup_index (INTEGER, NULL)
- created_at (timestamptz, NOT NULL, default=now())

DB: group_members
- group_id (UUID, PK, FK -> groups.id)
- user_id (UUID, PK, FK -> users.id)
- role (VARCHAR(20), NOT NULL, default="member")
- joined_at (timestamptz, NOT NULL, default=now())
"""

import uuid

from sqlalchemy import String, DateTime, func, ForeignKey, Boolean, Integer
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base


class Group(Base):
    __tablename__ = "groups"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)

    name: Mapped[str] = mapped_column(String(80), nullable=False, unique=True)
    description: Mapped[str | None] = mapped_column(String(255), nullable=True)

    created_by: Mapped[uuid.UUID | None] = mapped_column(
        UUID(as_uuid=True), ForeignKey("users.id"), nullable=True
    )

    group_profile: Mapped[dict] = mapped_column(JSONB, nullable=False, default=dict)
    is_subgroup: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    parent_group_id: Mapped[uuid.UUID | None] = mapped_column(
        UUID(as_uuid=True), ForeignKey("groups.id"), nullable=True
    )
    subgroup_index: Mapped[int | None] = mapped_column(Integer, nullable=True)

    created_at: Mapped[DateTime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )


class GroupMember(Base):
    __tablename__ = "group_members"

    group_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("groups.id"), primary_key=True
    )
    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("users.id"), primary_key=True
    )

    role: Mapped[str] = mapped_column(String(20), nullable=False, default="member")
    joined_at: Mapped[DateTime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )
