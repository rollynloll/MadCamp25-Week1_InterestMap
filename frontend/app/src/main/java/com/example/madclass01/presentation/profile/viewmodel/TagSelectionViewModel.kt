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
        return currentState.extractedTags.filter { it.isSelected } +
                currentState.recommendedTags.filter { it.isSelected } +
                currentState.customTags
    }
}
