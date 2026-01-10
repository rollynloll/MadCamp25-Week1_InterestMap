import httpx

from app.core.config import settings

KAKAO_AUTH_URL = "https://kauth.kakao.com/oauth/authorize"
KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token"
KAKAO_USER_URL = "https://kapi.kakao.com/v2/user/me"


def build_kakao_login_url(state: str | None = None) -> str:
    params = {
        "client_id": settings.KAKAO_REST_API_KEY,
        "redirect_uri": settings.KAKAO_REDIRECT_URI,
        "response_type": "code",
    }
    if state:
        params["state"] = state
    return str(httpx.URL(KAKAO_AUTH_URL, params=params))


async def exchange_code_for_token(code: str) -> dict:
    data = {
        "grant_type": "authorization_code",
        "client_id": settings.KAKAO_REST_API_KEY,
        "redirect_uri": settings.KAKAO_REDIRECT_URI,
        "code": code,
    }
    async with httpx.AsyncClient(timeout=10.0) as client:
        resp = await client.post(KAKAO_TOKEN_URL, data=data)
        resp.raise_for_status()
        return resp.json()


async def fetch_kakao_user(access_token: str) -> dict:
    headers = {"Authorization": f"Bearer {access_token}"}
    async with httpx.AsyncClient(timeout=10.0) as client:
        resp = await client.get(KAKAO_USER_URL, headers=headers)
        resp.raise_for_status()
        return resp.json()
