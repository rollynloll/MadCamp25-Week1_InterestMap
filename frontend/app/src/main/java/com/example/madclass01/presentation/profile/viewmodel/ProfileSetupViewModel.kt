package com.example.madclass01.presentation.profile.viewmodel

import android.content.Context
import android.net.Uri
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
import java.io.File
import javax.inject.Inject

data class ProfileSetupUiState(
    val nickname: String = "",
    val age: Int = 0,
    val region: String = "",
    val gender: String = "",  // 성별: "male", "female", "undecided"
    val bio: String = "",
    val images: List<ImageItem> = emptyList(),
    val interests: List<Tag> = emptyList(),  // 사용자가 직접 선택/입력한 관심사
    val photoInterests: List<Tag> = emptyList(),  // 사진 임베딩으로 AI가 추천한 관심사
    val recommendedTags: List<String> = emptyList(),  // AI가 추천한 태그 (자동 추출)
    val isLoading: Boolean = false,
    val isAnalyzingImages: Boolean = false,  // 이미지 분석 중
    val errorMessage: String = "",
    val isProfileComplete: Boolean = false,
    val nicknameError: String = "",
    val imageCountText: String = "0/20",
    val userId: String? = null,  // 백엔드 userId 저장
    val uploadedImageUrls: List<String> = emptyList()  // 업로드된 이미지 URL
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
    
    fun updateGender(newGender: String) {
        _uiState.value = _uiState.value.copy(gender = newGender)
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
            val (isSuccess, updatedTags) = addTagUseCase(hobbyName, "interest", currentState.interests)
            
            if (isSuccess) {
                _uiState.value = currentState.copy(interests = updatedTags)
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
            val updatedInterests = removeTagUseCase(tagId, currentState.interests)
            _uiState.value = currentState.copy(interests = updatedInterests)
        }
    }
    
    fun addPhotoInterest(tagName: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val (isSuccess, updatedTags) = addTagUseCase(tagName, "photo_interest", currentState.photoInterests)
            
            if (isSuccess) {
                _uiState.value = currentState.copy(photoInterests = updatedTags)
            } else {
                _uiState.value = currentState.copy(
                    errorMessage = "동일한 태그가 이미 존재합니다"
                )
            }
        }
    }
    
    fun removePhotoInterest(tagId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val updatedPhotoInterests = removeTagUseCase(tagId, currentState.photoInterests)
            _uiState.value = currentState.copy(photoInterests = updatedPhotoInterests)
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
    
    fun proceedToNextStep(context: Context) {
        val currentState = _uiState.value
        
        // 이미 로딩 중이면 중복 요청 방지
        if (currentState.isLoading || currentState.isAnalyzingImages) {
            android.util.Log.d("ProfileSetupViewModel", "이미 처리 중입니다. 중복 요청 무시")
            return
        }
        
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
        
        // 백엔드에 사진 업로드 및 이미지 분석
        if (currentState.userId != null) {
            android.util.Log.d("ProfileSetupViewModel", "사진 업로드 및 이미지 분석 시작 - userId: ${currentState.userId}")
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isAnalyzingImages = true, isLoading = true)

                // 1. 모든 이미지를 최적화 (리사이징 + WebP 압축)
                val optimizedFiles = currentState.images.mapNotNull { imageItem ->
                    backendRepository.optimizeImage(context, android.net.Uri.parse(imageItem.uri))
                }

                if (optimizedFiles.isEmpty()) {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isAnalyzingImages = false,
                        errorMessage = "프로필 사진 처리 실패"
                    )
                    return@launch
                }

                android.util.Log.d(
                    "ProfileSetupViewModel",
                    "이미지 최적화 완료: ${optimizedFiles.size}개 파일"
                )

                // 2. 한 번에 모든 사진 업로드
                var recommendedTags: List<String> = emptyList()
                val uploadedUrls = when (val uploadResult = backendRepository.uploadPhotos(
                    userId = currentState.userId!!,
                    files = optimizedFiles,
                    selectedTags = currentState.interests.map { it.name } + currentState.photoInterests.map { it.name }
                )) {
                    is ApiResult.Success -> {
                        android.util.Log.d(
                            "ProfileSetupViewModel",
                            "사진 업로드 성공: ${uploadResult.data.photos.size}개"
                        )
                        recommendedTags = uploadResult.data.suggestedTags.take(5)
                        
                        // 임시 파일 삭제
                        optimizedFiles.forEach { it.delete() }
                        
                        // URL 리스트 반환
                        uploadResult.data.photos.map { it.fileUrl }
                    }
                    is ApiResult.Error -> {
                        android.util.Log.e(
                            "ProfileSetupViewModel",
                            "사진 업로드 실패: ${uploadResult.message}"
                        )
                        // 임시 파일 삭제
                        optimizedFiles.forEach { it.delete() }
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            isAnalyzingImages = false,
                            errorMessage = "사진 업로드 실패: ${uploadResult.message}"
                        )
                        return@launch
                    }
                    is ApiResult.Loading -> emptyList()
                }

                if (uploadedUrls.isEmpty()) {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isAnalyzingImages = false,
                        errorMessage = "사진 업로드 실패"
                    )
                    return@launch
                }

                // 업로드된 URL 저장
                _uiState.value = _uiState.value.copy(uploadedImageUrls = uploadedUrls)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAnalyzingImages = false,
                    recommendedTags = recommendedTags,
                    isProfileComplete = true  // 태그 선택 화면으로 이동
                )
            }
        } else {
            android.util.Log.w("ProfileSetupViewModel", "userId가 null입니다!")
            _uiState.value = currentState.copy(
                errorMessage = "로그인 정보가 없습니다"
            )
        }
    }
    
    /**
     * 추천 태그를 photoInterests에 추가/제거 (토글)
     */
    fun toggleRecommendedTag(tagName: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val existing = currentState.photoInterests.find { it.name == tagName }
            
            if (existing != null) {
                // 이미 선택된 태그면 제거
                val updatedPhotoInterests = removeTagUseCase(existing.id, currentState.photoInterests)
                _uiState.value = currentState.copy(photoInterests = updatedPhotoInterests)
            } else {
                // 선택되지 않은 태그면 추가
                val (isSuccess, updatedTags) = addTagUseCase(tagName, "photo_interest", currentState.photoInterests)
                if (isSuccess) {
                    _uiState.value = currentState.copy(photoInterests = updatedTags)
                }
            }
        }
    }
    
    /**
     * 태그 선택 완료 후 최종 프로필 저장
     */
    fun completeProfileSetup() {
        val currentState = _uiState.value
        
        if (currentState.userId == null) {
            _uiState.value = currentState.copy(errorMessage = "로그인 정보가 없습니다")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val userInterests = currentState.interests.map { it.name }
            val photoBasedInterests = currentState.photoInterests.map { it.name }

            val profileData = mapOf(
                "age" to currentState.age,
                "gender" to currentState.gender,
                "region" to currentState.region,
                "bio" to currentState.bio,
                "interests" to userInterests,
                "photo_interests" to photoBasedInterests,
                "image_count" to currentState.images.size
            )

            android.util.Log.d("ProfileSetupViewModel", "최종 프로필 저장 - userId: ${currentState.userId}")
            when (val result = backendRepository.updateUser(
                userId = currentState.userId!!,
                nickname = currentState.nickname,
                profileImageUrl = currentState.uploadedImageUrls.firstOrNull(),
                profileData = profileData
            )) {
                is ApiResult.Success -> {
                    android.util.Log.d("ProfileSetupViewModel", "프로필 저장 성공")
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isProfileComplete = true,
                        errorMessage = ""
                    )
                }
                is ApiResult.Error -> {
                    android.util.Log.e("ProfileSetupViewModel", "프로필 저장 실패: ${result.message}")
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "프로필 저장 실패: ${result.message}"
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    fun resetCompleteState() {
        _uiState.value = _uiState.value.copy(isProfileComplete = false)
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }

    fun resetInputsForEdit() {
        val existingUserId = _uiState.value.userId
        _uiState.value = ProfileSetupUiState(userId = existingUserId)
    }

    private fun copyToTempFile(context: Context, image: ImageItem): File? {
        val uri = Uri.parse(image.uri)
        val inputStream = try {
            context.contentResolver.openInputStream(uri)
        } catch (exception: Exception) {
            android.util.Log.e("ProfileSetupViewModel", "이미지 열기 실패: $uri", exception)
            null
        }
        inputStream ?: return null

        val fileName = image.name.ifBlank { "image_${System.currentTimeMillis()}.jpg" }
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}_$fileName")
        return try {
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (exception: Exception) {
            android.util.Log.e("ProfileSetupViewModel", "임시 파일 저장 실패", exception)
            null
        }
    }
}
