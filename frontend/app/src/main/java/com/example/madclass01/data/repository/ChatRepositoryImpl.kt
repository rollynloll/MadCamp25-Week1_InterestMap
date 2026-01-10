package com.example.madclass01.data.repository

import com.example.madclass01.data.remote.ApiService
import com.example.madclass01.data.remote.dto.ChatMessageResponse
import com.example.madclass01.data.remote.dto.JoinLeaveRequest
import com.example.madclass01.data.remote.dto.SendChatMessageRequest
import com.example.madclass01.domain.model.ChatMessage
import com.example.madclass01.domain.repository.ChatRepository
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ChatRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ChatRepository {

    override suspend fun joinGroup(groupId: String, userId: String): Result<Unit> {
        return try {
            val res = apiService.joinGroupChat(groupId, JoinLeaveRequest(userId))
            if (res.isSuccessful) Result.success(Unit) else Result.failure(Exception("join failed ${res.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveGroup(groupId: String, userId: String): Result<Unit> {
        return try {
            val res = apiService.leaveGroupChat(groupId, JoinLeaveRequest(userId))
            if (res.isSuccessful) Result.success(Unit) else Result.failure(Exception("leave failed ${res.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeGroupMessages(groupId: String): Flow<List<ChatMessage>> = flow {
        while (true) {
            val res = apiService.getGroupMessages(groupId, limit = 100)
            if (res.isSuccessful && res.body() != null) {
                emit(res.body()!!.map(ChatMessageResponse::toDomain))
            }
            delay(5000) // 5초 폴링
        }
    }

    override suspend fun getRecentMessages(groupId: String, limit: Int): Result<List<ChatMessage>> {
        return try {
            val res = apiService.getGroupMessages(groupId, limit)
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!.map(ChatMessageResponse::toDomain))
            } else {
                Result.failure(Exception("get messages failed ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendTextMessage(groupId: String, userId: String, content: String): Result<ChatMessage> {
        return try {
            val res = apiService.sendGroupTextMessage(groupId, SendChatMessageRequest(groupId, userId, content))
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!.toDomain())
            } else {
                Result.failure(Exception("send text failed ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendImageMessage(groupId: String, userId: String, imageBytes: ByteArray, fileName: String): Result<ChatMessage> {
        return try {
            // 임시 파일 생성 후 업로드 (Compose에서 Uri를 바이트로 변환해 전달)
            val tmpFile = File.createTempFile("upload_", fileName)
            tmpFile.writeBytes(imageBytes)
            val reqFile: RequestBody = tmpFile.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, reqFile)
            val res = apiService.sendGroupImageMessage(groupId, userId, part)
            tmpFile.delete()
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!.toDomain())
            } else {
                Result.failure(Exception("send image failed ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
