package com.example.madclass01.data.remote.dto

import com.example.madclass01.domain.model.ChatMessage

// 요청 DTO

data class SendChatMessageRequest(
    val groupId: String,
    val userId: String,
    val content: String
)

data class JoinLeaveRequest(
    val userId: String
)

// 응답 DTO

data class ChatMessageResponse(
    val id: String,
    val groupId: String,
    val userId: String?,
    val userName: String?,
    val type: String,          // TEXT | IMAGE | SYSTEM
    val content: String?,
    val imageUrl: String?,
    val timestamp: Long,
    val readCount: Int = 0
) {
    fun toDomain(): ChatMessage = ChatMessage(
        id = id,
        groupId = groupId,
        userId = userId,
        userName = userName,
        type = ChatMessage.MessageType.valueOf(type),
        content = content,
        imageUrl = imageUrl,
        timestamp = timestamp,
        readCount = readCount
    )
}
