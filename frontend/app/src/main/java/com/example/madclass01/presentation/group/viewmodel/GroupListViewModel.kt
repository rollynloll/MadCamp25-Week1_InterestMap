package com.example.madclass01.presentation.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val errorMessage: String = ""
)

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val getMyGroupsUseCase: GetMyGroupsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GroupListUiState())
    val uiState: StateFlow<GroupListUiState> = _uiState.asStateFlow()
    
    init {
        loadMyGroups()
    }
    
    fun loadMyGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val groups = getMyGroupsUseCase()
                _uiState.value = _uiState.value.copy(
                    myGroups = groups,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "그룹을 불러오는데 실패했습니다"
                )
            }
        }
    }
}
