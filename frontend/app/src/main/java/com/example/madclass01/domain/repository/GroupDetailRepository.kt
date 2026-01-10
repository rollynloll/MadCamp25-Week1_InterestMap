package com.example.madclass01.domain.repository

import com.example.madclass01.domain.model.Group
import com.example.madclass01.domain.model.RelationshipGraph
import com.example.madclass01.domain.model.UserEmbedding

/**
 * 그룹 상세 정보 및 관계 그래프 데이터 접근
 */
interface GroupDetailRepository {
    
    /**
     * 그룹 상세 정보 조회
     */
    suspend fun getGroupDetail(groupId: String): Result<Group>
    
    /**
     * 그룹 내 사용자들의 임베딩 데이터 조회
     */
    suspend fun getGroupUserEmbeddings(groupId: String): Result<List<UserEmbedding>>
    
    /**
     * 현재 사용자의 임베딩 데이터 조회
     */
    suspend fun getCurrentUserEmbedding(userId: String): Result<UserEmbedding>
    
    /**
     * 관계 그래프 데이터 조회 및 계산
     */
    suspend fun getRelationshipGraph(
        groupId: String,
        currentUserId: String
    ): Result<RelationshipGraph>
}
