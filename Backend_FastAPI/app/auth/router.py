from urllib.parse import urlencode

import httpx
from fastapi import APIRouter, Depends, Header, HTTPException
from fastapi.responses import JSONResponse, RedirectResponse
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.kakao import build_kakao_login_url, exchange_code_for_token, fetch_kakao_user
from app.core.config import settings
from app.core.security import create_access_token
from app.db.session import get_db
from app.models.photo import UserPhoto
from app.models.user import User
from app.schemas import AuthResponse, AuthUser, KakaoAuthRequest

router = APIRouter(prefix="/auth", tags=["auth"])


def _error_json(code: str, message: str, status_code: int, details: dict | None = None):
    payload = {"error": {"code": code, "message": message}}
    if details is not None:
        payload["error"]["details"] = details
    return JSONResponse(status_code=status_code, content=payload)


async def _get_primary_photo_url(db: AsyncSession, user_id) -> str | None:
    result = await db.execute(
        select(UserPhoto.url).where(
            UserPhoto.user_id == user_id,
            UserPhoto.is_primary == True,  # noqa: E712
        )
    )
    return result.scalar_one_or_none()


async def _upsert_kakao_user(db: AsyncSession, kakao_user: dict) -> User:
    kakao_id = str(kakao_user.get("id", ""))
    if not kakao_id:
        raise HTTPException(status_code=400, detail="Kakao user id missing")

    # Kakao user payload may vary by consent/scopes.
    # Prefer kakao_account.profile, but also support legacy `properties`.
    profile = kakao_user.get("kakao_account", {}).get("profile", {}) or {}
    properties = kakao_user.get("properties", {}) or {}

    nickname = profile.get("nickname") or properties.get("nickname")
    profile_image_url = (
        profile.get("profile_image_url")
        or profile.get("thumbnail_image_url")
        or properties.get("profile_image")
        or properties.get("profile_image_url")
        or properties.get("thumbnail_image")
    )

    # If nickname is missing (e.g., user didn't grant profile scope), generate a stable fallback.
    if not nickname:
        nickname = f"kakao_{kakao_id[-6:]}"

    result = await db.execute(
        select(User).where(
            User.provider == "kakao",
            User.provider_user_id == kakao_id,
        )
    )
    user = result.scalar_one_or_none()
    if user:
        user.nickname = nickname
        user.profile_image_url = profile_image_url
    else:
        user = User(
            provider="kakao",
            provider_user_id=kakao_id,
            nickname=nickname,
            profile_image_url=profile_image_url,
            profile_data={},
        )
        db.add(user)

    await db.commit()
    await db.refresh(user)
    return user


async def _login_with_kakao_token(
    kakao_access_token: str,
    db: AsyncSession,
):
    try:
        kakao_user = await fetch_kakao_user(kakao_access_token)
    except httpx.HTTPStatusError as exc:
        if exc.response.status_code in {401, 403}:
            return _error_json(
                "KAKAO_TOKEN_INVALID",
                "카카오 토큰이 유효하지 않습니다.",
                status_code=401,
            )
        return _error_json("KAKAO_API_ERROR", "Kakao API error", status_code=502)
    except httpx.RequestError:
        return _error_json("KAKAO_API_ERROR", "Kakao API error", status_code=502)

    user = await _upsert_kakao_user(db, kakao_user)
    jwt_token = create_access_token(subject=str(user.id))
    primary_photo_url = await _get_primary_photo_url(db, user.id)

    return AuthResponse(
        access_token=jwt_token,
        token_type="bearer",
        user=AuthUser(
            id=str(user.id),
            nickname=user.nickname,
            profile_image_url=user.profile_image_url,
            primary_photo_url=primary_photo_url,
        ),
    )


@router.get("/kakao")
async def kakao_login():
    """
    (웹 리다이렉트 방식)
    /auth/kakao 접속 → 카카오 로그인 페이지로 리다이렉트
    """
    return RedirectResponse(url=build_kakao_login_url())


@router.get("/kakao/callback")
async def kakao_callback(
    code: str | None = None,
    error: str | None = None,
    db: AsyncSession = Depends(get_db),
):
    """
    (웹 리다이렉트 방식)
    카카오가 redirect_uri로 code를 줌 → code로 토큰 교환 → 유저 조회 → 우리 JWT 발급 → 프론트로 리다이렉트
    """
    if error:
        raise HTTPException(status_code=400, detail=f"Kakao error: {error}")
    if not code:
        raise HTTPException(status_code=400, detail="Missing code")

    token_data = await exchange_code_for_token(code)
    access_token = token_data.get("access_token")
    if not access_token:
        raise HTTPException(status_code=400, detail="No access_token from Kakao")

    kakao_user = await fetch_kakao_user(access_token)
    user = await _upsert_kakao_user(db, kakao_user)
    jwt_token = create_access_token(subject=str(user.id))
    primary_photo_url = await _get_primary_photo_url(db, user.id)

    # MVP: query로 프론트에 전달(실서비스는 쿠키 권장)
    qs = urlencode(
        {
            "token": jwt_token,
            "nickname": user.nickname,
            "provider": "kakao",
            "primary_photo_url": primary_photo_url or "",
        }
    )
    return RedirectResponse(url=f"{settings.FRONTEND_REDIRECT_URL}?{qs}")


@router.post("/kakao", response_model=AuthResponse)
async def kakao_token_login(
    payload: KakaoAuthRequest,
    db: AsyncSession = Depends(get_db),
):
    return await _login_with_kakao_token(payload.access_token, db)


@router.post("/kakao/app")
async def kakao_app_login(
    authorization: str = Header(...),
    db: AsyncSession = Depends(get_db),
):
    """
    (앱/SKD 방식 - 옵션)
    앱에서 카카오 SDK로 로그인 후 access_token을 받아서 백엔드에 전달하면,
    백엔드가 /v2/user/me로 검증하고 우리 JWT 발급.
    """
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Missing Bearer token")
    kakao_access_token = authorization.split(" ", 1)[1].strip()

    return await _login_with_kakao_token(kakao_access_token, db)
