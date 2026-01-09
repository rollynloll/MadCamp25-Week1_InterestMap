package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.model.Tag
import javax.inject.Inject

class ToggleTagUseCase @Inject constructor() {
    suspend operator fun invoke(tagId: String, tags: List<Tag>): List<Tag> {
        return tags.map { tag ->
            if (tag.id == tagId) {
                tag.copy(isSelected = !tag.isSelected)
            } else {
                tag
            }
        }
    }
}
