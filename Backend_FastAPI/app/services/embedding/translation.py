import httpx

from app.core.config import settings


async def translate_to_korean(text: str) -> str:
    if not text:
        return text

    api_key = settings.OPENAI_API_KEY
    if not api_key:
        return text

    model = settings.OPENAI_TRANSLATION_MODEL or "gpt-4o-mini"
    payload = {
        "model": model,
        "messages": [
            {"role": "system", "content": "Translate the text to Korean. Return only the translation."},
            {"role": "user", "content": text},
        ],
        "temperature": 0,
    }

    headers = {"Authorization": f"Bearer {api_key}"}
    async with httpx.AsyncClient(timeout=30) as client:
        response = await client.post(
            "https://api.openai.com/v1/chat/completions",
            json=payload,
            headers=headers,
        )
        response.raise_for_status()
        data = response.json()
    return data["choices"][0]["message"]["content"].strip()
