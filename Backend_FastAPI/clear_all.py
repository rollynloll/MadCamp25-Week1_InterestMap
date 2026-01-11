from __future__ import annotations

import asyncio
import shutil
from pathlib import Path


ROOT = Path(__file__).resolve().parent
UPLOADS_DIR = ROOT / "uploads"
EMBEDDINGS_DIR = ROOT / "user_embeddings"


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
        key = key.strip()
        value = value.strip().strip('"').strip("'")
        env[key] = value
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


async def truncate_db(dsn: str) -> None:
    import asyncpg

    conn = await asyncpg.connect(dsn)
    try:
        rows = await conn.fetch(
            """
            SELECT tablename
            FROM pg_tables
            WHERE schemaname = 'public'
            """
        )
        tables = [row["tablename"] for row in rows]
        if not tables:
            print("No tables to truncate.")
            return
        table_list = ", ".join(f'"public"."{table}"' for table in tables)
        await conn.execute(f"TRUNCATE TABLE {table_list} RESTART IDENTITY CASCADE;")
        print(f"Truncated {len(tables)} tables.")
    finally:
        await conn.close()


def clear_dir_contents(path: Path) -> None:
    if not path.exists():
        print(f"Missing: {path}")
        return
    for item in path.iterdir():
        if item.is_dir():
            shutil.rmtree(item)
        else:
            item.unlink()
    print(f"Cleared: {path}")


async def main() -> None:
    env_path = ROOT / ".env"
    if not env_path.exists():
        raise SystemExit(f".env not found at {env_path}")
    env = load_env(env_path)
    dsn = build_dsn(env)
    if not dsn:
        raise SystemExit("Missing DB config in .env")

    await truncate_db(dsn)
    clear_dir_contents(UPLOADS_DIR)
    clear_dir_contents(EMBEDDINGS_DIR)


if __name__ == "__main__":
    asyncio.run(main())
