package com.example.madclass01.utils

import kotlin.math.sqrt

/**
 * 코사인 유사도(Cosine Similarity) 계산 유틸리티
 * - 두 벡터 간의 각도를 기반으로 유사도 계산
 * - 범위: 0 (완전히 다름) ~ 1 (동일)
 */
object SimilarityCalculator {

    /**
     * 두 벡터의 코사인 유사도 계산
     * @param vector1 첫 번째 벡터
     * @param vector2 두 번째 벡터
     * @return 코사인 유사도 (0~1)
     */
    fun cosineSimilarity(vector1: List<Float>, vector2: List<Float>): Float {
        if (vector1.isEmpty() || vector2.isEmpty()) return 0f
        if (vector1.size != vector2.size) return 0f

        // 내적(Dot Product) 계산
        val dotProduct = vector1.zip(vector2).sumOf { (a, b) -> (a * b).toDouble() }

        // 각 벡터의 크기(Magnitude) 계산
        val magnitude1 = sqrt(vector1.sumOf { (it * it).toDouble() })
        val magnitude2 = sqrt(vector2.sumOf { (it * it).toDouble() })

        // 0으로 나누는 것을 방지
        if (magnitude1 == 0.0 || magnitude2 == 0.0) return 0f

        // 코사인 유사도 = 내적 / (magnitude1 * magnitude2)
        return (dotProduct / (magnitude1 * magnitude2)).toFloat()
    }

    /**
     * 유사도를 거리로 변환
     * - 유사도가 높을수록 거리가 짧음
     * @param similarity 코사인 유사도 (0~1)
     * @return 거리 (0 이상)
     */
    fun similarityToDistance(similarity: Float): Float {
        return 1f - similarity  // 유사도가 높을수록 거리는 짧음
    }

    /**
     * 거리를 실제 픽셀 거리로 변환
     * @param similarity 코사인 유사도 (0~1)
     * @param maxDistance 최대 거리 (픽셀)
     * @return 화면상 거리 (픽셀)
     */
    fun similarityToPixelDistance(similarity: Float, maxDistance: Float = 150f): Float {
        // 유사도가 높을수록 (0에 가까울수록) 거리가 짧음
        // 유사도가 0.5이면 maxDistance의 절반
        val distance = (1f - similarity) * maxDistance
        return distance.coerceIn(30f, maxDistance)  // 최소 30px, 최대 maxDistance
    }
}
