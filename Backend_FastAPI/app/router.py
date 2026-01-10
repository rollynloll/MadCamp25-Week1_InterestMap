from datetime import datetime, timezone
from typing import Any, Dict, List, Optional
import hashlib
import random
import uuid

from fastapi import APIRouter, Header, HTTPException, Query, status
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

router = APIRouter()

_users: Dict[str, Dict[str, Any]] = {}
_tokens: Dict[str, str] = {}
_kakao_token_to_user_id: Dict[str, str] = {}
_photos: Dict[str, Dict[str, Any]] = {}
_groups: Dict[str, Dict[str, Any]] = {}
_messages: Dict[str, List[Dict[str, Any]]] = {}


def _now_iso() -> str:
    return datetime.now(timezone.utc).astimezone().isoformat()


def _error(status_code: int, code: str, message: str, details: Optional[Dict[str, Any]] = None):
    payload: Dict[str, Any] = {"error": {"code": code, "message": message}}
    if details is not None:
        payload["error"]["details"] = details
    return JSONResponse(status_code=status_code, content=payload)


def _seed_groups() -> None:
    if _groups:
        return
    _groups["b3b8f5fd-51fd-4d5e-93d7-3fb2f7e8f001"] = {
        "id": "b3b8f5fd-51fd-4d5e-93d7-3fb2f7e8f001",
        "name": "SF Movie Room",
        "description": "People who like SF movies",
        "member_ids": [],
    }
    _groups["5c4a2d1e-6b11-4d4f-a5b1-9f7c2d3e4f55"] = {
        "id": "5c4a2d1e-6b11-4d4f-a5b1-9f7c2d3e4f55",
        "name": "Exhibition Room",
        "description": "Cinematography and aesthetics",
        "member_ids": [],
    }


def _get_current_user(authorization: Optional[str]) -> Dict[str, Any]:
    if not authorization or not authorization.lower().startswith("bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Unauthorized")
    token = authorization.split(" ", 1)[1].strip()
    user_id = _tokens.get(token)
    if not user_id or user_id not in _users:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Unauthorized")
    return _users[user_id]


def _get_optional_user(authorization: Optional[str]) -> Optional[Dict[str, Any]]:
    if not authorization or not authorization.lower().startswith("bearer "):
        return None
    token = authorization.split(" ", 1)[1].strip()
    user_id = _tokens.get(token)
    if not user_id:
        return None
    return _users.get(user_id)


def _user_photos(user_id: str) -> List[Dict[str, Any]]:
    photos = [photo for photo in _photos.values() if photo["user_id"] == user_id]
    return sorted(photos, key=lambda p: p["sort_order"])


def _photo_public(photo: Dict[str, Any]) -> Dict[str, Any]:
    return {
        "id": photo["id"],
        "url": photo["url"],
        "sort_order": photo["sort_order"],
        "is_primary": photo["is_primary"],
        "created_at": photo["created_at"],
    }


def _sync_primary_photo(user_id: str) -> Optional[str]:
    photos = _user_photos(user_id)
    primary = next((photo for photo in photos if photo["is_primary"]), None)
    primary_url = primary["url"] if primary else None
    _users[user_id]["primary_photo_url"] = primary_url
    return primary_url


def _set_primary_photo(user_id: str, photo_id: str) -> Optional[str]:
    for photo in _photos.values():
        if photo["user_id"] == user_id:
            photo["is_primary"] = photo["id"] == photo_id
    return _sync_primary_photo(user_id)


def _stable_pair(seed: str) -> Dict[str, float]:
    digest = hashlib.sha256(seed.encode("utf-8")).hexdigest()
    rng = random.Random(digest)
    return {"x": round(rng.uniform(-1, 1), 2), "y": round(rng.uniform(-1, 1), 2)}


class KakaoAuthRequest(BaseModel):
    access_token: str = Field(..., min_length=1)


class MeUpdateRequest(BaseModel):
    nickname: Optional[str] = None
    profile_data: Optional[Dict[str, Any]] = None


class PhotoCreateRequest(BaseModel):
    url: str = Field(..., min_length=1)
    make_primary: bool = False


class PhotoOrderItem(BaseModel):
    id: str
    sort_order: int


class PhotoOrderRequest(BaseModel):
    orders: List[PhotoOrderItem]


class MessageCreateRequest(BaseModel):
    text: str = Field(..., min_length=1)


