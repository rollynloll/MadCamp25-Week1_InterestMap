import hashlib
from typing import Iterable


def _normalize_items(items: Iterable[str]) -> list[str]:
    seen = set()
    normalized = []
    for item in items:
        value = item.strip()
        if not value or value in seen:
            continue
        seen.add(value)
        normalized.append(value)
    return normalized


def build_final_text(
    selected_tags: list[str],
    user_description: str | None,
    image_captions: list[str],
) -> str:
    tags = ", ".join(_normalize_items(selected_tags)) if selected_tags else ""
    description = (user_description or "").strip()
    captions = "\n".join(_normalize_items(image_captions)) if image_captions else ""

    return (
        "[SelectedTags]\n"
        f"{tags}\n"
        "[UserDescription]\n"
        f"{description}\n"
        "[ImageCaptions]\n"
        f"{captions}\n"
    )


def compute_source_hash(final_text: str) -> str:
    return hashlib.sha256(final_text.encode("utf-8")).hexdigest()
