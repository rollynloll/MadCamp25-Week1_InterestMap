from __future__ import annotations

from datetime import datetime
from typing import Any

from pydantic import AliasChoices, BaseModel, ConfigDict, Field


class BaseSchema(BaseModel):
    model_config = ConfigDict(from_attributes=True)


class OkResponse(BaseSchema):
    ok: bool = True


class KakaoAuthRequest(BaseSchema):
    access_token: str = Field(validation_alias=AliasChoices("access_token", "accessToken"))


class AuthUser(BaseSchema):
    id: str
    nickname: str | None = None
    profile_image_url: str | None = None
    primary_photo_url: str | None = None


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
    nickname: str | None = None
    profile_image_url: str | None = None
    primary_photo_url: str | None = None
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
    description: str | None = None
    member_count: int
    is_member: bool
    tags: list[str] = []
    region: str = ""
    image_url: str = ""


class GroupListResponse(BaseSchema):
    items: list[GroupListItem]


class GroupMemberItem(BaseSchema):
    user_id: str
    nickname: str | None
    primary_photo_url: str | None


class GroupMembersResponse(BaseSchema):
    items: list[GroupMemberItem]


class UserCreateRequest(BaseSchema):
    provider: str
    provider_user_id: str
    nickname: str | None = None
    profile_image_url: str | None = None
    profile_data: dict[str, Any] | None = None


class UserUpdateRequest(BaseSchema):
    nickname: str | None = None
    profile_image_url: str | None = None
    profile_data: dict[str, Any] | None = None


class UserResponse(BaseSchema):
    id: str
    provider: str
    provider_user_id: str
    nickname: str | None
    profile_image_url: str | None
    profile_data: dict[str, Any]
    is_new_user: bool = False
    created_at: str | None
    updated_at: str | None


class GroupCreateRequest(BaseSchema):
    name: str = Field(..., min_length=1, max_length=100)
    creator_id: str
    description: str | None = None
    tags: list[str] = Field(default_factory=list)
    region: str | None = None
    image_url: str | None = None
    icon_type: str | None = None
    is_public: bool = True


class GroupResponse(BaseSchema):
    id: str
    name: str
    creator_id: str
    description: str | None
    member_ids: list[str]
    created_at: str
    tags: list[str] = []
    region: str = ""
    image_url: str = ""
    icon_type: str = ""
    is_public: bool = True


class GroupSearchItem(BaseSchema):
    id: str
    name: str
    description: str | None = None
    memberCount: int = 0
    tags: list[str] = []
    region: str = ""
    imageUrl: str = ""
    iconType: str = ""
    isPublic: bool = True
    matchScore: float = 0.0


class GroupSearchResponse(BaseSchema):
    items: list[GroupSearchItem]


class GroupDetailResponse(BaseSchema):
    id: str
    name: str
    description: str | None = None
    iconType: str
    memberCount: int
    isPublic: bool
    createdByUserId: str
    createdAt: str
    updatedAt: str
    profileImageUrl: str | None = None
    activityStatus: str = "오늘 활동"


class UserEmbeddingResponse(BaseSchema):
    userId: str
    userName: str
    profileImageUrl: str | None = None
    embeddingVector: list[float]
    activityStatus: str = "활동중"


class GraphNodePositionResponse(BaseSchema):
    userId: str
    x: float
    y: float
    distance: float
    similarityScore: float


class GroupEmbeddingResponse(BaseSchema):
    groupId: str
    currentUserId: str
    currentUserEmbedding: UserEmbeddingResponse
    otherUserEmbeddings: list[UserEmbeddingResponse]
    nodePositions: list[GraphNodePositionResponse]


class AddMemberRequest(BaseSchema):
    user_id: str


class SubgroupClusterRequest(BaseSchema):
    index: int
    member_ids: list[str]


class SubgroupCreateRequest(BaseSchema):
    clusters: list[SubgroupClusterRequest]


class SubgroupItemResponse(BaseSchema):
    id: str
    name: str
    cluster_index: int
    member_ids: list[str]


class PhotoUploadResponse(BaseSchema):
    id: str
    user_id: str
    file_path: str
    file_url: str
    uploaded_at: str | None


class ImageAnalysisRequest(BaseSchema):
    user_id: str
    image_urls: list[str]


class ImageKeyword(BaseSchema):
    keyword: str
    confidence: float
    category: str | None = None


class ImageAnalysisResult(BaseSchema):
    image_url: str
    caption: str
    keywords: list[ImageKeyword]


class ImageAnalysisResponse(BaseSchema):
    user_id: str
    results: list[ImageAnalysisResult]
    recommended_tags: list[str]
    all_keywords: list[ImageKeyword]


class GenerateEmbeddingRequest(BaseSchema):
    user_id: str
    nickname: str
    age: int | None = None
    region: str | None = None
    bio: str | None = None
    tags: list[str] = Field(default_factory=list)
    image_keywords: list[str] = Field(default_factory=list)


class GenerateEmbeddingResponse(BaseSchema):
    user_id: str
    embedding: list[float]
    map_position: dict[str, float]


class TextEmbeddingRequest(BaseSchema):
    user_id: str
    text: str


class BatchPhotoUploadResponse(BaseSchema):
    photos: list[PhotoUploadResponse]
    suggested_tags: list[str]
    image_tags: list[str] = Field(default_factory=list)
    embedding: list[float] | None = None
    map_position: dict[str, float] | None = None


class EmbeddingResponse(BaseSchema):
    ok: bool
    embedding: MeEmbedding


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


class PublicMessageCreateRequest(BaseSchema):
    user_id: str
    text: str


class PublicMessageItem(BaseSchema):
    id: str
    group_id: str
    user_id: str
    nickname: str | None = None
    primary_photo_url: str | None = None
    text: str | None = None
    image_url: str | None = None
    sent_at: datetime
