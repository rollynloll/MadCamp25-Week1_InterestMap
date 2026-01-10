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

    init {
        // 오프라인/로그인 전에도 "내가 참여한 그룹" 목업이 보이도록 기본 데이터 로드
        viewModelScope.launch {
            val groups = getMyGroupsUseCase()
            _uiState.value = _uiState.value.copy(myGroups = groups)
        }
    }
    
    fun setUserId(userId: String) {
        _uiState.value = _uiState.value.copy(userId = userId)
        loadMyGroups()
    }
    
    fun loadMyGroups() {
        val userId = _uiState.value.userId

        // userId가 없으면(로그인 전/오프라인) 목업 데이터로 표시
        if (userId.isNullOrBlank()) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
                val groups = getMyGroupsUseCase()
                _uiState.value = _uiState.value.copy(myGroups = groups, isLoading = false)
            }
            return
        }
        
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
                    // 백엔드 실패 시에도 UX가 막히지 않도록 목업으로 fallback
                    val fallbackGroups = getMyGroupsUseCase()
                    _uiState.value = _uiState.value.copy(
                        myGroups = fallbackGroups,
                        isLoading = false,
                        errorMessage = ""
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
