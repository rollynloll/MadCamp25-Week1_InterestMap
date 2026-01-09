package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.model.Tag
import javax.inject.Inject

class AddTagUseCase @Inject constructor() {
    suspend operator fun invoke(tagName: String, category: String, currentTags: List<Tag>): Pair<Boolean, List<Tag>> {
        if (tagName.isEmpty() || tagName.length > 20) {
            return Pair(false, currentTags)
        }
        
        // 중복 확인
        if (currentTags.any { it.name == tagName }) {
            return Pair(false, currentTags)
        }
        
        val newTag = Tag(
            id = "${System.currentTimeMillis()}",
            name = tagName,
            category = category,
            isSelected = true
        )
        
        return Pair(true, currentTags + newTag)
    }
}
