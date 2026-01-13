import asyncio
from urllib.parse import urlparse

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import sessionmaker

from app.core.config import settings
from app.models.group import Group
from app.models.message import GroupMessage
from app.models.photo import UserPhoto
from app.models.user import User


def _normalize_upload_url(value: str | None) -> str | None:
    if not value:
        return value
    parsed = urlparse(value)
    if parsed.scheme and parsed.netloc:
        return parsed.path if parsed.path.startswith("/uploads/") else value
    if value.startswith("/uploads/"):
        return value
    if value.startswith("uploads/"):
        return f"/{value}"
    return value


async def _run_migration() -> None:
    if not settings.DATABASE_URL:
        raise RuntimeError("DATABASE_URL is not configured")

    engine = create_async_engine(settings.DATABASE_URL, echo=False, pool_pre_ping=True)
    session_factory = sessionmaker(bind=engine, class_=AsyncSession, expire_on_commit=False)

    updated_users = 0
    updated_photos = 0
    updated_groups = 0
    updated_messages = 0

    async with session_factory() as session:
        result = await session.execute(select(User))
        for user in result.scalars():
            new_url = _normalize_upload_url(user.profile_image_url)
            if new_url != user.profile_image_url:
                user.profile_image_url = new_url
                updated_users += 1

        result = await session.execute(select(UserPhoto))
        for photo in result.scalars():
            new_url = _normalize_upload_url(photo.url)
            if new_url != photo.url:
                photo.url = new_url or ""
                updated_photos += 1

        result = await session.execute(select(Group))
        for group in result.scalars():
            profile = dict(group.group_profile or {})
            current = profile.get("image_url")
            new_url = _normalize_upload_url(current)
            if new_url != current:
                profile["image_url"] = new_url or ""
                group.group_profile = profile
                updated_groups += 1

        result = await session.execute(select(GroupMessage))
        for message in result.scalars():
            content = dict(message.content or {})
            current = content.get("image_url")
            new_url = _normalize_upload_url(current)
            if new_url != current:
                content["image_url"] = new_url
                message.content = content
                updated_messages += 1

        await session.commit()

    await engine.dispose()

    print(
        "Migration completed: "
        f"users={updated_users}, "
        f"user_photos={updated_photos}, "
        f"groups={updated_groups}, "
        f"group_messages={updated_messages}"
    )


if __name__ == "__main__":
    asyncio.run(_run_migration())
