package com.example.madclass01.presentation.group.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.domain.model.Tag
import com.example.madclass01.domain.usecase.CreateGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.madclass01.data.repository.BackendRepository

data class CreateGroupUiState(
    val groupName: String = "",
    val groupDescription: String = "",
    val useCustomImage: Boolean = false,  // true: 사진 사용, false: 아이콘 사용
    val selectedIconType: String = "users",
    val profileImageUri: String? = null,
    val selectedRegion: String = "전체",
    val selectedTags: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isCreateSuccess: Boolean = false,
    val createdGroupId: String? = null
)

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val createGroupUseCase: CreateGroupUseCase,
    private val backendRepository: BackendRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()
    
    fun updateGroupName(name: String) {
        _uiState.value = _uiState.value.copy(
            groupName = name,
            errorMessage = ""
        )
    }
    
    fun updateGroupDescription(description: String) {
        _uiState.value = _uiState.value.copy(
            groupDescription = description,
            errorMessage = ""
        )
    }

    fun updateRegion(region: String) {
        _uiState.value = _uiState.value.copy(
            selectedRegion = region,
            errorMessage = ""
        )
    }
    
    fun selectIconType(iconType: String) {
        _uiState.value = _uiState.value.copy(
            selectedIconType = iconType,
            useCustomImage = false,
            profileImageUri = null
        )
    }
    
    fun updateProfileImageUri(uri: String?) {
        _uiState.value = _uiState.value.copy(
            profileImageUri = uri,
            useCustomImage = uri != null
        )
    }
    
    fun addTag(tag: String) {
        val currentTags = _uiState.value.selectedTags.toMutableList()
        if (!currentTags.contains(tag) && currentTags.size < 5) {
            currentTags.add(tag)
            _uiState.value = _uiState.value.copy(
                selectedTags = currentTags
            )
        }
    }
    
    fun removeTag(tag: String) {
        val currentTags = _uiState.value.selectedTags.toMutableList()
        currentTags.remove(tag)
        _uiState.value = _uiState.value.copy(
            selectedTags = currentTags
        )
    }
    
    fun setPublic(isPublic: Boolean) {
        _uiState.value = _uiState.value.copy(
            isPublic = isPublic
        )
    }
    
    fun createGroup(userId: String, context: Context) {
        val currentState = _uiState.value
        
        // 유효성 검사
        if (currentState.groupName.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "그룹 이름을 입력해주세요"
            )
            return
        }
        
        if (currentState.groupDescription.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "그룹 설명을 입력해주세요"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isLoading = true,
                errorMessage = ""
            )
            
            val result = createGroupUseCase(
                name = currentState.groupName,
                description = currentState.groupDescription,
                iconType = currentState.selectedIconType,
                tags = currentState.selectedTags,
                region = currentState.selectedRegion,
                imageUrl = null,
                isPublic = currentState.isPublic,
                userId = userId
            )
            
            result.onSuccess { group ->
                if (currentState.useCustomImage && currentState.profileImageUri != null) {
                    val optimized = backendRepository.optimizeImage(
                        context,
                        android.net.Uri.parse(currentState.profileImageUri)
                    )
                    if (optimized != null) {
                        viewModelScope.launch {
                            backendRepository.uploadGroupProfileImage(group.id, optimized)
                            optimized.delete()
                        }
                    }
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isCreateSuccess = true,
                    createdGroupId = group.id
                )
            }
            
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "그룹 생성에 실패했습니다"
                )
            }
        }
    }
    
    fun resetCreateState() {
        _uiState.value = CreateGroupUiState()
    }
}
