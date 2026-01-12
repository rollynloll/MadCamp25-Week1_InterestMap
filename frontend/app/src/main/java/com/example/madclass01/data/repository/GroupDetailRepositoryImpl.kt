package com.example.madclass01.data.repository

import com.example.madclass01.data.remote.ApiService
import com.example.madclass01.data.remote.dto.GroupDetailResponse
import com.example.madclass01.data.remote.dto.GroupEmbeddingResponse
import com.example.madclass01.data.remote.dto.UserEmbeddingResponse
import com.example.madclass01.domain.model.Group
import com.example.madclass01.domain.model.GraphNodePosition
import com.example.madclass01.domain.model.RelationshipGraph
import com.example.madclass01.domain.model.UserEmbedding
import com.example.madclass01.domain.repository.GroupDetailRepository
import com.example.madclass01.utils.GraphLayoutCalculator
import com.example.madclass01.utils.SimilarityCalculator
import javax.inject.Inject

/**
 * 그룹 상세 정보 및 관계 그래프 Repository 구현
 */
class GroupDetailRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : GroupDetailRepository {

    override suspend fun getGroupDetail(groupId: String): Result<Group> {
        return try {
            val response = apiService.getGroupDetail(groupId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomainModel())
            } else {
                Result.failure(Exception("Failed to get group detail: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGroupUserEmbeddings(
        groupId: String,
        currentUserId: String?
    ): Result<List<UserEmbedding>> {
        return try {
            val response = apiService.getGroupUserEmbeddings(groupId, currentUserId)
            if (response.isSuccessful && response.body() != null) {
                val embeddings = response.body()!!
                val userEmbeddings = listOf(embeddings.currentUserEmbedding) + 
                    embeddings.otherUserEmbeddings
                Result.success(userEmbeddings.map { it.toDomainModel() })
            } else {
                Result.failure(Exception("Failed to get embeddings: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserEmbedding(userId: String): Result<UserEmbedding> {
        return try {
            val response = apiService.getUserEmbedding(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomainModel())
            } else {
                Result.failure(Exception("Failed to get user embedding: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRelationshipGraph(
        groupId: String,
        currentUserId: String
    ): Result<RelationshipGraph> {
        return try {
            // 1. 그룹 임베딩 데이터 조회
            val embeddingsResponse = apiService.getGroupUserEmbeddings(groupId, currentUserId)
            if (!embeddingsResponse.isSuccessful || embeddingsResponse.body() == null) {
                return Result.failure(Exception("Failed to get embeddings"))
            }

            val embeddings = embeddingsResponse.body()!!
            val currentUserEmbeddingDto = embeddings.currentUserEmbedding
            val otherUserEmbeddingDtos = embeddings.otherUserEmbeddings

            // 2. DTO를 Domain 모델로 변환
            val currentUserEmbedding = currentUserEmbeddingDto.toDomainModel()
            val otherUserEmbeddings = otherUserEmbeddingDtos.map { it.toDomainModel() }

            // 3. 현재 사용자의 노드 위치 (중심)
            val centerX = 167f  // 390 / 2
            val centerY = 460f  // 200 + 260 (Graph Canvas 중앙)
            
            val currentUserNode = GraphNodePosition(
                userId = currentUserId,
                x = centerX,
                y = centerY,
                distance = 0f,
                similarityScore = 1f  // 자신과의 유사도는 1
            )

            // 4. 다른 사용자들의 노드 위치 계산
            val otherUserNodes = GraphLayoutCalculator.calculateNodePositions(
                currentUserEmbedding = currentUserEmbedding,
                otherUserEmbeddings = otherUserEmbeddings,
                centerX = centerX,
                centerY = centerY,
                maxDistance = 150f
            )

            // 5. 임베딩 맵 생성
            val embeddingMap = mutableMapOf<String, UserEmbedding>()
            embeddingMap[currentUserId] = currentUserEmbedding
            otherUserEmbeddings.forEach { embedding ->
                embeddingMap[embedding.userId] = embedding
            }

            // 6. 관계 그래프 생성
            val relationshipGraph = RelationshipGraph(
                groupId = groupId,
                currentUserId = currentUserId,
                currentUserNode = currentUserNode,
                otherUserNodes = otherUserNodes,
                embeddings = embeddingMap
            )

            Result.success(relationshipGraph)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // DTO 변환 메서드
    private fun GroupDetailResponse.toDomainModel(): Group {
        return Group(
            id = this.id,
            name = this.name,
            description = this.description ?: "",
            memberCount = this.memberCount,
            activity = "보통",
            tags = emptyList(),
            imageUrl = "",
            lastActivityDate = "",
            messageCount = 0,
            matchPercentage = 0,
            region = "",
            memberAge = "",
            isJoined = false
        )
    }

    private fun UserEmbeddingResponse.toDomainModel(): UserEmbedding {
        return UserEmbedding(
            userId = this.userId,
            userName = this.userName,
            profileImageUrl = this.profileImageUrl,
            embeddingVector = this.embeddingVector,
            activityStatus = this.activityStatus
        )
    }
}
