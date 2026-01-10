package com.example.madclass01.domain.model

/**
 * 사용자 임베딩 데이터 모델
 * - 사용자의 취향/취미를 벡터로 표현
 * - 코사인 유사도 계산에 사용
 */
data class UserEmbedding(
    val userId: String,
    val userName: String,
    val profileImageUrl: String? = null,
    val embeddingVector: List<Float>,  // 임베딩 벡터 (N차원)
    val activityStatus: String = "활동중"  // "활동중", "오늘", "어제" 등
)

/**
 * 그래프 노드 위치 데이터
 * - Canvas에 렌더링할 때 필요한 X, Y 좌표
 * - 유사도 기반 거리
 */
data class GraphNodePosition(
    val userId: String,
    val x: Float,
    val y: Float,
    val distance: Float,  // "나"와의 유사도 (0~1, 높을수록 유사)
    val similarityScore: Float  // 코사인 유사도 (0~1)
)

/**
 * 그룹의 관계 그래프 데이터
 */
data class RelationshipGraph(
    val groupId: String,
    val currentUserId: String,
    val currentUserNode: GraphNodePosition,
    val otherUserNodes: List<GraphNodePosition>,
    val embeddings: Map<String, UserEmbedding>
)
