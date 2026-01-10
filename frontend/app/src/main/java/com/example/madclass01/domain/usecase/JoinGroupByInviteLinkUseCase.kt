package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.repository.InviteRepository
import javax.inject.Inject

class JoinGroupByInviteLinkUseCase @Inject constructor(
    private val inviteRepository: InviteRepository
) {
    suspend operator fun invoke(
        inviteUrl: String,
        userId: String
    ): Result<Boolean> = try {
        inviteRepository.joinGroupByInviteLink(inviteUrl, userId)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
