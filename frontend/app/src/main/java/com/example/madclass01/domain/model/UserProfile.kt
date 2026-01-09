package com.example.madclass01.domain.model

data class UserProfile(
    val nickname: String = "",
    val age: Int = 0,
    val region: String = "",
    val hobbies: List<Tag> = emptyList(),
    val interests: List<Tag> = emptyList(),
    val images: List<ImageItem> = emptyList(),
    val selectedTags: List<Tag> = emptyList()
)
