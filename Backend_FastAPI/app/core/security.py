from datetime import datetime, timedelta, timezone
from jose import jwt, JWTError

from app.core.config import settings


def create_access_token(subject: str, expires_minutes: int | None = None) -> str:
    minutes = expires_minutes or settings.JWT_EXPIRE_MINUTES
    now = datetime.now(timezone.utc)

    payload = {
        "sub": subject,
        "iat": int(now.timestamp()),
        "exp": int((now + timedelta(minutes=minutes)).timestamp()),
    }
    return jwt.encode(payload, settings.JWT_SECRET, algorithm=settings.JWT_ALG)


def decode_access_token(token: str) -> dict:
    try:
        return jwt.decode(token, settings.JWT_SECRET, algorithms=[settings.JWT_ALG])
    except JWTError as exc:
        raise ValueError("Invalid token") from exc
