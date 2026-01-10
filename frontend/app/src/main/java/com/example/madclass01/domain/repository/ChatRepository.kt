package com.example.madclass01.domain.repository

import com.example.madclass01.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * 그룹 채팅 Repository 인터페이스
 */
interface ChatRepository {
    /** 그룹 채팅방 입장 (서버에 이벤트 기록) */
    suspend fun joinGroup(groupId: String, userId: String): Result<Unit>

    /** 그룹 채팅방 퇴장 (서버에 이벤트 기록) */
    suspend fun leaveGroup(groupId: String, userId: String): Result<Unit>

    /** 그룹 채팅 메시지 스트림 관찰 (폴링 기반) */
    fun observeGroupMessages(groupId: String): Flow<List<ChatMessage>>

    /** 최근 메시지 가져오기 (초기 로드) */
    suspend fun getRecentMessages(groupId: String, limit: Int = 50): Result<List<ChatMessage>>

    /** 텍스트 메시지 전송 */
    suspend fun sendTextMessage(groupId: String, userId: String, content: String): Result<ChatMessage>

    /** 이미지 메시지 전송 (사진 업로드) */
    suspend fun sendImageMessage(groupId: String, userId: String, imageBytes: ByteArray, fileName: String): Result<ChatMessage>
}
