package com.example.madclass01.core

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val storedToken = tokenManager.getToken()?.trim().takeUnless { it.isNullOrBlank() }
            ?: tokenManager.getUserId()?.trim().takeUnless { it.isNullOrBlank() }

        val existingAuthHeader = originalRequest.header("Authorization")?.trim()

        // If caller already supplied a proper Bearer header, keep it as-is.
        if (!existingAuthHeader.isNullOrBlank() && existingAuthHeader.startsWith("Bearer ", ignoreCase = true)) {
            return chain.proceed(originalRequest)
        }

        // If caller supplied an auth scheme like "Basic ...", keep it as-is.
        if (!existingAuthHeader.isNullOrBlank() && existingAuthHeader.contains(" ")) {
            return chain.proceed(originalRequest)
        }

        // If caller supplied a raw token (no scheme), normalize it to Bearer.
        if (!existingAuthHeader.isNullOrBlank()) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $existingAuthHeader")
                .build()
            return chain.proceed(newRequest)
        }

        // Otherwise, attach stored token if present.
        if (storedToken.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $storedToken")
            .build()

        return chain.proceed(newRequest)
    }
}
