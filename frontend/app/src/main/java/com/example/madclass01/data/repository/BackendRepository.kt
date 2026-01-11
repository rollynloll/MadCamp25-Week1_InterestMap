package com.example.madclass01.data.repository

import android.content.Context
import com.example.madclass01.data.remote.ApiService
import com.example.madclass01.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}

@Singleton
class BackendRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    suspend fun healthCheck(): ApiResult<HealthCheckResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getHealth()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Health check failed", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
    
    // Test용 APIs (개발 단계에서 사용)
    suspend fun createTestUser(
        provider: String,
        providerUserId: String,
        nickname: String? = null
    ): ApiResult<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreateUserRequest(
                provider = provider,
                providerUserId = providerUserId,
                nickname = nickname,
                profileImageUrl = null,
                profileData = null
            )
            val response = apiService.createTestUser(request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to create user", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error: ${e.javaClass.simpleName}")
        }
    }
    
    suspend fun getTestUser(userId: String): ApiResult<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTestUser(userId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to get user", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error: ${e.javaClass.simpleName}")
        }
    }
    
    suspend fun createUser(
        provider: String,
        providerUserId: String,
        nickname: String? = null,
        profileImageUrl: String? = null,
        profileData: Map<String, Any>? = null
    ): ApiResult<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreateUserRequest(
                provider = provider,
                providerUserId = providerUserId,
                nickname = nickname,
                profileImageUrl = profileImageUrl,
                profileData = profileData
            )
            val response = apiService.createUser(request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to create user", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error: ${e.javaClass.simpleName}")
        }
    }
    
    suspend fun getUser(userId: String): ApiResult<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUser(userId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to get user", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error: ${e.javaClass.simpleName}")
        }
    }
    
    suspend fun updateUser(
        userId: String,
        nickname: String? = null,
        profileImageUrl: String? = null,
        profileData: Map<String, Any>? = null
    ): ApiResult<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateUserRequest(
                nickname = nickname,
                profileImageUrl = profileImageUrl,
                profileData = profileData
            )
            val response = apiService.updateUser(userId, request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to update user", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun uploadPhoto(
        userId: String,
        file: File
    ): ApiResult<PhotoResponse> = withContext(Dispatchers.IO) {
        try {
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val userIdBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.uploadPhoto(userIdBody, multipartBody)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to upload photo", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * 다중 사진 업로드 (리사이징 + WebP 압축)
     */
    suspend fun uploadPhotos(
        userId: String,
        files: List<File>
    ): ApiResult<List<PhotoResponse>> = withContext(Dispatchers.IO) {
        try {
            val multipartParts = files.mapIndexed { index, file ->
                val requestBody = file.asRequestBody("image/webp".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("files", "photo_${index}.webp", requestBody)
            }
            val userIdBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.uploadPhotos(userIdBody, multipartParts)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to upload photos", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * 이미지 최적화 (리사이징 + WebP 압축 80%)
     */
    suspend fun optimizeImage(
        context: Context,
        imageUri: android.net.Uri,
        maxWidth: Int = 1920,
        maxHeight: Int = 1920
    ): File? = withContext(Dispatchers.IO) {
        try {
            val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, imageUri)
                android.graphics.ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }

            // 리사이징
            val ratio = minOf(
                maxWidth.toFloat() / bitmap.width,
                maxHeight.toFloat() / bitmap.height,
                1.0f
            )
            val resizedBitmap = if (ratio < 1.0f) {
                android.graphics.Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * ratio).toInt(),
                    (bitmap.height * ratio).toInt(),
                    true
                )
            } else {
                bitmap
            }

            // WebP로 압축 (80%)
            val tempFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.webp")
            tempFile.outputStream().use { out ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP_LOSSY, 80, out)
                } else {
                    @Suppress("DEPRECATION")
                    resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP, 80, out)
                }
            }

            if (resizedBitmap != bitmap) resizedBitmap.recycle()
            bitmap.recycle()

            tempFile
        } catch (e: Exception) {
            android.util.Log.e("BackendRepository", "Image optimization failed", e)
            null
        }
    }
    
    suspend fun getUserPhotos(userId: String): ApiResult<List<PhotoResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserPhotos(userId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to get user photos", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun createGroup(
        name: String,
        creatorId: String,
        description: String? = null
    ): ApiResult<GroupResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreateGroupRequest(
                name = name,
                creatorId = creatorId,
                description = description
            )
            val response = apiService.createGroup(request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to create group", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun getGroup(groupId: String): ApiResult<GroupResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getGroup(groupId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to get group", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun getUserGroups(userId: String): ApiResult<List<GroupResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserGroups(userId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to get user groups", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun addGroupMember(
        groupId: String,
        userId: String
    ): ApiResult<GroupResponse> = withContext(Dispatchers.IO) {
        try {
            val request = AddMemberRequest(userId = userId)
            val response = apiService.addGroupMember(groupId, request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to add group member", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
    
    // ==================== Image Analysis APIs ====================
    
    suspend fun analyzeImages(
        userId: String,
        imageUrls: List<String>
    ): ApiResult<ImageAnalysisResponse> = withContext(Dispatchers.IO) {
        try {
            val request = ImageAnalysisRequest(
                userId = userId,
                imageUrls = imageUrls
            )
            val response = apiService.analyzeImages(request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to analyze images", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error: ${e.javaClass.simpleName}")
        }
    }
    
    suspend fun generateEmbedding(
        userId: String,
        nickname: String,
        age: Int?,
        region: String?,
        bio: String?,
        tags: List<String>,
        imageKeywords: List<String>
    ): ApiResult<EmbeddingResponse> = withContext(Dispatchers.IO) {
        try {
            val request = GenerateEmbeddingRequest(
                userId = userId,
                nickname = nickname,
                age = age,
                region = region,
                bio = bio,
                tags = tags,
                imageKeywords = imageKeywords
            )
            val response = apiService.generateEmbedding(request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to generate embedding", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error: ${e.javaClass.simpleName}")
        }
    }
}
