package com.example.madclass01.data.repository

import com.example.madclass01.data.remote.ApiService
import com.example.madclass01.data.remote.dto.CreateGroupRequest
import com.example.madclass01.domain.model.Group
import com.example.madclass01.domain.model.Tag
import com.example.madclass01.domain.repository.GroupRepository
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : GroupRepository {
    
    override suspend fun createGroup(
        name: String,
        description: String,
        iconType: String,
        tags: List<String>,
        region: String?,
        imageUrl: String?,
        isPublic: Boolean,
        userId: String
    ): Group {
        val request = CreateGroupRequest(
            name = name,
            creatorId = userId,
            description = description.ifBlank { null },
            tags = tags,
            region = region,
            imageUrl = imageUrl,
            iconType = iconType,
            isPublic = isPublic
        )
        
        val response = apiService.createGroup(request)
        
        return if (response.isSuccessful && response.body() != null) {
            val groupResponse = response.body()!!
            Group(
                id = groupResponse.id,
                name = groupResponse.name,
                description = groupResponse.description ?: "",
                memberCount = groupResponse.memberIds.size,
                activity = "보통",
                tags = groupResponse.tags.map { Tag(id = it, name = it) },
                imageUrl = groupResponse.imageUrl,
                iconType = groupResponse.iconType,
                lastActivityDate = "",
                messageCount = 0,
                matchPercentage = 0,
                region = groupResponse.region,
                memberAge = "",
                isJoined = true
            )
        } else {
            throw Exception("Failed to create group: ${response.code()}")
        }
    }
}
