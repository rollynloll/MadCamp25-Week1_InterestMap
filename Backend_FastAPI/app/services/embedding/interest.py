import json
import logging

import httpx

from app.core.config import settings


async def infer_interest_tags(caption_ko: str) -> list[str]:
    if not caption_ko:
        return []

    api_key = settings.OPENAI_API_KEY
    if not api_key:
        return []

    model = settings.OPENAI_TRANSLATION_MODEL or "gpt-4o-mini"
    payload = {
        "model": model,
        "messages": [
            {
                "role": "system",
                "content": (
                    "Infer 0-3 likely hobbies/interests from the image caption. "
                    "Prefer higher-level hobby tags over literal objects. "
                    "Return JSON only: {\"tags\": [\"...\"]}. Use short Korean nouns. "
                    "If uncertain, return an empty list."
                ),
            },
            {"role": "user", "content": caption_ko},
        ],
        "temperature": 0.2,
        "response_format": {"type": "json_object"},
    }

    headers = {"Authorization": f"Bearer {api_key}"}
    try:
        async with httpx.AsyncClient(timeout=20) as client:
            response = await client.post(
                "https://api.openai.com/v1/chat/completions",
                json=payload,
                headers=headers,
            )
            response.raise_for_status()
            data = response.json()
    except Exception as exc:
        logging.getLogger("uvicorn.error").warning("Interest inference failed: %s", exc)
        return []

    content = data["choices"][0]["message"]["content"].strip()
    try:
        parsed = json.loads(content)
        tags = parsed.get("tags", []) if isinstance(parsed, dict) else parsed
    except json.JSONDecodeError:
        tags = [tag.strip() for tag in content.split(",") if tag.strip()]

    cleaned: list[str] = []
    seen: set[str] = set()
    for tag in tags:
        if not isinstance(tag, str):
            continue
        value = tag.strip()
        if not value or value in seen:
            continue
        seen.add(value)
        cleaned.append(value)

    return cleaned[:3]
