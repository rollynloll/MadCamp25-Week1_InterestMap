package com.example.madclass01.domain.repository

import com.example.madclass01.domain.model.InviteLink

interface InviteRepository {
    suspend fun generateInviteLink(
        groupId: String,
        createdByUserId: String
    ): InviteLink
    
    suspend fun getInviteLink(groupId: String): InviteLink
    
    suspend fun joinGroupByInviteLink(
        inviteUrl: String,
        userId: String
    ): Result<Boolean>
}
