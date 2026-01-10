package com.example.madclass01.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== Health Check ====================
data class HealthCheckResponse(
    val message: String
)

// ==================== 인증 (Auth) ====================

data class KakaoAuthRequest(
    @SerializedName("access_token")
    val accessToken: String
)

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String = "bearer",
    val user: AuthUser
)

data class AuthUser(
    val id: String,
    val nickname: String? = null,
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,
    @SerializedName("primary_photo_url")
    val primaryPhotoUrl: String? = null
)

// ==================== 사용자 정보 (Me) ====================

data class OkResponse(
    val ok: Boolean = true
)

data class MeResponse(
    val id: String,
    val nickname: String? = null,
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,
    @SerializedName("primary_photo_url")
    val primaryPhotoUrl: String? = null,
    @SerializedName("profile_data")
    val profileData: Map<String, Any>,
    val photos: List<MePhoto>,
    val embedding: MeEmbedding? = null
)

data class MeUpdateRequest(
    val nickname: String? = null,
    @SerializedName("profile_data")
    val profileData: Map<String, Any>? = null
)

data class MePhoto(
    val id: String,
    val url: String,
    @SerializedName("sort_order")
    val sortOrder: Int,
    @SerializedName("is_primary")
    val isPrimary: Boolean,
    @SerializedName("created_at")
    val createdAt: String
)

data class MeEmbedding(
    val status: String,
    val model: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

// ==================== 사진 관리 (Photos) ====================

data class PhotoCreateRequest(
    val url: String,
    @SerializedName("make_primary")
    val makePrimary: Boolean = false
)

data class PhotoOrderItem(
    val id: String,
    @SerializedName("sort_order")
    val sortOrder: Int
)

data class PhotoOrderRequest(
    val orders: List<PhotoOrderItem>
)

// ==================== 그룹 (Groups) ====================

data class GroupListItem(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerializedName("member_count")
    val memberCount: Int,
    @SerializedName("is_member")
    val isMember: Boolean
)

data class GroupMemberItem(
    @SerializedName("user_id")
    val userId: String,
    val nickname: String? = null,
    @SerializedName("primary_photo_url")
    val primaryPhotoUrl: String? = null
)

data class GroupResponse(
    val id: String,
    val name: String,
    @SerializedName("creator_id")
    val creatorId: String,
    val description: String? = null,
    @SerializedName("member_ids")
    val memberIds: List<String>,
    @SerializedName("created_at")
    val createdAt: String
)

data class CreateGroupRequest(
    val name: String,
    @SerializedName("creator_id")
    val creatorId: String,
    val description: String? = null
)

data class AddMemberRequest(
    @SerializedName("user_id")
    val userId: String
)

// ==================== Interest Map ====================

data class InterestMapResponse(
    val group: InterestMapGroup,
    val layout: InterestMapLayout,
    val nodes: List<InterestMapNode>,
    val edges: List<InterestMapEdge>
)

data class InterestMapGroup(
    val id: String,
    val name: String
)

data class InterestMapLayout(
    val method: String,
    val version: String,
    @SerializedName("generated_at")
    val generatedAt: String
)

data class InterestMapNode(
    @SerializedName("user_id")
    val userId: String,
    val nickname: String? = null,
    @SerializedName("primary_photo_url")
    val primaryPhotoUrl: String? = null,
    val x: Float,
    val y: Float,
    @SerializedName("embedding_status")
    val embeddingStatus: String
)

data class InterestMapEdge(
    @SerializedName("from_user_id")
    val fromUserId: String,
    @SerializedName("to_user_id")
    val toUserId: String,
    val similarity: Float
)

// ==================== 그룹 메시지 (Messages) ====================

data class MessageCreateRequest(
    val text: String
)

data class MessageContent(
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    val nickname: String? = null,
    @SerializedName("primary_photo_url")
    val primaryPhotoUrl: String? = null,
    val text: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("sent_at")
    val sentAt: String
)

// ==================== 사용자 (User) ====================

data class CreateUserRequest(
    val provider: String,
    @SerializedName("provider_user_id")
    val providerUserId: String,
    val nickname: String? = null,
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,
    @SerializedName("profile_data")
    val profileData: Map<String, Any>? = null
)

data class UpdateUserRequest(
    val nickname: String? = null,
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,
    @SerializedName("profile_data")
    val profileData: Map<String, Any>? = null
)

data class UserResponse(
    val id: String,
    val provider: String,
    @SerializedName("provider_user_id")
    val providerUserId: String,
    val nickname: String? = null,
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,
    @SerializedName("profile_data")
    val profileData: Map<String, Any>,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class PhotoResponse(
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("file_path")
    val filePath: String,
    @SerializedName("file_url")
    val fileUrl: String,
    @SerializedName("uploaded_at")
    val uploadedAt: String
)

// ==================== Image Analysis ====================

data class ImageAnalysisRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("image_urls")
    val imageUrls: List<String>
)

data class ImageKeyword(
    val keyword: String,
    val confidence: Float,
    val category: String? = null
)

data class ImageAnalysisResult(
    @SerializedName("image_url")
    val imageUrl: String,
    val caption: String,
    val keywords: List<ImageKeyword>
)

data class ImageAnalysisResponse(
    @SerializedName("user_id")
    val userId: String,
    val results: List<ImageAnalysisResult>,
    @SerializedName("recommended_tags")
    val recommendedTags: List<String>,
    @SerializedName("all_keywords")
    val allKeywords: List<ImageKeyword>
)

// ==================== Embedding ====================

data class GenerateEmbeddingRequest(
    @SerializedName("user_id")
    val userId: String,
    val nickname: String,
    val age: Int? = null,
    val region: String? = null,
    val bio: String? = null,
    val tags: List<String>,
    @SerializedName("image_keywords")
    val imageKeywords: List<String>
)

data class EmbeddingResponse(
    @SerializedName("user_id")
    val userId: String,
    val embedding: List<Float>,
    @SerializedName("map_position")
    val mapPosition: MapPosition
)

data class MapPosition(
    val x: Float,
    val y: Float
)
