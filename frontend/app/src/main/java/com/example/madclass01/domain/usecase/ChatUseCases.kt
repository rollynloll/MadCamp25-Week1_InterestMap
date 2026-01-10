package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.model.ChatMessage
import com.example.madclass01.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGroupChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(groupId: String): Flow<List<ChatMessage>> =
        chatRepository.observeGroupMessages(groupId)
}

class SendGroupTextMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(groupId: String, userId: String, content: String): Result<ChatMessage> =
        chatRepository.sendTextMessage(groupId, userId, content)
}

class SendGroupImageMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(groupId: String, userId: String, imageBytes: ByteArray, fileName: String): Result<ChatMessage> =
        chatRepository.sendImageMessage(groupId, userId, imageBytes, fileName)
}

class JoinGroupChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(groupId: String, userId: String): Result<Unit> =
        chatRepository.joinGroup(groupId, userId)
}

class LeaveGroupChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(groupId: String, userId: String): Result<Unit> =
        chatRepository.leaveGroup(groupId, userId)
}
