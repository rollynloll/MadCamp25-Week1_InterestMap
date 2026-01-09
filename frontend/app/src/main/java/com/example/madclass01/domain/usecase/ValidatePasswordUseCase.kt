package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.repository.LoginRepository
import javax.inject.Inject

class ValidatePasswordUseCase @Inject constructor(
    private val loginRepository: LoginRepository
) {
    suspend operator fun invoke(password: String): Boolean {
        return loginRepository.validatePassword(password)
    }
}
