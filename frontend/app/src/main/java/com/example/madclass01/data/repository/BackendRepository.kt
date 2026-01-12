package com.example.madclass01.data.repository

import android.content.Context
import com.example.madclass01.data.remote.ApiService
import com.example.madclass01.data.remote.dto.*
import com.google.gson.Gson
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
    private val gson = Gson()

    /**
     * 카카오 SDK access token을 백엔드에 전달해 우리 JWT(access_token)를 발급받는다.
     * Backend: POST /auth/kakao {"access_token": "..."}
     */
    suspend fun kakaoLogin(kakaoAccessToken: String): ApiResult<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.kakaoLogin(KakaoAuthRequest(accessToken = kakaoAccessToken))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                val errorText = try {
                    response.errorBody()?.string()
                } catch (_: Exception) {
                    null
                }
                val msg = buildString {
                    append("Failed to login with Kakao")
                    if (!errorText.isNullOrBlank()) {
                        append(": ")
                        append(errorText)
                    }
                }
                ApiResult.Error(msg, response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error: ${e.javaClass.simpleName}")
        }
    }
    
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
    
    /**
     * 사진과 함께 유저 생성 (사진 배치 업로드 후 유저 생성)
     */
    suspend fun createUserWithPhotos(
        context: Context,
        provider: String,
        providerUserId: String,
        nickname: String? = null,
        profileData: Map<String, Any>? = null,
        photoUris: List<android.net.Uri>
    ): ApiResult<UserResponse> = withContext(Dispatchers.IO) {
        try {
            // 1. 먼저 유저를 생성 (사진 없이)
            val createResult = createUser(provider, providerUserId, nickname, null, profileData)
            
            if (createResult !is ApiResult.Success) {
                return@withContext createResult
            }
            
            val userId = createResult.data.id
            
            // 2. 사진들을 최적화
            val optimizedFiles = photoUris.mapNotNull { uri ->
                optimizeImage(context, uri)
            }
            
            if (optimizedFiles.isEmpty()) {
                return@withContext createResult
            }
            
            // 3. 배치로 사진 업로드
            val uploadResult = uploadPhotos(userId, optimizedFiles)
            
            // 4. 임시 파일 삭제
            optimizedFiles.forEach { it.delete() }
            
            if (uploadResult !is ApiResult.Success) {
                return@withContext ApiResult.Error("User created but photo upload failed: ${(uploadResult as? ApiResult.Error)?.message}")
            }

            // 5. 첫 번째 사진을 프로필 이미지로 설정
            val firstPhotoUrl = uploadResult.data.photos.firstOrNull()?.fileUrl
            if (firstPhotoUrl != null) {
                updateUser(userId, profileImageUrl = firstPhotoUrl)
            } else {
                createResult
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
    
    /**
     * 사진과 함께 유저 프로필 업데이트 (사진 배치 업로드 후 프로필 업데이트)
     */
    suspend fun updateUserWithPhotos(
        context: Context,
        userId: String,
        nickname: String? = null,
        profileData: Map<String, Any>? = null,
        photoUris: List<android.net.Uri>
    ): ApiResult<UserResponse> = withContext(Dispatchers.IO) {
        try {
            // 1. 사진들을 최적화
            val optimizedFiles = photoUris.mapNotNull { uri ->
                optimizeImage(context, uri)
            }
            
            var profileImageUrl: String? = null
            
            // 2. 사진이 있으면 배치로 업로드
            if (optimizedFiles.isNotEmpty()) {
                val uploadResult = uploadPhotos(userId, optimizedFiles)
                
                // 임시 파일 삭제
                optimizedFiles.forEach { it.delete() }
                
                if (uploadResult is ApiResult.Success) {
                    // 첫 번째 사진을 프로필 이미지로 사용
                    profileImageUrl = uploadResult.data.photos.firstOrNull()?.fileUrl
                }
            }
            
            // 3. 프로필 업데이트
            updateUser(userId, nickname, profileImageUrl, profileData)
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
        files: List<File>,
        selectedTags: List<String> = emptyList()
    ): ApiResult<BatchPhotoUploadResponse> = withContext(Dispatchers.IO) {
        try {
            val multipartParts = files.mapIndexed { index, file ->
                val requestBody = file.asRequestBody("image/webp".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("files", "photo_${index}.webp", requestBody)
            }
            val userIdBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())
            val tagsJson = gson.toJson(selectedTags)
            val tagsBody = tagsJson.toRequestBody("application/json".toMediaTypeOrNull())

            val response = apiService.uploadPhotos(userIdBody, multipartParts, tagsBody)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                android.util.Log.e("BackendRepository", "Upload failed: code=${response.code()}, body=$errorBody")
                ApiResult.Error("Failed to upload photos: $errorBody", response.code())
            }
        } catch (e: Exception) {
            android.util.Log.e("BackendRepository", "Upload exception", e)
            ApiResult.Error(e.message ?: "Network error: ${e.javaClass.simpleName}")
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
    
    suspend fun getAllGroups(): ApiResult<List<GroupResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllGroups()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to get groups", response.code())
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

    suspend fun uploadGroupProfileImage(
        groupId: String,
        file: File
    ): ApiResult<GroupResponse> = withContext(Dispatchers.IO) {
        try {
            val requestBody = file.asRequestBody("image/webp".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val response = apiService.uploadGroupProfileImage(groupId, multipartBody)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to upload group profile image", response.code())
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
    
    // ==================== 그룹 채팅 관련 ====================
    
    /**
     * 그룹 채팅 메시지 목록 조회
     */
    suspend fun getGroupMessages(
        groupId: String,
        limit: Int = 50
    ): ApiResult<List<MessageContent>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getGroupMessages(groupId, limit, "")
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to get messages", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
    
    /**
     * 그룹 채팅 텍스트 메시지 전송
     */
    suspend fun sendGroupMessage(
        groupId: String,
        text: String
    ): ApiResult<MessageContent> = withContext(Dispatchers.IO) {
        try {
            val request = MessageCreateRequest(text = text)
            val response = apiService.sendGroupMessage(groupId, request, "")
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to send message", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
    
    /**
     * 그룹 채팅 이미지 메시지 전송
     */
    suspend fun sendGroupImageMessage(
        groupId: String,
        userId: String,
        imageFile: File
    ): ApiResult<GroupMessageItem> = withContext(Dispatchers.IO) {
        try {
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)
            
            val response = apiService.sendGroupImageMessage(groupId, userId, multipartBody)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to send image", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}
