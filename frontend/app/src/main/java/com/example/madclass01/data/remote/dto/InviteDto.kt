package com.example.madclass01.data.remote.dto

data class GenerateInviteLinkRequest(
    val groupId: String,
    val createdByUserId: String
)

data class InviteLinkResponse(
    val id: String,
    val groupId: String,
    val inviteUrl: String,
    val qrCodeData: String,
    val expiresAt: String,
    val createdAt: String,
    val maxUses: Int = 100,
    val currentUses: Int = 0
)

data class JoinByInviteLinkRequest(
    val inviteUrl: String,
    val userId: String
)

data class JoinByInviteLinkResponse(
    val success: Boolean,
    val groupId: String,
    val message: String
)
