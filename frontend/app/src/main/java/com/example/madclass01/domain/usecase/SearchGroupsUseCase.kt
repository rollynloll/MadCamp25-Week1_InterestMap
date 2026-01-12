package com.example.madclass01.domain.usecase

import com.example.madclass01.data.remote.dto.GroupResponse
import com.example.madclass01.data.repository.ApiResult
import com.example.madclass01.data.repository.BackendRepository
import com.example.madclass01.domain.model.Group
import com.example.madclass01.domain.model.Tag
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

class SearchGroupsUseCase @Inject constructor(
    private val backendRepository: BackendRepository
) {
    suspend operator fun invoke(query: String, filters: Map<String, Any>): List<Group> {
        val result = backendRepository.getAllGroups()
        val groups = when (result) {
            is ApiResult.Success -> result.data.map { it.toDomain() }
            is ApiResult.Error -> throw IllegalStateException(result.message)
            is ApiResult.Loading -> emptyList()
        }

        val trimmedQuery = query.trim()
        val filtered = if (trimmedQuery.isBlank()) {
            groups
        } else {
            groups.filter { it.name.contains(trimmedQuery, ignoreCase = true) }
        }

        val collator = Collator.getInstance(Locale.KOREAN)
        return filtered.sortedWith { a, b -> collator.compare(a.name, b.name) }
    }

    private fun GroupResponse.toDomain(): Group {
        return Group(
            id = id,
            name = name,
            description = description ?: "",
            memberCount = memberIds.size,
            tags = tags.map { Tag(id = it, name = it) },
            imageUrl = imageUrl,
            region = region
        )
    }
}
