from __future__ import annotations

import json
import logging
import re
from datetime import datetime, timezone
from pathlib import Path


_LOG_DIR = Path(__file__).resolve().parents[3] / "user_embeddings"


def _safe_filename(name: str | None, fallback: str) -> str:
    if not name:
        return fallback
    cleaned = re.sub(r"[^A-Za-z0-9_-]+", "_", name).strip("_")
    return cleaned or fallback


def log_embedding_io(
    user_name: str | None,
    user_id: str,
    input_text: str,
    image_captions: list[str] | None,
    image_tags: list[str] | None,
    embedding: list[float],
    model_name: str | None,
    model_version: str | None,
) -> None:
    logger = logging.getLogger("uvicorn.error")
    _LOG_DIR.mkdir(parents=True, exist_ok=True)
    safe_name = _safe_filename(user_name, user_id)
    payload = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "user_id": user_id,
        "user_name": user_name,
        "input_text": input_text,
        "input_text_lines": input_text.splitlines(),
        "image_captions": image_captions or [],
        "image_tags": image_tags or [],
        "embedding": embedding,
        "model_name": model_name,
        "model_version": model_version,
    }
    log_path = _LOG_DIR / f"{safe_name}_embeddings.json"
    try:
        if log_path.exists():
            existing = json.loads(log_path.read_text(encoding="utf-8") or "[]")
            if not isinstance(existing, list):
                existing = []
        else:
            existing = []
        existing.append(payload)
        log_path.write_text(
            json.dumps(existing, ensure_ascii=False, indent=2),
            encoding="utf-8",
        )
    except Exception as exc:
        logger.warning(
            "Embedding log write failed user_id=%s path=%s error=%s",
            user_id,
            log_path,
            exc,
        )
        return
    logger.info("Embedding log written user_id=%s path=%s", user_id, log_path)
