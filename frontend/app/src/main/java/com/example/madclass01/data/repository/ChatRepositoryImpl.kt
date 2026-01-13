package com.example.madclass01.data.repository

import com.example.madclass01.data.remote.ApiService
import com.example.madclass01.data.remote.dto.MessageCreateRequest
import com.example.madclass01.data.remote.dto.toDomain
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
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ChatRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ChatRepository {

    override suspend fun joinGroup(groupId: String, userId: String): Result<Unit> {
        return try {
            val res = apiService.joinGroupChat(groupId, com.example.madclass01.data.remote.dto.AddMemberRequest(userId))
            if (res.isSuccessful && res.body() != null) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("join failed ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveGroup(groupId: String, userId: String): Result<Unit> {
        return try {
            val res = apiService.removeGroupMember(groupId, userId)
            if (res.isSuccessful) {
                Result.success(Unit)
            } else {
                val fallback = apiService.leaveGroupChat(groupId)
                if (fallback.isSuccessful && fallback.body()?.ok == true) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("leave failed ${res.code()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeGroupMessages(groupId: String): Flow<List<ChatMessage>> = flow {
        while (true) {
            val res = apiService.getGroupMessages(groupId, limit = 100, "")
            if (res.isSuccessful && res.body() != null) {
                val messages: List<ChatMessage> = res.body()!!.map { it.toDomain() }
                emit(messages.reversed()) // 최신 메시지가 아래로 가도록 역순 정렬
            }
            delay(3000) // 3초 폴링
        }
    }

    override suspend fun getRecentMessages(groupId: String, limit: Int): Result<List<ChatMessage>> {
        return try {
            val res = apiService.getGroupMessages(groupId, limit, "")
            if (res.isSuccessful && res.body() != null) {
                val messages: List<ChatMessage> = res.body()!!.map { it.toDomain() }
                Result.success(messages.reversed())
            } else {
                Result.failure(Exception("get messages failed ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendTextMessage(groupId: String, userId: String, content: String): Result<ChatMessage> {
        return try {
            val res = apiService.sendGroupMessage(groupId, MessageCreateRequest(userId = userId, text = content), "")
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
            // 임시 파일 생성 후 업로드
            val tmpFile = File.createTempFile("upload_", fileName)
            tmpFile.writeBytes(imageBytes)
            val reqFile: RequestBody = tmpFile.asRequestBody("image/*".toMediaTypeOrNull())
            val userIdBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, reqFile)
            val res = apiService.sendGroupImageMessage(groupId, userIdBody, part)
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
