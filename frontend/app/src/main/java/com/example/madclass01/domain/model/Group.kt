package com.example.madclass01.domain.model

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val memberCount: Int = 0,
    val activity: String = "", // "활발함", "보통", "조용함"
    val tags: List<Tag> = emptyList(),
    val imageUrl: String = "",
    val iconType: String = "",
    val lastActivityDate: String = "",
    val messageCount: Int = 0,
    val matchPercentage: Int = 0, // 매칭도 (0-100)
    val region: String = "",
    val memberAge: String = "",
    val isJoined: Boolean = false
)
