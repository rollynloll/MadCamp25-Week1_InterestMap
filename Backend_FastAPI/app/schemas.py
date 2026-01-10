from datetime import datetime
from typing import Any

from pydantic import BaseModel, ConfigDict


class BaseSchema(BaseModel):
    model_config = ConfigDict(from_attributes=True)


class OkResponse(BaseSchema):
    ok: bool = True


class KakaoAuthRequest(BaseSchema):
    access_token: str


class AuthUser(BaseSchema):
    id: str
    nickname: str | None
    profile_image_url: str | None
    primary_photo_url: str | None


class AuthResponse(BaseSchema):
    access_token: str
    token_type: str = "bearer"
    user: AuthUser


class MePhoto(BaseSchema):
    id: str
    url: str
    sort_order: int
    is_primary: bool
    created_at: datetime


class MeEmbedding(BaseSchema):
    status: str
    model: str | None = None
    updated_at: datetime | None = None


class MeResponse(BaseSchema):
    id: str
    nickname: str | None
    profile_image_url: str | None
    primary_photo_url: str | None
    profile_data: dict[str, Any]
    photos: list[MePhoto]
    embedding: MeEmbedding | None = None


class MeUpdateRequest(BaseSchema):
    nickname: str | None = None
    profile_data: dict[str, Any] | None = None


class PhotoCreateRequest(BaseSchema):
    url: str
    make_primary: bool = False


class PhotoOrderItem(BaseSchema):
    id: str
    sort_order: int


class PhotoOrderRequest(BaseSchema):
    orders: list[PhotoOrderItem]


class GroupListItem(BaseSchema):
    id: str
    name: str
    description: str | None
    member_count: int
    is_member: bool


class GroupListResponse(BaseSchema):
    items: list[GroupListItem]


class GroupMemberItem(BaseSchema):
    user_id: str
    nickname: str | None
    primary_photo_url: str | None


class GroupMembersResponse(BaseSchema):
    items: list[GroupMemberItem]


class InterestMapGroup(BaseSchema):
    id: str
    name: str


class InterestMapLayout(BaseSchema):
    method: str
    version: str
    generated_at: datetime


class InterestMapNode(BaseSchema):
    user_id: str
    nickname: str | None
    primary_photo_url: str | None
    x: float
    y: float
    embedding_status: str


class InterestMapEdge(BaseSchema):
    from_user_id: str
    to_user_id: str
    similarity: float


class InterestMapResponse(BaseSchema):
    group: InterestMapGroup
    layout: InterestMapLayout
    nodes: list[InterestMapNode]
    edges: list[InterestMapEdge]


class MessageSender(BaseSchema):
    user_id: str
    nickname: str | None
    primary_photo_url: str | None


class MessageContent(BaseSchema):
    text: str


class MessageItem(BaseSchema):
    id: str
    group_id: str
    sender: MessageSender
    content: MessageContent
    created_at: datetime


class MessageListResponse(BaseSchema):
    items: list[MessageItem]
    next_before: datetime | None = None


class MessageCreateRequest(BaseSchema):
    text: str


class EmbeddingResponse(BaseSchema):
    ok: bool
    embedding: MeEmbedding
