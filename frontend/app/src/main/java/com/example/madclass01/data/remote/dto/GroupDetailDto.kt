package com.example.madclass01.data.remote.dto

/**
 * 사용자 임베딩 DTO
 * - 백엔드에서 받은 임베딩 벡터 데이터
 */
data class UserEmbeddingResponse(
    val userId: String,
    val userName: String,
    val profileImageUrl: String? = null,
    val embeddingVector: List<Float>,
    val activityStatus: String = "활동중"
)

/**
 * 그룹 임베딩 데이터 응답
 * - 그룹 내 전체 사용자의 임베딩
 */
data class GroupEmbeddingResponse(
    val groupId: String,
    val currentUserId: String,
    val currentUserEmbedding: UserEmbeddingResponse,
    val otherUserEmbeddings: List<UserEmbeddingResponse>
)

/**
 * 그룹 상세 정보 응답
 */
data class GroupDetailResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val iconType: String,
    val memberCount: Int,
    val isPublic: Boolean,
    val createdByUserId: String,
    val createdAt: String,
    val updatedAt: String,
    val profileImageUrl: String? = null,
    val activityStatus: String = "오늘 활동"
)
