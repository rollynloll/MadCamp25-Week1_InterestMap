from __future__ import annotations

import asyncio
from pathlib import Path


ROOT = Path(__file__).resolve().parent


def load_env(path: Path) -> dict[str, str]:
    env: dict[str, str] = {}
    for raw_line in path.read_text().splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        if line.startswith("export "):
            line = line[len("export "):]
        if "=" not in line:
            continue
        key, value = line.split("=", 1)
        env[key.strip()] = value.strip().strip('"').strip("'")
    return env


def build_dsn(env: dict[str, str]) -> str | None:
    dsn = env.get("DATABASE_URL")
    if dsn:
        return dsn.replace("postgresql+asyncpg://", "postgresql://", 1)

    required = ("POSTGRES_DB", "POSTGRES_USER", "POSTGRES_PASSWORD")
    if not all(env.get(key) for key in required):
        return None
    host = env.get("POSTGRES_HOST", "localhost")
    port = env.get("POSTGRES_PORT", "5432")
    user = env["POSTGRES_USER"]
    password = env["POSTGRES_PASSWORD"]
    db = env["POSTGRES_DB"]
    return f"postgresql://{user}:{password}@{host}:{port}/{db}"


async def reset_schema(dsn: str, db_user: str | None) -> None:
    import asyncpg

    conn = await asyncpg.connect(dsn)
    try:
        await conn.execute("DROP SCHEMA public CASCADE;")
        await conn.execute("CREATE SCHEMA public;")
        if db_user:
            await conn.execute(f'GRANT ALL ON SCHEMA public TO "{db_user}";')
        await conn.execute("GRANT ALL ON SCHEMA public TO public;")
    finally:
        await conn.close()


async def recreate_tables() -> None:
    from app.db.base import Base
    from app.db.session import engine
    import app.models  # noqa: F401

    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)


async def main() -> None:
    env_path = ROOT / ".env"
    if not env_path.exists():
        raise SystemExit(f".env not found at {env_path}")
    env = load_env(env_path)
    dsn = build_dsn(env)
    if not dsn:
        raise SystemExit("Missing DB config in .env")

    await reset_schema(dsn, env.get("POSTGRES_USER"))
    await recreate_tables()
    print("DB schema reset and tables recreated.")


if __name__ == "__main__":
    asyncio.run(main())
