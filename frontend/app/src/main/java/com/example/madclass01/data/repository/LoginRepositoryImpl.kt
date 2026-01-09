package com.example.madclass01.data.repository

import com.example.madclass01.domain.model.LoginResult
import com.example.madclass01.domain.model.User
import com.example.madclass01.domain.repository.LoginRepository
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor() : LoginRepository {
    
    override suspend fun login(user: User): LoginResult {
        return try {
            // 여기에서 실제 API 호출을 수행합니다
            if (validateEmail(user.email) && validatePassword(user.password)) {
                LoginResult(
                    isSuccess = true,
                    message = "로그인 성공",
                    token = "fake_token_${System.currentTimeMillis()}"
                )
            } else {
                LoginResult(
                    isSuccess = false,
                    message = "이메일 또는 비밀번호가 올바르지 않습니다"
                )
            }
        } catch (e: Exception) {
            LoginResult(
                isSuccess = false,
                message = "로그인 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }
    
    override suspend fun validateEmail(email: String): Boolean {
        // 간단한 이메일 검증
        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@(.+)$")
        return emailPattern.matches(email) && email.isNotEmpty()
    }
    
    override suspend fun validatePassword(password: String): Boolean {
        // 최소 6자 이상
        return password.length >= 6
    }
}
