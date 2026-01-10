import hashlib
import random

import httpx

from app.core.config import settings

MODEL_NAME = settings.OPENAI_EMBED_MODEL or "text-embedding-3-small"
MODEL_VERSION = settings.OPENAI_EMBED_MODEL_VERSION
EMBEDDING_DIM = 1024


def _fallback_embedding(text: str) -> list[float]:
    digest = hashlib.sha256(text.encode("utf-8")).digest()
    seed = int.from_bytes(digest[:8], "big")
    rng = random.Random(seed)
    return [rng.uniform(-1, 1) for _ in range(EMBEDDING_DIM)]


async def embed_text(text: str) -> tuple[list[float], str, str | None]:
    api_key = settings.OPENAI_API_KEY
    if not api_key:
        return _fallback_embedding(text), MODEL_NAME, "mock"

    payload: dict[str, object] = {"model": MODEL_NAME, "input": text}
    if MODEL_NAME.startswith("text-embedding-3-"):
        payload["dimensions"] = EMBEDDING_DIM
    headers = {"Authorization": f"Bearer {api_key}"}
    async with httpx.AsyncClient(timeout=30) as client:
        response = await client.post(
            "https://api.openai.com/v1/embeddings",
            json=payload,
            headers=headers,
        )
        response.raise_for_status()
        data = response.json()

    embedding = data["data"][0]["embedding"]
    return embedding, MODEL_NAME, MODEL_VERSION
