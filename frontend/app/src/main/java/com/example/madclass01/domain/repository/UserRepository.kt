package com.example.madclass01.domain.repository

import com.example.madclass01.data.remote.dto.MeResponse
import com.example.madclass01.data.repository.ApiResult

interface UserRepository {
    suspend fun getMe(token: String): ApiResult<MeResponse>
}
