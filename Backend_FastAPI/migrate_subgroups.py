import asyncio
import os
import sys
from pathlib import Path

from sqlalchemy import text
from sqlalchemy.ext.asyncio import create_async_engine

BASE_DIR = Path(__file__).resolve().parents[0]
if str(BASE_DIR) not in sys.path:
    sys.path.insert(0, str(BASE_DIR))


def _load_env(env_path: Path) -> None:
    if not env_path.exists():
        return
    for line in env_path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        key = key.strip()
        value = value.strip().strip('"').strip("'")
        os.environ.setdefault(key, value)


_load_env(BASE_DIR / ".env")


async def main() -> None:
    database_url = os.getenv("DATABASE_URL")
    if not database_url:
        raise RuntimeError("DATABASE_URL is not configured")

    engine = create_async_engine(database_url, echo=False, pool_pre_ping=True)
    async with engine.begin() as conn:
        await conn.execute(
            text(
                """
                ALTER TABLE groups
                ADD COLUMN IF NOT EXISTS is_subgroup BOOLEAN NOT NULL DEFAULT FALSE;
                """
            )
        )
        await conn.execute(
            text(
                """
                ALTER TABLE groups
                ADD COLUMN IF NOT EXISTS parent_group_id UUID NULL;
                """
            )
        )
        await conn.execute(
            text(
                """
                ALTER TABLE groups
                ADD COLUMN IF NOT EXISTS subgroup_index INTEGER NULL;
                """
            )
        )
        await conn.execute(
            text(
                """
                CREATE INDEX IF NOT EXISTS ix_groups_parent_subgroup
                ON groups (parent_group_id, subgroup_index);
                """
            )
        )
    await engine.dispose()
    print("Migration completed: groups subgroup columns added.")


if __name__ == "__main__":
    asyncio.run(main())
