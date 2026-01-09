package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.model.Group
import javax.inject.Inject

class SearchGroupsUseCase @Inject constructor() {
    suspend operator fun invoke(query: String, filters: Map<String, Any>): List<Group> {
        // Mock data - 실제로는 Repository에서 검색
        return listOf(
            Group(
                id = "3",
                name = "서울 러닝 크루",
                description = "매주 일요일 한강에서 모여요",
                memberCount = 48,
                activity = "활발함",
                tags = listOf(
                    com.example.madclass01.domain.model.Tag(id = "1", name = "러닝", category = "hobby"),
                    com.example.madclass01.domain.model.Tag(id = "2", name = "운동", category = "interest"),
                    com.example.madclass01.domain.model.Tag(id = "3", name = "건강", category = "interest")
                ),
                matchPercentage = 92,
                region = "서울",
                memberAge = "20-30대",
                isJoined = false
            ),
            Group(
                id = "4",
                name = "감성 카페 탐방러",
                description = "힙한 카페를 찾아다니는 모임",
                memberCount = 32,
                activity = "보통",
                tags = listOf(
                    com.example.madclass01.domain.model.Tag(id = "4", name = "카페", category = "hobby"),
                    com.example.madclass01.domain.model.Tag(id = "5", name = "사진", category = "interest"),
                    com.example.madclass01.domain.model.Tag(id = "6", name = "감성", category = "interest")
                ),
                matchPercentage = 85,
                region = "서울",
                memberAge = "20-30대",
                isJoined = false
            ),
            Group(
                id = "5",
                name = "필름 사진 동호회",
                description = "아날로그 감성을 사랑하는 사람들",
                memberCount = 24,
                activity = "조용함",
                tags = listOf(
                    com.example.madclass01.domain.model.Tag(id = "7", name = "사진", category = "hobby"),
                    com.example.madclass01.domain.model.Tag(id = "8", name = "필름", category = "interest"),
                    com.example.madclass01.domain.model.Tag(id = "9", name = "예술", category = "interest")
                ),
                matchPercentage = 78,
                region = "전국",
                memberAge = "전체",
                isJoined = false
            )
        )
    }
}
