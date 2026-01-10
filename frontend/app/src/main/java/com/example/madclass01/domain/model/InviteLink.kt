package com.example.madclass01.domain.model

data class InviteLink(
    val id: String = "",
    val groupId: String = "",
    val inviteUrl: String = "",
    val qrCodeData: String = "",
    val expiresAt: String = "",
    val createdAt: String = "",
    val maxUses: Int = 100,
    val currentUses: Int = 0
)
