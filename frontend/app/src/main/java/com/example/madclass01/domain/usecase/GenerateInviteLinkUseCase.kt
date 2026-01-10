package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.model.InviteLink
import com.example.madclass01.domain.repository.InviteRepository
import javax.inject.Inject

class GenerateInviteLinkUseCase @Inject constructor(
    private val inviteRepository: InviteRepository
) {
    suspend operator fun invoke(
        groupId: String,
        createdByUserId: String
    ): Result<InviteLink> = try {
        val inviteLink = inviteRepository.generateInviteLink(groupId, createdByUserId)
        Result.success(inviteLink)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
