package com.example.madclass01.presentation.group.viewmodel

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

data class CreateGroupUiState(
    val groupName: String = "",
    val groupDescription: String = "",
    val selectedIconType: String = "users", // users, coffee, camera, mountain
    val selectedTags: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isCreateSuccess: Boolean = false,
    val createdGroupId: String? = null
)

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val createGroupUseCase: CreateGroupUseCase
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
    
    fun selectIconType(iconType: String) {
        _uiState.value = _uiState.value.copy(
            selectedIconType = iconType
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
    
    fun createGroup(userId: String) {
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
                isPublic = currentState.isPublic,
                userId = userId
            )
            
            result.onSuccess { group ->
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
