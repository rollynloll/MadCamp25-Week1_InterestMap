package com.example.madclass01.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val imageCountText: String = "0/20"
)

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val addImageUseCase: AddImageUseCase,
    private val removeImageUseCase: RemoveImageUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val removeTagUseCase: RemoveTagUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()
    
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
        viewModelScope.launch {
            val currentState = _uiState.value
            val imageItem = ImageItem(uri = imageUri, name = imageName, size = imageSize)
            
            val (isSuccess, updatedImages) = addImageUseCase(imageItem, currentState.images)
            
            if (isSuccess) {
                _uiState.value = currentState.copy(
                    images = updatedImages,
                    imageCountText = "${updatedImages.size}/20"
                )
            } else {
                if (currentState.images.size >= 20) {
                    _uiState.value = currentState.copy(
                        errorMessage = "최대 20개의 이미지만 선택할 수 있습니다"
                    )
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
        
        _uiState.value = currentState.copy(
            isProfileComplete = true,
            errorMessage = ""
        )
    }
    
    fun resetCompleteState() {
        _uiState.value = _uiState.value.copy(isProfileComplete = false)
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}
