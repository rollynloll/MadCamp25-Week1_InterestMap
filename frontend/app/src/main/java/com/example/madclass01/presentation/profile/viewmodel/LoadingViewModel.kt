package com.example.madclass01.presentation.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.data.repository.ApiResult
import com.example.madclass01.data.repository.BackendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoadingUiState(
    val userId: String? = null,
    val imageUrls: List<String> = emptyList(),
    val isAnalyzing: Boolean = false,
    val progress: Float = 0f,
    val statusMessage: String = "이미지 분석 준비 중...",
    val recommendedTags: List<String> = emptyList(),
    val allKeywords: List<String> = emptyList(),
    val isComplete: Boolean = false,
    val errorMessage: String = ""
)

@HiltViewModel
class LoadingViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoadingUiState())
    val uiState: StateFlow<LoadingUiState> = _uiState.asStateFlow()
    
    fun startImageAnalysis(userId: String, imageUrls: List<String>) {
        if (imageUrls.isEmpty()) {
            Log.w("LoadingViewModel", "이미지 URL이 비어있습니다")
            _uiState.value = _uiState.value.copy(
                errorMessage = "분석할 이미지가 없습니다",
                isComplete = true
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                userId = userId,
                imageUrls = imageUrls,
                isAnalyzing = true,
                progress = 0.1f,
                statusMessage = "AI가 이미지를 분석하고 있어요..."
            )
            
            Log.d("LoadingViewModel", "이미지 분석 시작 - userId: $userId, 이미지 수: ${imageUrls.size}")
            
            // 진행률 애니메이션
            launch {
                for (i in 1..8) {
                    delay(300)
                    _uiState.value = _uiState.value.copy(progress = 0.1f + (i * 0.1f))
                }
            }
            
            delay(1000) // UI 효과를 위한 최소 대기
            
            when (val result = backendRepository.analyzeImages(userId, imageUrls)) {
                is ApiResult.Success -> {
                    Log.d("LoadingViewModel", "이미지 분석 성공 - 추천 태그: ${result.data.recommendedTags}")
                    
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        progress = 1.0f,
                        statusMessage = "분석 완료!",
                        recommendedTags = result.data.recommendedTags,
                        allKeywords = result.data.allKeywords.map { it.keyword },
                        isComplete = true,
                        errorMessage = ""
                    )
                }
                is ApiResult.Error -> {
                    Log.e("LoadingViewModel", "이미지 분석 실패: ${result.message}")
                    
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        statusMessage = "분석 실패",
                        errorMessage = "이미지 분석에 실패했습니다: ${result.message}",
                        isComplete = true
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    fun resetState() {
        _uiState.value = LoadingUiState()
    }
}
