package com.example.madclass01.utils

import com.example.madclass01.domain.model.GraphNodePosition
import com.example.madclass01.domain.model.UserEmbedding
import kotlin.math.cos
import kotlin.math.sin

/**
 * 그래프 노드의 위치를 계산하는 유틸리티
 * - 원형 배치 또는 힘 기반 레이아웃
 * - 유사도 기반 거리 계산
 */
object GraphLayoutCalculator {

    /**
     * 사용자 임베딩과 현재 사용자를 기반으로 그래프 노드 위치 계산
     * @param currentUserEmbedding 현재 사용자의 임베딩
     * @param otherUserEmbeddings 다른 사용자들의 임베딩
     * @param centerX 중심 X 좌표 (현재 사용자)
     * @param centerY 중심 Y 좌표 (현재 사용자)
     * @param maxDistance 최대 거리 (픽셀)
     * @return GraphNodePosition 목록
     */
    fun calculateNodePositions(
        currentUserEmbedding: UserEmbedding,
        otherUserEmbeddings: List<UserEmbedding>,
        centerX: Float = 167f,  // 390 / 2
        centerY: Float = 460f,  // 200 + 260 (Graph Canvas 중앙)
        maxDistance: Float = 150f
    ): List<GraphNodePosition> {
        return otherUserEmbeddings.mapIndexed { index, userEmbedding ->
            // 1. 코사인 유사도 계산
            val similarity = SimilarityCalculator.cosineSimilarity(
                currentUserEmbedding.embeddingVector,
                userEmbedding.embeddingVector
            )

            // 2. 유사도 기반 거리 계산
            val pixelDistance = SimilarityCalculator.similarityToPixelDistance(similarity, maxDistance)

            // 3. 원형 배치: 각도 계산 (균등 분산)
            val totalUsers = otherUserEmbeddings.size
            val angle = (2 * Math.PI * index / totalUsers).toFloat()

            // 4. 극좌표 → 직교좌표 변환
            val x = centerX + pixelDistance * cos(angle.toDouble()).toFloat()
            val y = centerY + pixelDistance * sin(angle.toDouble()).toFloat()

            GraphNodePosition(
                userId = userEmbedding.userId,
                x = x,
                y = y,
                distance = pixelDistance,
                similarityScore = similarity
            )
        }
    }

    /**
     * 힘 기반 레이아웃 (Force-Directed Layout)
     * - 더 자연스러운 배치
     * - 반발력과 인력을 사용하여 최적의 배치 계산
     */
    fun calculateForceDirectedLayout(
        currentUserEmbedding: UserEmbedding,
        otherUserEmbeddings: List<UserEmbedding>,
        centerX: Float = 167f,
        centerY: Float = 460f,
        maxDistance: Float = 150f,
        iterations: Int = 50
    ): List<GraphNodePosition> {
        // 초기 위치 (원형 배치)
        var positions = calculateNodePositions(
            currentUserEmbedding,
            otherUserEmbeddings,
            centerX,
            centerY,
            maxDistance
        ).toMutableList()

        // 반복 최적화
        repeat(iterations) {
            val newPositions = positions.toMutableList()

            positions.forEachIndexed { index, position ->
                var fx = 0f  // X 방향 힘
                var fy = 0f  // Y 방향 힘

                // 다른 노드들로부터의 반발력
                positions.forEachIndexed { otherIndex, otherPosition ->
                    if (index != otherIndex) {
                        val dx = position.x - otherPosition.x
                        val dy = position.y - otherPosition.y
                        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                        if (distance > 0) {
                            val repulsion = 5000f / (distance * distance)
                            fx += (dx / distance) * repulsion
                            fy += (dy / distance) * repulsion
                        }
                    }
                }

                // 중심으로의 인력 (유사도 기반)
                val dx = position.x - centerX
                val dy = position.y - centerY
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                if (distance > 0) {
                    val targetDistance = SimilarityCalculator.similarityToPixelDistance(
                        position.similarityScore,
                        maxDistance
                    )
                    val attraction = 0.1f * (distance - targetDistance)
                    fx -= (dx / distance) * attraction
                    fy -= (dy / distance) * attraction
                }

                // 속도 제한
                val speed = kotlin.math.sqrt(fx * fx + fy * fy)
                if (speed > 2f) {
                    fx = (fx / speed) * 2f
                    fy = (fy / speed) * 2f
                }

                // 위치 업데이트
                newPositions[index] = position.copy(
                    x = (position.x + fx).coerceIn(30f, 360f),
                    y = (position.y + fy).coerceIn(230f, 650f)
                )
            }

            positions = newPositions
        }

        return positions
    }

    /**
     * 사용자 크기 계산 (유사도 기반)
     * - 유사도가 높을수록 더 큼
     */
    fun calculateNodeSize(similarity: Float): Float {
        // 유사도 0.5 = 56px, 유사도 0.3 = 48px, 유사도 0.1 = 40px
        return 40f + (similarity * 16f)  // 40~56px 범위
    }

    /**
     * 사용자 배경 색상 선택 (유사도 기반)
     */
    fun selectNodeColor(similarity: Float): String {
        return when {
            similarity >= 0.7 -> "#10B981"   // 초록색 (매우 유사)
            similarity >= 0.5 -> "#10B981"   // 초록색 (유사)
            similarity >= 0.3 -> "#F59E0B"   // 주황색 (보통)
            else -> "#E5E7EB"                // 회색 (낮음)
        }
    }
}