@router.post("/auth/kakao")
def auth_kakao(request: KakaoAuthRequest):
    if request.access_token.lower().startswith("invalid"):
        return _error(
            status.HTTP_401_UNAUTHORIZED,
            "KAKAO_TOKEN_INVALID",
            "Invalid Kakao token.",
        )

    user_id = _kakao_token_to_user_id.get(request.access_token)
    if not user_id:
        user_id = str(uuid.uuid4())
        _users[user_id] = {
            "id": user_id,
            "nickname": f"user_{len(_users) + 1}",
            "profile_image_url": None,
            "primary_photo_url": None,
            "profile_data": {},
            "embedding": {"status": "missing", "model": None, "updated_at": None},
        }
        _kakao_token_to_user_id[request.access_token] = user_id

    access_token = f"mock-jwt-{uuid.uuid4()}"
    _tokens[access_token] = user_id

    user = _users[user_id]
    _sync_primary_photo(user_id)

    return {
        "access_token": access_token,
        "token_type": "bearer",
        "user": {
            "id": user["id"],
            "nickname": user["nickname"],
            "profile_image_url": user["profile_image_url"],
            "primary_photo_url": user.get("primary_photo_url"),
        },
    }


@router.get("/me")
def get_me(authorization: Optional[str] = Header(None)):
    user = _get_current_user(authorization)
    photos = [_photo_public(photo) for photo in _user_photos(user["id"])]
    _sync_primary_photo(user["id"])
    return {
        "id": user["id"],
        "nickname": user["nickname"],
        "profile_image_url": user["profile_image_url"],
        "primary_photo_url": user.get("primary_photo_url"),
        "profile_data": user.get("profile_data", {}),
        "photos": photos,
        "embedding": user.get("embedding", {"status": "missing", "model": None, "updated_at": None}),
    }


@router.patch("/me")
def update_me(request: MeUpdateRequest, authorization: Optional[str] = Header(None)):
    user = _get_current_user(authorization)
    if request.nickname is not None:
        user["nickname"] = request.nickname
    if request.profile_data is not None:
        user.setdefault("profile_data", {}).update(request.profile_data)
    return {"ok": True}


@router.post("/me/photos", status_code=status.HTTP_201_CREATED)
def add_photo(request: PhotoCreateRequest, authorization: Optional[str] = Header(None)):
    user = _get_current_user(authorization)
    existing = [
        photo for photo in _photos.values()
        if photo["user_id"] == user["id"] and photo["url"] == request.url
    ]
    if existing:
        return _error(
            status.HTTP_409_CONFLICT,
            "PHOTO_URL_DUPLICATE",
            "Photo URL already registered.",
        )

    photos = _user_photos(user["id"])
    next_sort_order = (photos[-1]["sort_order"] if photos else 0) + 10
    photo_id = str(uuid.uuid4())
    is_primary = request.make_primary or not photos

    photo_data = {
        "id": photo_id,
        "user_id": user["id"],
        "url": request.url,
        "sort_order": next_sort_order,
        "is_primary": is_primary,
        "created_at": _now_iso(),
    }
    _photos[photo_id] = photo_data

    if is_primary:
        _set_primary_photo(user["id"], photo_id)
    else:
        _sync_primary_photo(user["id"])

    return _photo_public(photo_data)


@router.patch("/me/photos/order")
def update_photo_order(request: PhotoOrderRequest, authorization: Optional[str] = Header(None)):
    user = _get_current_user(authorization)
    for item in request.orders:
        photo = _photos.get(item.id)
        if photo and photo["user_id"] == user["id"]:
            photo["sort_order"] = item.sort_order
    return {"ok": True}


@router.post("/me/photos/{photo_id}/primary")
def set_primary_photo(photo_id: str, authorization: Optional[str] = Header(None)):
    user = _get_current_user(authorization)
    photo = _photos.get(photo_id)
    if not photo or photo["user_id"] != user["id"]:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Photo not found")
    primary_url = _set_primary_photo(user["id"], photo_id)
    return {"ok": True, "primary_photo_url": primary_url}


@router.delete("/me/photos/{photo_id}")
def delete_photo(photo_id: str, authorization: Optional[str] = Header(None)):
    user = _get_current_user(authorization)
    photo = _photos.get(photo_id)
    if not photo or photo["user_id"] != user["id"]:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Photo not found")
    was_primary = photo["is_primary"]
    _photos.pop(photo_id, None)
    if was_primary:
        photos = _user_photos(user["id"])
        if photos:
            _set_primary_photo(user["id"], photos[0]["id"])
        else:
            user["primary_photo_url"] = None
    return {"ok": True}


