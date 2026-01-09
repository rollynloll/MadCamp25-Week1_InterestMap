package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.model.ImageItem
import javax.inject.Inject

class RemoveImageUseCase @Inject constructor() {
    suspend operator fun invoke(imageUri: String, currentImages: List<ImageItem>): List<ImageItem> {
        return currentImages.filter { it.uri != imageUri }
    }
}
