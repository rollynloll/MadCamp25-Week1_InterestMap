from urllib.parse import urlencode

from fastapi import APIRouter, Header, HTTPException
from fastapi.responses import RedirectResponse

from app.auth.kakao import build_kakao_login_url, exchange_code_for_token, fetch_kakao_user
from app.core.config import settings
from app.core.security import create_access_token

router = APIRouter(prefix="/auth", tags=["auth"])


@router.get("/kakao")
async def kakao_login():
    """
    (웹 리다이렉트 방식)
    /auth/kakao 접속 → 카카오 로그인 페이지로 리다이렉트
    """
    return RedirectResponse(url=build_kakao_login_url())


@router.get("/kakao/callback")
async def kakao_callback(code: str | None = None, error: str | None = None):
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

    kakao_id = str(kakao_user["id"])
    nickname = (
        kakao_user.get("kakao_account", {})
        .get("profile", {})
        .get("nickname")
    )
    if not nickname:
        raise HTTPException(status_code=400, detail="Kakao nickname missing")

    # ✅ DB upsert는 여기 끼워 넣으면 됨 (지금은 MVP로 생략)
    subject = f"kakao:{kakao_id}"
    jwt_token = create_access_token(subject=subject)

    # MVP: query로 프론트에 전달(실서비스는 쿠키 권장)
    qs = urlencode({"token": jwt_token, "nickname": nickname, "provider": "kakao"})
    return RedirectResponse(url=f"{settings.FRONTEND_REDIRECT_URL}?{qs}")


@router.post("/kakao/app")
async def kakao_app_login(authorization: str = Header(...)):
    """
    (앱/SKD 방식 - 옵션)
    앱에서 카카오 SDK로 로그인 후 access_token을 받아서 백엔드에 전달하면,
    백엔드가 /v2/user/me로 검증하고 우리 JWT 발급.
    """
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Missing Bearer token")
    kakao_access_token = authorization.split(" ", 1)[1].strip()

    kakao_user = await fetch_kakao_user(kakao_access_token)

    kakao_id = str(kakao_user["id"])
    nickname = (
        kakao_user.get("kakao_account", {})
        .get("profile", {})
        .get("nickname")
    )
    if not nickname:
        raise HTTPException(status_code=400, detail="Kakao nickname missing")

    subject = f"kakao:{kakao_id}"
    jwt_token = create_access_token(subject=subject)

    return {
        "token": jwt_token,
        "provider": "kakao",
        "nickname": nickname,
    }
