package com.example.madclass01.domain.repository

import com.example.madclass01.domain.model.LoginResult
import com.example.madclass01.domain.model.User

interface LoginRepository {
    suspend fun login(user: User): LoginResult
    suspend fun validateEmail(email: String): Boolean
    suspend fun validatePassword(password: String): Boolean
}
