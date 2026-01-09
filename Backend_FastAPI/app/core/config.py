from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # Kakao
    KAKAO_REST_API_KEY: str
    KAKAO_REDIRECT_URI: str

    # Our JWT
    JWT_SECRET: str
    JWT_ALG: str = "HS256"
    JWT_EXPIRE_MINUTES: int = 60

    # Where to redirect after login (web)
    FRONTEND_REDIRECT_URL: str = "http://localhost:5173/auth/callback"

    class Config:
        env_file = ".env"


settings = Settings()
