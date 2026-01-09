"""
DB: user_photos
- id (UUID, PK)
- user_id (UUID, FK -> users.id, NOT NULL)
- url (TEXT, NOT NULL)
- sort_order (INT, NOT NULL)                   # 표시 순서 (작을수록 먼저)
- is_primary (BOOL, NOT NULL, default=false)   # 대표 사진 여부
- created_at (timestamptz, NOT NULL, default=now())
- updated_at (timestamptz, NOT NULL, default=now(), onupdate=now())

Constraints / Indexes
- UNIQUE(user_id, url)                         # 같은 유저가 같은 URL 중복 저장 불가
- INDEX(user_id, sort_order)                   # 갤러리 정렬 조회용
- PARTIAL UNIQUE INDEX (user_id WHERE is_primary = true)
  → 유저당 대표사진 1장만 허용
"""

import uuid

from sqlalchemy import (
    Boolean,
    DateTime,
    ForeignKey,
    Index,
    Integer,
    Text,
    UniqueConstraint,
    func,
    column,
)
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base


class UserPhoto(Base):
    __tablename__ = "user_photos"
    __table_args__ = (
        # 같은 유저가 같은 URL을 중복 저장하지 못하게
        UniqueConstraint("user_id", "url", name="uq_user_photos_user_url"),

        # 유저 갤러리 정렬 조회용
        Index("ix_user_photos_user_sort", "user_id", "sort_order"),

        # 유저당 대표사진 1장만 허용 (Postgres partial unique index)
        Index(
            "ux_user_primary_photo",
            "user_id",
            unique=True,
            postgresql_where=(column("is_primary") == True),  # type: ignore
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
    )

    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
    )

    url: Mapped[str] = mapped_column(
        Text,
        nullable=False,
    )

    sort_order: Mapped[int] = mapped_column(
        Integer,
        nullable=False,
        default=0,
    )

    is_primary: Mapped[bool] = mapped_column(
        Boolean,
        nullable=False,
        default=False,
    )

    created_at: Mapped[DateTime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False,
    )

    updated_at: Mapped[DateTime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False,
    )
