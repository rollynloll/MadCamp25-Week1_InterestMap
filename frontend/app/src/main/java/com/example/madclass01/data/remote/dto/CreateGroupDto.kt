package com.example.madclass01.data.remote.dto

data class CreateGroupFormRequest(
    val name: String,
    val description: String,
    val iconType: String,
    val tags: List<String>,
    val isPublic: Boolean,
    val userId: String
)

data class CreateGroupFormResponse(
    val id: String,
    val name: String,
    val description: String,
    val iconType: String,
    val tags: List<String>,
    val isPublic: Boolean,
    val userId: String,
    val createdAt: String,
    val memberCount: Int = 1
)
