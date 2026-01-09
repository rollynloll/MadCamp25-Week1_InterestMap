package com.example.madclass01.domain.model

data class TagAnalysisResult(
    val extractedTags: List<Tag> = emptyList(), // AI가 추출한 태그
    val recommendedTags: List<Tag> = emptyList() // AI가 추천한 태그
)
