package com.example.madclass01.domain.usecase

import com.example.madclass01.domain.model.LoginResult
import com.example.madclass01.domain.model.User
import com.example.madclass01.domain.repository.LoginRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val loginRepository: LoginRepository
) {
    suspend operator fun invoke(user: User): LoginResult {
        return loginRepository.login(user)
    }
}
