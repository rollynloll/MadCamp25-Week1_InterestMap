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

        val publicGroups = groups.filter { it.isPublic }

        val trimmedQuery = query.trim()
        val filteredByQuery = if (trimmedQuery.isBlank()) {
            publicGroups
        } else {
            publicGroups.filter { group ->
                val inName = group.name.contains(trimmedQuery, ignoreCase = true)
                val inDesc = group.description.contains(trimmedQuery, ignoreCase = true)
                val inTags = group.tags.any { it.name.contains(trimmedQuery, ignoreCase = true) }
                inName || inDesc || inTags
            }
        }

        val selectedRegion = filters["region"] as? String ?: "전체"
        val selectedMemberRange = filters["memberRange"] as? String ?: "전체"

        val filteredByRegion = if (selectedRegion == "전체") {
            filteredByQuery
        } else {
            filteredByQuery.filter { it.region == selectedRegion }
        }

        val filteredByMembers = when (selectedMemberRange) {
            "10명 이하" -> filteredByRegion.filter { it.memberCount <= 10 }
            "10-30명" -> filteredByRegion.filter { it.memberCount in 10..30 }
            "30명 이상" -> filteredByRegion.filter { it.memberCount >= 30 }
            else -> filteredByRegion
        }

        val collator = Collator.getInstance(Locale.KOREAN)
        return filteredByMembers.sortedWith { a, b -> collator.compare(a.name, b.name) }
    }

    private fun GroupResponse.toDomain(): Group {
        return Group(
            id = id,
            name = name,
            description = description ?: "",
            memberCount = memberIds.size,
            tags = tags.map { Tag(id = it, name = it) },
            imageUrl = imageUrl,
            iconType = iconType,
            region = region,
            isPublic = isPublic
        )
    }
}
