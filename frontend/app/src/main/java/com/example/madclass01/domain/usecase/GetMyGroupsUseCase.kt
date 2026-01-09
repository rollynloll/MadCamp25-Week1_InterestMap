package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.model.Group
import javax.inject.Inject

class GetMyGroupsUseCase @Inject constructor() {
    suspend operator fun invoke(): List<Group> {
        // Mock data - 실제로는 Repository에서 가져옴
        return listOf(
            Group(
                id = "1",
                name = "서울 러너스",
                description = "매주 일요일 한강에서 모여요",
                memberCount = 24,
                activity = "활발함",
                tags = listOf(
                    com.example.madclass01.domain.model.Tag(id = "1", name = "러닝", category = "hobby"),
                    com.example.madclass01.domain.model.Tag(id = "2", name = "운동", category = "interest")
                ),
                lastActivityDate = "오늘",
                messageCount = 12,
                isJoined = true
            ),
            Group(
                id = "2",
                name = "감성 카페 탐방",
                description = "힙한 카페를 찾아다니는 모임",
                memberCount = 16,
                activity = "보통",
                tags = listOf(
                    com.example.madclass01.domain.model.Tag(id = "3", name = "카페", category = "hobby"),
                    com.example.madclass01.domain.model.Tag(id = "4", name = "사진", category = "interest")
                ),
                lastActivityDate = "2일 전",
                messageCount = 5,
                isJoined = true
            )
        )
    }
}
