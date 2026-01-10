package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.model.Group
import com.example.madclass01.domain.repository.GroupRepository
import javax.inject.Inject

class CreateGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        iconType: String,
        tags: List<String>,
        isPublic: Boolean,
        userId: String
    ): Result<Group> = try {
        val group = groupRepository.createGroup(
            name = name,
            description = description,
            iconType = iconType,
            tags = tags,
            isPublic = isPublic,
            userId = userId
        )
        Result.success(group)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
