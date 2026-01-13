package com.example.madclass01.presentation.profile.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.core.UrlResolver
import com.example.madclass01.data.repository.ApiResult
import com.example.madclass01.data.repository.BackendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ProfileEditUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val uploadedProfileImageUrl: String? = null,
    val isUploadingImage: Boolean = false
)

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    /**
     * 단순히 텍스트 기반의 프로필 정보만 업데이트합니다.
     */
    fun updateProfile(
        userId: String,
        nickname: String,
        profileImageUrl: String?,
        age: Int?,
        region: String?,
        bio: String,
        tags: List<String>,
        keptImageUrls: List<String>,
        newImageFiles: List<File>
    ) {
        viewModelScope.launch {
            android.util.Log.d("ProfileEditViewModel", "저장 시작: Kept=${keptImageUrls.size}, New=${newImageFiles.size}")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)

            try {
                // 1. Delete Removed Photos
                val serverPhotosResult = backendRepository.getUserPhotos(userId)
                if (serverPhotosResult is ApiResult.Success) {
                    val serverPhotos = serverPhotosResult.data
                    val keptSet = keptImageUrls.toSet()
                    
                    val photosToDelete = serverPhotos.filter { photo ->
                        val resolvedUrl = UrlResolver.resolve(photo.fileUrl.ifBlank { photo.filePath })
                        resolvedUrl !in keptSet
                    }
                    
                    photosToDelete.forEach { photo ->
                        android.util.Log.d("ProfileEditViewModel", "Deleting photo: ${photo.id}")
                        backendRepository.deletePhoto(photo.id.toString()) 
                    }
                }

                // 2. Upload New Photos
                if (newImageFiles.isNotEmpty()) {
                    newImageFiles.forEach { file ->
                         android.util.Log.d("ProfileEditViewModel", "Uploading file: ${file.name}")
                         backendRepository.uploadPhoto(userId, file)
                    }
                }

                // 3. Update Text Profile & Profile Image
                val profileData = mapOf(
                    "age" to (age ?: ""),
                    "region" to (region ?: ""),
                    "bio" to bio,
                    "interests" to tags
                )

                val updateResult = backendRepository.updateUser(
                    userId = userId,
                    nickname = nickname,
                    profileImageUrl = profileImageUrl, // Explicitly pass profile image URL
                    profileData = profileData
                )
                
                android.util.Log.d("ProfileEditViewModel", "Update result: $updateResult")

                when (updateResult) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = updateResult.message)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }
    
    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
    
    /**
     * 프로필 사진만 업로드하고 URL을 받아옴니다.
     */
    fun uploadProfileImage(
        userId: String,
        imageFile: File
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingImage = true, error = null)
            
            // Use the new dedicated endpoint that avoids creating UserPhoto entries
            val uploadResult = backendRepository.uploadOnlyProfileImage(userId, imageFile)
            
            when (uploadResult) {
                is ApiResult.Success -> {
                    val photoUrl = uploadResult.data.profileImageUrl ?: ""
                    val resolvedPhotoUrl = UrlResolver.resolve(photoUrl)
                    android.util.Log.d("ProfileEditViewModel", "Profile image uploaded: $photoUrl")
                    
                    _uiState.value = _uiState.value.copy(
                        isUploadingImage = false,
                        uploadedProfileImageUrl = resolvedPhotoUrl
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUploadingImage = false,
                        error = uploadResult.message
                    )
                }
                else -> {}
            }
        }
    }
}
