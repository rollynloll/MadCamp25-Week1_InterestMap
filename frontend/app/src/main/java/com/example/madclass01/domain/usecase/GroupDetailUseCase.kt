package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.model.Group
import com.example.madclass01.domain.model.RelationshipGraph
import com.example.madclass01.domain.repository.GroupDetailRepository
import javax.inject.Inject

/**
 * 그룹 상세 정보 조회 UseCase
 */
class GetGroupDetailUseCase @Inject constructor(
    private val groupDetailRepository: GroupDetailRepository
) {
    suspend operator fun invoke(groupId: String): Result<Group> {
        return try {
            groupDetailRepository.getGroupDetail(groupId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 관계 그래프 조회 UseCase
 * - 그룹 내 사용자들의 임베딩을 조회
 * - 코사인 유사도 기반 거리 계산
 * - 노드 위치 계산
 */
class GetRelationshipGraphUseCase @Inject constructor(
    private val groupDetailRepository: GroupDetailRepository
) {
    suspend operator fun invoke(
        groupId: String,
        currentUserId: String
    ): Result<RelationshipGraph> {
        return try {
            groupDetailRepository.getRelationshipGraph(groupId, currentUserId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
