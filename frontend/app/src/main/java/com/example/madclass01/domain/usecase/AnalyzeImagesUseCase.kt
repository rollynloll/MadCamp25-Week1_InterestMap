package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.model.Tag
import com.example.madclass01.domain.model.TagAnalysisResult
import com.example.madclass01.domain.model.ImageItem
import javax.inject.Inject

class AnalyzeImagesUseCase @Inject constructor() {
    
    suspend operator fun invoke(images: List<ImageItem>): TagAnalysisResult {
        // AI 분석 시뮬레이션 (실제로는 서버/ML 모델 호출)
        val extractedTags = generateMockExtractedTags()
        val recommendedTags = generateMockRecommendedTags()
        
        return TagAnalysisResult(
            extractedTags = extractedTags,
            recommendedTags = recommendedTags
        )
    }
    
    private fun generateMockExtractedTags(): List<Tag> {
        return listOf(
            Tag(id = "1", name = "감성 카페", category = "hobby"),
            Tag(id = "2", name = "러닝", category = "hobby"),
            Tag(id = "3", name = "필름 카메라", category = "interest"),
            Tag(id = "4", name = "전시회", category = "interest"),
            Tag(id = "5", name = "베이킹", category = "hobby")
        )
    }
    
    private fun generateMockRecommendedTags(): List<Tag> {
        return listOf(
            Tag(id = "6", name = "흩한 카페", category = "hobby"),
            Tag(id = "7", name = "등산", category = "hobby"),
            Tag(id = "8", name = "댕댕이", category = "interest")
        )
    }
}
