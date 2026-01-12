package com.example.madclass01.data.repository

import com.example.madclass01.data.remote.ApiService
import com.example.madclass01.data.remote.dto.GenerateInviteLinkRequest
import com.example.madclass01.data.remote.dto.JoinByInviteLinkRequest
import com.example.madclass01.domain.model.InviteLink
import com.example.madclass01.domain.repository.InviteRepository
import javax.inject.Inject

class InviteRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : InviteRepository {
    
    override suspend fun generateInviteLink(
        groupId: String,
        createdByUserId: String
    ): InviteLink {
        // TODO: Backend API 구현 시 실제 호출로 변경
        // 현재는 Deep Link 형식의 초대 링크를 직접 생성
        
        // Deep Link URL 생성 (madcamp:// 스키마 사용)
        val deepLinkUrl = "madcamp://invite/$groupId"
        
        // 대체 HTTPS URL (웹 브라우저에서도 작동)
        val httpsUrl = "https://madcamp.app/invite/$groupId"
        
        // 현재 시간
        val currentTime = System.currentTimeMillis()
        val expiryTime = currentTime + 24 * 60 * 60 * 1000 // 24시간 후
        
        return InviteLink(
            id = "invite_$currentTime",
            groupId = groupId,
            inviteUrl = deepLinkUrl,  // Deep Link 형식으로 변경
            qrCodeData = httpsUrl,    // QR 코드는 HTTPS URL 사용 (더 범용적)
            expiresAt = expiryTime.toString(),
            createdAt = currentTime.toString(),
            maxUses = 100,
            currentUses = 0
        )
        
        /* Backend API 구현 시 사용할 코드:
        val request = GenerateInviteLinkRequest(
            groupId = groupId,
            createdByUserId = createdByUserId
        )
        
        val response = apiService.generateInviteLink(request)
        
        return if (response.isSuccessful && response.body() != null) {
            val linkResponse = response.body()!!
            InviteLink(
                id = linkResponse.id,
                groupId = linkResponse.groupId,
                inviteUrl = linkResponse.inviteUrl,
                qrCodeData = linkResponse.qrCodeData,
                expiresAt = linkResponse.expiresAt,
                createdAt = linkResponse.createdAt,
                maxUses = linkResponse.maxUses,
                currentUses = linkResponse.currentUses
            )
        } else {
            throw Exception("Failed to generate invite link: ${response.code()}")
        }
        */
    }
    
    override suspend fun getInviteLink(groupId: String): InviteLink {
        // TODO: Backend API 구현 시 실제 호출로 변경
        // 현재는 generateInviteLink와 동일한 로직 사용
        return generateInviteLink(groupId, "currentUser")
        
        /* Backend API 구현 시 사용할 코드:
        val response = apiService.getInviteLink(groupId)
        
        return if (response.isSuccessful && response.body() != null) {
            val linkResponse = response.body()!!
            InviteLink(
                id = linkResponse.id,
                groupId = linkResponse.groupId,
                inviteUrl = linkResponse.inviteUrl,
                qrCodeData = linkResponse.qrCodeData,
                expiresAt = linkResponse.expiresAt,
                createdAt = linkResponse.createdAt,
                maxUses = linkResponse.maxUses,
                currentUses = linkResponse.currentUses
            )
        } else {
            throw Exception("Failed to get invite link: ${response.code()}")
        }
        */
    }
    
    override suspend fun joinGroupByInviteLink(
        inviteUrl: String,
        userId: String
    ): Result<Boolean> = try {
        // Deep Link에서 groupId 추출
        // Format: madcamp://invite/{groupId} 또는 https://madcamp.app/invite/{groupId}
        val groupId = extractGroupIdFromInviteUrl(inviteUrl)
            ?: return Result.failure(Exception("Invalid invite URL format"))
        
        // 기존 join group API 사용 (인증 헤더 불필요한 버전)
        val response = apiService.joinGroupChat(
            groupId,
            com.example.madclass01.data.remote.dto.AddMemberRequest(userId)
        )
        
        if (response.isSuccessful && response.body() != null) {
            Result.success(true)
        } else {
            Result.failure(Exception("Failed to join group: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    private fun extractGroupIdFromInviteUrl(inviteUrl: String): String? {
        return try {
            // madcamp://invite/{groupId} 형식
            if (inviteUrl.startsWith("madcamp://invite/")) {
                inviteUrl.substringAfter("madcamp://invite/")
            }
            // https://madcamp.app/invite/{groupId} 형식
            else if (inviteUrl.contains("/invite/")) {
                inviteUrl.substringAfter("/invite/")
            }
            else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
