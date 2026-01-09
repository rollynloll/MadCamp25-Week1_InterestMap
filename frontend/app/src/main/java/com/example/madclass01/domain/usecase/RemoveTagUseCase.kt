package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.model.Tag
import javax.inject.Inject

class RemoveTagUseCase @Inject constructor() {
    suspend operator fun invoke(tagId: String, currentTags: List<Tag>): List<Tag> {
        return currentTags.filter { it.id != tagId }
    }
}
