package com.example.madclass01.presentation.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.data.repository.ApiResult
import com.example.madclass01.data.repository.BackendRepository
import com.example.madclass01.domain.model.Group
import com.example.madclass01.domain.usecase.GetMyGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupListUiState(
    val myGroups: List<Group> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val userId: String? = null
)

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val getMyGroupsUseCase: GetMyGroupsUseCase,
    private val backendRepository: BackendRepository  // 백엔드 추가
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GroupListUiState())
    val uiState: StateFlow<GroupListUiState> = _uiState.asStateFlow()
    
    fun setUserId(userId: String) {
        _uiState.value = _uiState.value.copy(userId = userId)
        loadMyGroups()
    }
    
    fun loadMyGroups() {
        val userId = _uiState.value.userId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = backendRepository.getUserGroups(userId)) {
                is ApiResult.Success -> {
                    // Backend response를 Domain model로 변환
                    val groups = result.data.map { groupResponse ->
                        Group(
                            id = groupResponse.id,
                            name = groupResponse.name,
                            description = groupResponse.description ?: "",
                            memberCount = groupResponse.memberIds.size,
                            tags = emptyList(),  // TODO: 태그 정보 추가
                            imageUrl = ""
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        myGroups = groups,
                        isLoading = false,
                        errorMessage = ""
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "그룹을 불러오는데 실패했습니다: ${result.message}"
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    /**
     * 새 그룹 생성
     */
    fun createGroup(name: String, description: String?) {
        val userId = _uiState.value.userId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = backendRepository.createGroup(name, userId, description)) {
                is ApiResult.Success -> {
                    // 그룹 생성 성공 후 목록 새로고침
                    loadMyGroups()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "그룹 생성 실패: ${result.message}"
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }
}
