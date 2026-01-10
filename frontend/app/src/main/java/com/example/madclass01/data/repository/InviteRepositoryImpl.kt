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
    }
    
    override suspend fun getInviteLink(groupId: String): InviteLink {
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
    }
    
    override suspend fun joinGroupByInviteLink(
        inviteUrl: String,
        userId: String
    ): Result<Boolean> = try {
        val request = JoinByInviteLinkRequest(
            inviteUrl = inviteUrl,
            userId = userId
        )
        
        val response = apiService.joinByInviteLink(request)
        
        if (response.isSuccessful && response.body() != null) {
            val result = response.body()!!
            Result.success(result.success)
        } else {
            Result.failure(Exception("Failed to join group: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
