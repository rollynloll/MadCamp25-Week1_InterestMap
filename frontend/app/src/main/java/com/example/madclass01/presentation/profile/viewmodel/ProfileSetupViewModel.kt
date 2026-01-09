package com.example.madclass01.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.data.repository.ApiResult
import com.example.madclass01.data.repository.BackendRepository
import com.example.madclass01.domain.model.ImageItem
import com.example.madclass01.domain.model.Tag
import com.example.madclass01.domain.usecase.AddImageUseCase
import com.example.madclass01.domain.usecase.AddTagUseCase
import com.example.madclass01.domain.usecase.RemoveImageUseCase
import com.example.madclass01.domain.usecase.RemoveTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileSetupUiState(
    val nickname: String = "",
    val age: Int = 0,
    val region: String = "",
    val bio: String = "",
    val images: List<ImageItem> = emptyList(),
    val hobbies: List<Tag> = emptyList(),
    val interests: List<Tag> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isProfileComplete: Boolean = false,
    val nicknameError: String = "",
    val imageCountText: String = "0/20",
    val userId: String? = null  // 백엔드 userId 저장
)

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val addImageUseCase: AddImageUseCase,
    private val removeImageUseCase: RemoveImageUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val removeTagUseCase: RemoveTagUseCase,
    private val backendRepository: BackendRepository  // 백엔드 추가
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()
    
    /**
     * 로그인 후 userId 설정
     */
    fun setUserId(userId: String) {
        android.util.Log.d("ProfileSetupViewModel", "setUserId 호출됨: $userId")
        _uiState.value = _uiState.value.copy(userId = userId)
    }
    
    fun updateNickname(newNickname: String) {
        _uiState.value = _uiState.value.copy(
            nickname = newNickname,
            nicknameError = ""
        )
    }
    
    fun updateAge(newAge: Int) {
        _uiState.value = _uiState.value.copy(age = newAge)
    }
    
    fun updateRegion(newRegion: String) {
        _uiState.value = _uiState.value.copy(region = newRegion)
    }
    
    fun updateBio(newBio: String) {
        if (newBio.length <= 500) {
            _uiState.value = _uiState.value.copy(bio = newBio)
        }
    }
    
    fun addImage(imageUri: String, imageName: String = "", imageSize: Long = 0L) {
        android.util.Log.d("ProfileSetupViewModel", "addImage 호출 - URI: $imageUri, 현재 이미지 수: ${_uiState.value.images.size}")
        viewModelScope.launch {
            val currentState = _uiState.value
            val imageItem = ImageItem(uri = imageUri, name = imageName, size = imageSize)
            
            val (isSuccess, updatedImages) = addImageUseCase(imageItem, currentState.images)
            
            android.util.Log.d("ProfileSetupViewModel", "addImageUseCase 결과 - isSuccess: $isSuccess, 업데이트된 이미지 수: ${updatedImages.size}")
            
            if (isSuccess) {
                _uiState.value = currentState.copy(
                    images = updatedImages,
                    imageCountText = "${updatedImages.size}/20"
                )
                android.util.Log.d("ProfileSetupViewModel", "이미지 추가 성공! 총 ${updatedImages.size}개")
            } else {
                if (currentState.images.size >= 20) {
                    android.util.Log.w("ProfileSetupViewModel", "이미지 최대 개수 도달")
                    _uiState.value = currentState.copy(
                        errorMessage = "최대 20개의 이미지만 선택할 수 있습니다"
                    )
                } else {
                    android.util.Log.e("ProfileSetupViewModel", "이미지 추가 실패 - 이유 불명")
                }
            }
        }
    }
    
    fun removeImage(imageUri: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val updatedImages = removeImageUseCase(imageUri, currentState.images)
            
            _uiState.value = currentState.copy(
                images = updatedImages,
                imageCountText = "${updatedImages.size}/20",
                errorMessage = ""
            )
        }
    }
    
    fun addHobby(hobbyName: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val (isSuccess, updatedTags) = addTagUseCase(hobbyName, "hobby", currentState.hobbies)
            
            if (isSuccess) {
                _uiState.value = currentState.copy(hobbies = updatedTags)
            } else {
                _uiState.value = currentState.copy(
                    errorMessage = "동일한 태그가 이미 존재합니다"
                )
            }
        }
    }
    
    fun removeHobby(tagId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val updatedHobbies = removeTagUseCase(tagId, currentState.hobbies)
            _uiState.value = currentState.copy(hobbies = updatedHobbies)
        }
    }
    
    fun addInterest(interestName: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val (isSuccess, updatedTags) = addTagUseCase(interestName, "interest", currentState.interests)
            
            if (isSuccess) {
                _uiState.value = currentState.copy(interests = updatedTags)
            } else {
                _uiState.value = currentState.copy(
                    errorMessage = "동일한 태그가 이미 존재합니다"
                )
            }
        }
    }
    
    fun removeInterest(tagId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val updatedInterests = removeTagUseCase(tagId, currentState.interests)
            _uiState.value = currentState.copy(interests = updatedInterests)
        }
    }
    
    fun proceedToNextStep() {
        val currentState = _uiState.value
        
        // 닉네임 검증
        if (currentState.nickname.isEmpty()) {
            _uiState.value = currentState.copy(
                nicknameError = "닉네임을 입력해주세요"
            )
            return
        }
        
        if (currentState.nickname.length < 2) {
            _uiState.value = currentState.copy(
                nicknameError = "닉네임은 2자 이상이어야 합니다"
            )
            return
        }
        
        // 이미지 최소 1개 확인
        if (currentState.images.isEmpty()) {
            _uiState.value = currentState.copy(
                errorMessage = "최소 1개 이상의 이미지를 선택해주세요"
            )
            return
        }
        
        // 백엔드에 프로필 업데이트
        if (currentState.userId != null) {
            android.util.Log.d("ProfileSetupViewModel", "프로필 업데이트 시작 - userId: ${currentState.userId}")
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val profileData = mapOf(
                    "age" to currentState.age,
                    "region" to currentState.region,
                    "bio" to currentState.bio,
                    "image_count" to currentState.images.size
                )
                
                android.util.Log.d("ProfileSetupViewModel", "백엔드 updateUser 호출 - userId: ${currentState.userId}, nickname: ${currentState.nickname}")
                when (val result = backendRepository.updateUser(
                    userId = currentState.userId!!,
                    nickname = currentState.nickname,
                    profileData = profileData
                )) {
                    is ApiResult.Success -> {
                        android.util.Log.d("ProfileSetupViewModel", "프로필 업데이트 성공")
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            isProfileComplete = true,
                            errorMessage = ""
                        )
                    }
                    is ApiResult.Error -> {
                        android.util.Log.e("ProfileSetupViewModel", "프로필 업데이트 실패: ${result.message}")
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            errorMessage = "프로필 저장 실패: ${result.message}"
                        )
                    }
                    is ApiResult.Loading -> {}
                }
            }
        } else {
            android.util.Log.w("ProfileSetupViewModel", "userId가 null입니다! 백엔드 업데이트 스킵")
            // userId가 없으면 로컬만 업데이트
            _uiState.value = currentState.copy(
                isProfileComplete = true,
                errorMessage = ""
            )
        }
    }
    
    fun resetCompleteState() {
        _uiState.value = _uiState.value.copy(isProfileComplete = false)
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}
