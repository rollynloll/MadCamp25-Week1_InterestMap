package com.example.madclass01.data.repository

import com.example.madclass01.data.remote.ApiService
import com.example.madclass01.data.remote.dto.MeResponse
import com.example.madclass01.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getMe(token: String): ApiResult<MeResponse> = withContext(Dispatchers.IO) {
        try {
            // token might be just userId in this project context, or a real JWT
            // The ApiService expects "Authorization" header. 
            // If it's a JWT, it should be "Bearer $token". 
            // If the backend treats userId as token (insecure dev mode), it might be just "$token" or "Bearer $token".
            // Based on LoginViewModel: loginToken = user.id.
            // Let's assume the backend expects the token as is or with Bearer. 
            // Safest bet for "Authorization" header is usually "Bearer <token>".
            // But if the backend simply checks the header value, I'll try sending just the token or Bearer + token.
            // Looking at ApiService.kt: @Header("Authorization") authorization: String
            // I'll assume "Bearer $token" is the standard way.
            
            val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
            
            val response = apiService.getMe(authHeader)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to fetch profile", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}
