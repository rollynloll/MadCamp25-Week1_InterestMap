package com.example.madclass01.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.domain.model.Tag
import com.example.madclass01.domain.model.TagAnalysisResult
import com.example.madclass01.domain.usecase.AnalyzeImagesUseCase
import com.example.madclass01.domain.usecase.AddTagUseCase
import com.example.madclass01.domain.usecase.RemoveTagUseCase
import com.example.madclass01.domain.usecase.ToggleTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagSelectionUiState(
    val extractedTags: List<Tag> = emptyList(),
    val recommendedTags: List<Tag> = emptyList(),
    val customTags: List<Tag> = emptyList(),
    val photoInterests: List<Tag> = emptyList(),  // 사진 임베딩으로 AI가 추천한 관심사
    val errorMessage: String = "",
    val isSelectionComplete: Boolean = false
)

@HiltViewModel
class TagSelectionViewModel @Inject constructor(
    private val analyzeImagesUseCase: AnalyzeImagesUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val removeTagUseCase: RemoveTagUseCase,
    private val toggleTagUseCase: ToggleTagUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TagSelectionUiState())
    val uiState: StateFlow<TagSelectionUiState> = _uiState.asStateFlow()
    
    fun analyzeImages() {
        viewModelScope.launch {
            try {
                val result = analyzeImagesUseCase(emptyList())
                _uiState.value = _uiState.value.copy(
                    extractedTags = result.extractedTags.map { it.copy(isSelected = true) },
                    recommendedTags = result.recommendedTags
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "이미지 분석에 실패했습니다"
                )
            }
        }
    }
    
    fun setRecommendedTags(tags: List<String>) {
        val tagModels = tags.mapIndexed { index, tagName ->
            Tag(
                id = "recommended_$index",
                name = tagName,
                category = "recommended",
                isSelected = true  // 추천 태그는 기본 선택
            )
        }
        _uiState.value = _uiState.value.copy(
            extractedTags = tagModels
        )
    }
    
    fun toggleExtractedTag(tagId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val updatedTags = toggleTagUseCase(tagId, currentState.extractedTags)
            _uiState.value = currentState.copy(extractedTags = updatedTags)
        }
    }
    
    fun toggleRecommendedTag(tagId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val updatedTags = toggleTagUseCase(tagId, currentState.recommendedTags)
            _uiState.value = currentState.copy(recommendedTags = updatedTags)
        }
    }
    
    fun addCustomTag(tagName: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val allCurrentTags = currentState.extractedTags + currentState.recommendedTags + currentState.customTags
            val (isSuccess, updatedTags) = addTagUseCase(tagName, "custom", allCurrentTags)
            
            if (isSuccess) {
                _uiState.value = currentState.copy(
                    customTags = currentState.customTags + updatedTags.last()
                )
            } else {
                _uiState.value = currentState.copy(
                    errorMessage = "동일한 태그가 이미 존재합니다"
                )
            }
        }
    }
    
    fun removeCustomTag(tagId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val updatedCustomTags = removeTagUseCase(tagId, currentState.customTags)
            _uiState.value = currentState.copy(customTags = updatedCustomTags)
        }
    }
    
    fun addPhotoInterest(tagName: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val allCurrentTags = currentState.extractedTags + currentState.recommendedTags + 
                                currentState.customTags + currentState.photoInterests
            val (isSuccess, updatedTags) = addTagUseCase(tagName, "photo_interest", allCurrentTags)
            
            if (isSuccess) {
                _uiState.value = currentState.copy(
                    photoInterests = currentState.photoInterests + updatedTags.last()
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
    
    fun setPhotoInterests(interests: List<String>) {
        val photoInterestTags = interests.map { tagName ->
            Tag(
                id = "photo_${System.currentTimeMillis()}_${tagName.hashCode()}",
                name = tagName,
                category = "photo_interest",
                isSelected = true
            )
        }
        _uiState.value = _uiState.value.copy(photoInterests = photoInterestTags)
    }
    
    fun completeSelection() {
        _uiState.value = _uiState.value.copy(isSelectionComplete = true)
    }
    
    fun resetCompleteState() {
        _uiState.value = _uiState.value.copy(isSelectionComplete = false)
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
    
    fun getSelectedTags(): List<Tag> {
        val currentState = _uiState.value
        // 사용자가 직접 선택한 태그만 반환 (AI 추천 태그만 포함, photoInterests 제외)
        // 최대 5개로 제한
        val userSelectedTags = currentState.extractedTags.filter { it.isSelected } +
                currentState.recommendedTags.filter { it.isSelected } +
                currentState.customTags
        return userSelectedTags.take(5)
    }
    
    /**
     * 프로필 표시용: 사용자가 직접 선택한 관심사만 최대 5개 반환
     * (photoInterests는 제외)
     */
    fun getUserInterestsForProfile(): List<Tag> {
        return getSelectedTags()
    }
    
    /**
     * 백엔드 저장용: 모든 선택된 태그 반환 (photoInterests 포함)
     */
    fun getAllSelectedTags(): List<Tag> {
        val currentState = _uiState.value
        return currentState.extractedTags.filter { it.isSelected } +
                currentState.recommendedTags.filter { it.isSelected } +
                currentState.customTags +
                currentState.photoInterests.filter { it.isSelected }
    }
}
