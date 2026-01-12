package com.example.madclass01.presentation.profile.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        age: Int?,
        region: String?,
        bio: String,
        tags: List<String>
    ) {
        viewModelScope.launch {
            android.util.Log.d("ProfileEditViewModel", "저장눌림")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)

            val profileData = mapOf(
                "age" to (age ?: ""),
                "region" to (region ?: ""),
                "bio" to bio,
                "interests" to tags
            )

            // 텍스트 정보만 업데이트 (profileImageUrl은 null로 보내면 기존 값 유지되도록 처리)
            val updateResult = backendRepository.updateUser(
                userId = userId,
                nickname = nickname,
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
            
            val uploadResult = backendRepository.uploadPhoto(userId, imageFile)
            
            when (uploadResult) {
                is ApiResult.Success -> {
                    val photoUrl = uploadResult.data.fileUrl
                    android.util.Log.d("ProfileEditViewModel", "Profile image uploaded: $photoUrl")
                    
                    // URL을 프로필 이미지로 업데이트
                    val updateResult = backendRepository.updateUser(
                        userId = userId,
                        nickname = null,  // 닉네임은 변경하지 않음
                        profileImageUrl = photoUrl,
                        profileData = null
                    )
                    
                    when (updateResult) {
                        is ApiResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isUploadingImage = false,
                                uploadedProfileImageUrl = photoUrl
                            )
                        }
                        is ApiResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isUploadingImage = false,
                                error = updateResult.message
                            )
                        }
                        else -> {}
                    }
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