@router.get("/groups")
def list_groups(authorization: Optional[str] = Header(None)):
    _seed_groups()
    user = _get_optional_user(authorization)
    user_id = user["id"] if user else None
    items = []
    for group in _groups.values():
        items.append({
            "id": group["id"],
            "name": group["name"],
            "description": group.get("description"),
            "member_count": len(group["member_ids"]),
            "is_member": user_id in group["member_ids"] if user_id else False,
        })
    return {"items": items}


@router.post("/groups/{group_id}/join")
def join_group(group_id: str, authorization: Optional[str] = Header(None)):
    _seed_groups()
    user = _get_current_user(authorization)
    group = _groups.get(group_id)
    if not group:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Group not found")
    if user["id"] not in group["member_ids"]:
        group["member_ids"].append(user["id"])
    return {"ok": True}


@router.get("/groups/{group_id}/members")
def list_group_members(group_id: str, authorization: Optional[str] = Header(None)):
    _seed_groups()
    _get_current_user(authorization)
    group = _groups.get(group_id)
    if not group:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Group not found")
    items = []
    for member_id in group["member_ids"]:
        user = _users.get(member_id)
        if not user:
            continue
        _sync_primary_photo(member_id)
        items.append({
            "user_id": member_id,
            "nickname": user["nickname"],
            "primary_photo_url": user.get("primary_photo_url"),
        })
    return {"items": items}


@router.get("/groups/{group_id}/interest-map")
def group_interest_map(group_id: str, authorization: Optional[str] = Header(None)):
    _seed_groups()
    _get_current_user(authorization)
    group = _groups.get(group_id)
    if not group:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Group not found")

    nodes = []
    for member_id in group["member_ids"]:
        user = _users.get(member_id)
        if not user:
            continue
        _sync_primary_photo(member_id)
        status_value = user.get("embedding", {}).get("status", "missing")
        coords = _stable_pair(member_id)
        nodes.append({
            "user_id": member_id,
            "nickname": user["nickname"],
            "primary_photo_url": user.get("primary_photo_url"),
            "x": coords["x"],
            "y": coords["y"],
            "embedding_status": status_value,
        })

    edges = []
    ready_nodes = [node for node in nodes if node["embedding_status"] == "ready"]
    for i in range(len(ready_nodes)):
        for j in range(i + 1, len(ready_nodes)):
            key = f"{ready_nodes[i]['user_id']}:{ready_nodes[j]['user_id']}"
            sim = _stable_pair(key)["x"]
            edges.append({
                "from_user_id": ready_nodes[i]["user_id"],
                "to_user_id": ready_nodes[j]["user_id"],
                "similarity": round(abs(sim), 2),
            })

    return {
        "group": {"id": group["id"], "name": group["name"]},
        "layout": {"method": "umap", "version": "v1", "generated_at": _now_iso()},
        "nodes": nodes,
        "edges": edges,
    }


@router.get("/groups/{group_id}/messages")
def get_group_messages(
    group_id: str,
    authorization: Optional[str] = Header(None),
    limit: int = Query(30, ge=1, le=100),
    before: Optional[str] = Query(None),
):
    _seed_groups()
    _get_current_user(authorization)
    if group_id not in _groups:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Group not found")

    items = _messages.get(group_id, [])
    if before:
        try:
            before_dt = datetime.fromisoformat(before)
            items = [item for item in items if datetime.fromisoformat(item["created_at"]) < before_dt]
        except ValueError:
            pass

    items = sorted(items, key=lambda item: item["created_at"], reverse=True)
    page = items[:limit]
    next_before = page[-1]["created_at"] if len(items) > limit else None

    return {"items": page, "next_before": next_before}


@router.post("/groups/{group_id}/messages", status_code=status.HTTP_201_CREATED)
def create_group_message(
    group_id: str,
    request: MessageCreateRequest,
    authorization: Optional[str] = Header(None),
):
    _seed_groups()
    user = _get_current_user(authorization)
    if group_id not in _groups:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Group not found")

    _messages.setdefault(group_id, [])
    _sync_primary_photo(user["id"])
    message = {
        "id": str(uuid.uuid4()),
        "group_id": group_id,
        "sender": {
            "user_id": user["id"],
            "nickname": user["nickname"],
            "primary_photo_url": user.get("primary_photo_url"),
        },
        "content": {"text": request.text},
        "created_at": _now_iso(),
    }
    _messages[group_id].append(message)
    return message


@router.post("/me/embedding/rebuild")
def rebuild_embedding(authorization: Optional[str] = Header(None)):
    user = _get_current_user(authorization)
    user["embedding"] = {
        "status": "ready",
        "model": "text-embedding-3-large",
        "updated_at": _now_iso(),
    }
    return {"ok": True, "embedding": user["embedding"]}
