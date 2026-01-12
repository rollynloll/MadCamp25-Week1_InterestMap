import logging
from pathlib import Path
from threading import Lock
from typing import Any

_MODEL_NAME = "blip-base"
_MODEL_VERSION = "salesforce/blip-image-captioning-base"
_CACHE_DIR = Path.home() / ".cache" / "huggingface"

_processor: Any | None = None
_model: Any | None = None
_LOAD_LOCK = Lock()


def is_blip_ready() -> bool:
    return _processor is not None and _model is not None


def _load_blip() -> bool:
    global _processor, _model
    if _processor is not None and _model is not None:
        return True
    with _LOAD_LOCK:
        if _processor is not None and _model is not None:
            return True
        try:
            from transformers import BlipForConditionalGeneration, BlipProcessor  # type: ignore
        except Exception as exc:
            logging.getLogger("uvicorn.error").warning("BLIP not available: %s", exc)
            return False

        _CACHE_DIR.mkdir(parents=True, exist_ok=True)
        cache_dir = str(_CACHE_DIR)
        try:
            _processor = BlipProcessor.from_pretrained(
                _MODEL_VERSION,
                cache_dir=cache_dir,
                use_fast=False,
            )
            _model = BlipForConditionalGeneration.from_pretrained(
                _MODEL_VERSION,
                cache_dir=cache_dir,
                use_safetensors=True,
            )
        except Exception as exc:
            logging.getLogger("uvicorn.error").warning("BLIP load failed: %s", exc)
            try:
                _processor = BlipProcessor.from_pretrained(
                    _MODEL_VERSION,
                    cache_dir=cache_dir,
                    local_files_only=True,
                    use_fast=False,
                )
                _model = BlipForConditionalGeneration.from_pretrained(
                    _MODEL_VERSION,
                    cache_dir=cache_dir,
                    local_files_only=True,
                    use_safetensors=True,
                )
            except Exception as exc2:
                logging.getLogger("uvicorn.error").warning(
                    "BLIP load failed (cache): %s",
                    exc2,
                )
                _processor = None
                _model = None
                return False
        return True


def caption_image(image_path: str) -> tuple[str, str, str]:
    if not _load_blip():
        fallback = f"an uploaded image ({Path(image_path).name})"
        return fallback, _MODEL_NAME, _MODEL_VERSION

    from PIL import Image  # type: ignore
    import torch  # type: ignore

    image = Image.open(image_path).convert("RGB")
    inputs = _processor(image, return_tensors="pt")
    with torch.no_grad():
        output = _model.generate(**inputs, max_new_tokens=32)
    caption = _processor.decode(output[0], skip_special_tokens=True)
    return caption.strip(), _MODEL_NAME, _MODEL_VERSION
