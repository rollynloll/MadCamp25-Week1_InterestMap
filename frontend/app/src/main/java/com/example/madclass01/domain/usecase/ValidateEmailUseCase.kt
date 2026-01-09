package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.repository.LoginRepository
import javax.inject.Inject

class ValidateEmailUseCase @Inject constructor(
    private val loginRepository: LoginRepository
) {
    suspend operator fun invoke(email: String): Boolean {
        return loginRepository.validateEmail(email)
    }
}
