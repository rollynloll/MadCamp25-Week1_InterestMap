package com.example.madclass01.domain.model

/**
 * 그룹 채팅 메시지 도메인 모델
 */
data class ChatMessage(
    val id: String,
    val groupId: String,
    val userId: String?,
    val userName: String?,
    val type: MessageType,
    val content: String?,
    val imageUrl: String?,
    val userProfileImage: String? = null,
    val timestamp: Long,
    val readCount: Int = 0
) {
    enum class MessageType { TEXT, IMAGE, SYSTEM }
}

/**
 * 채팅방 메타 정보 (선택사항)
 */
data class ChatRoomMeta(
    val groupId: String,
    val groupName: String,
    val memberCount: Int
)
