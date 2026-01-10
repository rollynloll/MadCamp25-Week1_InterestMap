from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
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

# User Schemas
class UserCreateRequest(BaseModel):
    provider: str = Field(..., description="OAuth provider (kakao, google, etc)")
    provider_user_id: str = Field(..., description="Provider's user ID")
    nickname: Optional[str] = None
    profile_image_url: Optional[str] = None
    profile_data: Optional[Dict[str, Any]] = None

class UserUpdateRequest(BaseModel):
    nickname: Optional[str] = None
    profile_image_url: Optional[str] = None
    profile_data: Optional[Dict[str, Any]] = None


class GroupMembersResponse(BaseSchema):
    items: list[GroupMemberItem]


class UserResponse(BaseModel):
    id: str
    provider: str
    provider_user_id: str
    nickname: Optional[str]
    profile_image_url: Optional[str]
    profile_data: Dict[str, Any]
    created_at: str
    updated_at: str
    
    class Config:
        from_attributes = True


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


# Legacy /api schemas used by Android client
class UserCreateRequest(BaseSchema):
    provider: str
    provider_user_id: str
    nickname: Optional[str]
    profile_image_url: Optional[str]
    profile_data: Dict[str, Any]
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
    created_at: str
    updated_at: str

    class Config:
        from_attributes = True

# Image Analysis Schemas
class ImageAnalysisRequest(BaseModel):
    user_id: str
    image_urls: List[str] = Field(..., description="List of uploaded image URLs")

class ImageKeyword(BaseModel):
    keyword: str
    confidence: float
    category: Optional[str] = None  # "hobby", "interest", "style", etc.

class ImageAnalysisResult(BaseModel):
    image_url: str
    caption: str
    keywords: List[ImageKeyword]

class ImageAnalysisResponse(BaseModel):
    user_id: str
    results: List[ImageAnalysisResult]
    recommended_tags: List[str] = Field(..., description="TOP-5 recommended tags")
    all_keywords: List[ImageKeyword] = Field(..., description="All extracted keywords with scores")


class PhotoUploadResponse(BaseSchema):
# Embedding Schemas
class GenerateEmbeddingRequest(BaseModel):
    user_id: str
    nickname: str
    age: Optional[int] = None
    region: Optional[str] = None
    bio: Optional[str] = None
    tags: List[str] = Field(..., description="User selected tags")
    image_keywords: List[str] = Field(..., description="Keywords from image analysis")

class EmbeddingResponse(BaseModel):
    user_id: str
    embedding: List[float] = Field(..., description="128-dimensional embedding vector")
    map_position: Dict[str, float] = Field(..., description="2D position on feature map (x, y)")

# Photo Schemas
class PhotoUploadResponse(BaseModel):
    id: str
    user_id: str
    file_path: str
    file_url: str
    uploaded_at: str


    class Config:
        from_attributes = True

class GroupCreateRequest(BaseSchema):
    name: str
# Group Schemas
class GroupCreateRequest(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    creator_id: str
    description: str | None = None
    description: Optional[str] = None


class GroupResponse(BaseSchema):
class GroupResponse(BaseModel):
    id: str
    name: str
    creator_id: str
    description: str | None
    member_ids: list[str]
    description: Optional[str]
    member_ids: List[str]
    created_at: str


    class Config:
        from_attributes = True

class AddMemberRequest(BaseSchema):
class AddMemberRequest(BaseModel):
    user_id: str


class TagAnalysisRequest(BaseSchema):
# Tag/Embedding Schemas
class TagAnalysisRequest(BaseModel):
    user_id: str
    image_urls: list[str]


class TagAnalysisResponse(BaseSchema):
    tags: list[str]
    categories: list[str]
    interests: list[str]


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
    tags: list[str]
    image_keywords: list[str]


class GenerateEmbeddingResponse(BaseSchema):
    user_id: str
    embedding: list[float]
    map_position: dict[str, float]
    image_urls: List[str]

class TagAnalysisResponse(BaseModel):
    tags: List[str]
    categories: List[str]
    interests: List[str]
