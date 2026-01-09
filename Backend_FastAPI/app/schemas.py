from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from datetime import datetime

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

# Group Schemas
class GroupCreateRequest(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    creator_id: str
    description: Optional[str] = None

class GroupResponse(BaseModel):
    id: str
    name: str
    creator_id: str
    description: Optional[str]
    member_ids: List[str]
    created_at: str
    
    class Config:
        from_attributes = True

class AddMemberRequest(BaseModel):
    user_id: str

# Tag/Embedding Schemas
class TagAnalysisRequest(BaseModel):
    user_id: str
    image_urls: List[str]

class TagAnalysisResponse(BaseModel):
    tags: List[str]
    categories: List[str]
    interests: List[str]
