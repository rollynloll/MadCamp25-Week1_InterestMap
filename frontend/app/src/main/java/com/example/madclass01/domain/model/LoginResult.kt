package com.example.madclass01.domain.model

data class LoginResult(
    val isSuccess: Boolean,
    val message: String,
    val token: String? = null
)
