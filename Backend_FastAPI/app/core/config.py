from urllib.parse import urlparse

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    # Database
    DATABASE_URL: str | None = None
    POSTGRES_DB: str | None = None
    POSTGRES_USER: str | None = None
    POSTGRES_PASSWORD: str | None = None
    POSTGRES_HOST: str = "localhost"
    POSTGRES_PORT: int = 5432

    # Kakao
    KAKAO_REST_API_KEY: str
    KAKAO_REDIRECT_URI: str

    # Our JWT
    JWT_SECRET: str
    JWT_ALG: str = "HS256"
    JWT_EXPIRE_MINUTES: int = 60

    # Where to redirect after login (web)
    FRONTEND_REDIRECT_URL: str = "http://localhost:5173/auth/callback"

    # OpenAI
    OPENAI_API_KEY: str | None = None
    OPENAI_TRANSLATION_MODEL: str = "gpt-4o-mini"
    OPENAI_EMBED_MODEL: str = "text-embedding-3-small"
    OPENAI_EMBED_MODEL_VERSION: str | None = None

    def _build_database_url(self) -> str | None:
        if not (self.POSTGRES_DB and self.POSTGRES_USER and self.POSTGRES_PASSWORD):
            return None
        return (
            "postgresql+asyncpg://"
            f"{self.POSTGRES_USER}:{self.POSTGRES_PASSWORD}"
            f"@{self.POSTGRES_HOST}:{self.POSTGRES_PORT}/{self.POSTGRES_DB}"
        )

    def model_post_init(self, __context: object) -> None:
        if not self.DATABASE_URL:
            self.DATABASE_URL = self._build_database_url()
            return

        parsed = urlparse(self.DATABASE_URL)
        if parsed.hostname == "db" and (parsed.password or "") == "your_password":
            fallback = self._build_database_url()
            if fallback:
                self.DATABASE_URL = fallback

settings = Settings()
