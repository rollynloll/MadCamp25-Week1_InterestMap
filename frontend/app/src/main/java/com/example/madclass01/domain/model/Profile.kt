package com.example.madclass01.domain.model

data class Profile(
    val nickname: String = "",
    val bio: String = "",
    val images: List<ImageItem> = emptyList()
)
