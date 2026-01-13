package com.example.madclass01.data.remote.dto

import com.example.madclass01.core.UrlResolver
import com.example.madclass01.domain.model.ChatMessage
import com.google.gson.annotations.SerializedName

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

// 백엔드 /groups/{groupId}/messages 응답 구조
data class GroupMessagesResponse(
    val items: List<GroupMessageItem>,
    @SerializedName("next_before") val nextBefore: String?
)

data class GroupMessageItem(
    val id: String,
    @SerializedName("group_id") val groupId: String,
    val sender: MessageSender,
    val content: GroupMessageContent,
    @SerializedName("created_at") val createdAt: String
) {
    fun toDomain(): ChatMessage {
        val timestamp = try {
            java.time.Instant.parse(createdAt).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
        
        return ChatMessage(
            id = id,
            groupId = groupId,
            userId = sender.userId,
            userName = sender.nickname,
            type = if (content.text != null) ChatMessage.MessageType.TEXT else ChatMessage.MessageType.SYSTEM,
            content = content.text ?: "",
            imageUrl = null,
            timestamp = timestamp,
            readCount = 0
        )
    }
}

data class MessageSender(
    @SerializedName("user_id") val userId: String,
    val nickname: String,
    @SerializedName("primary_photo_url") val primaryPhotoUrl: String?
)

data class GroupMessageContent(
    val text: String?
)

// Legacy DTO (이전 구현용, 향후 제거 가능)
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
        imageUrl = UrlResolver.resolve(imageUrl),
        timestamp = timestamp,
        readCount = readCount
    )
}
