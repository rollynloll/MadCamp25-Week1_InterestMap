from uuid import UUID

from fastapi import Depends, Header, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import decode_access_token
from app.db.session import get_db
from app.models.user import User


def _parse_subject(subject: str) -> tuple[str, str] | None:
    if ":" not in subject:
        return None
    provider, provider_user_id = subject.split(":", 1)
    if not provider or not provider_user_id:
        return None
    return provider, provider_user_id


async def get_current_user(
    authorization: str | None = Header(default=None),
    db: AsyncSession = Depends(get_db),
) -> User:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Missing Bearer token")

    token = authorization.split(" ", 1)[1].strip()
    try:
        payload = decode_access_token(token)
    except ValueError:
        raise HTTPException(status_code=401, detail="Invalid token")

    subject = payload.get("sub")
    if not subject:
        raise HTTPException(status_code=401, detail="Invalid token")

    provider_subject = _parse_subject(subject)
    if provider_subject:
        provider, provider_user_id = provider_subject
        result = await db.execute(
            select(User).where(
                User.provider == provider,
                User.provider_user_id == provider_user_id,
            )
        )
        user = result.scalar_one_or_none()
    else:
        try:
            user_id = UUID(subject)
        except ValueError as exc:
            raise HTTPException(status_code=401, detail="Invalid token") from exc
        result = await db.execute(select(User).where(User.id == user_id))
        user = result.scalar_one_or_none()

    if not user:
        raise HTTPException(status_code=401, detail="User not found")

    return user
