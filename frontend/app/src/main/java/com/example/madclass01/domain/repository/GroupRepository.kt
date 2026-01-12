package com.example.madclass01.domain.repository

import com.example.madclass01.domain.model.Group

interface GroupRepository {
    suspend fun createGroup(
        name: String,
        description: String,
        iconType: String,
        tags: List<String>,
        region: String?,
        imageUrl: String?,
        isPublic: Boolean,
        userId: String
    ): Group
}
