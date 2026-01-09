package com.example.madclass01.data.remote.dto

import com.google.gson.annotations.SerializedName

// Health Check
data class HealthCheckResponse(
    val message: String
)

// User DTOs
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
    val nickname: String?,
    @SerializedName("profile_image_url")
    val profileImageUrl: String?,
    @SerializedName("profile_data")
    val profileData: Map<String, Any>,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

// Photo DTOs
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

// Group DTOs
data class CreateGroupRequest(
    val name: String,
    @SerializedName("creator_id")
    val creatorId: String,
    val description: String? = null
)

data class GroupResponse(
    val id: String,
    val name: String,
    @SerializedName("creator_id")
    val creatorId: String,
    val description: String?,
    @SerializedName("member_ids")
    val memberIds: List<String>,
    @SerializedName("created_at")
    val createdAt: String
)

data class AddMemberRequest(
    @SerializedName("user_id")
    val userId: String
)

// Image Analysis DTOs
data class ImageAnalysisRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("image_urls")
    val imageUrls: List<String>
)

data class ImageKeyword(
    val keyword: String,
    val confidence: Float,
    val category: String?
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

// Embedding DTOs
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
