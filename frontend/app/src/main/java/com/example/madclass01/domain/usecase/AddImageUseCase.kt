package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.model.ImageItem
import javax.inject.Inject

class AddImageUseCase @Inject constructor() {
    suspend operator fun invoke(imageItem: ImageItem, currentImages: List<ImageItem>): Pair<Boolean, List<ImageItem>> {
        // 최대 20개 제한
        if (currentImages.size >= 20) {
            return Pair(false, currentImages)
        }
        
        // 중복 제거
        if (currentImages.any { it.uri == imageItem.uri }) {
            return Pair(false, currentImages)
        }
        
        return Pair(true, currentImages + imageItem)
    }
}
