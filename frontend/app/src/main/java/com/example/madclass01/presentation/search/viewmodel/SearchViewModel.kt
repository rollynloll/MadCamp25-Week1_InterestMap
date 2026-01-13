package com.example.madclass01.presentation.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.domain.model.Group
import com.example.madclass01.domain.usecase.SearchGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<Group> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val filters: Map<String, Any> = mapOf(
        "region" to "전체",
        "memberRange" to "전체"
    )
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchGroupsUseCase: SearchGroupsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    private var currentUserId: String? = null

    init {
        searchGroups()
    }

    fun setUserId(userId: String?) {
        if (currentUserId == userId) return
        currentUserId = userId
        searchGroups()
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    fun performSearch() {
        val query = _uiState.value.searchQuery.trim()
        
        // 검색어가 비어있으면 검색 안함
        if (query.isEmpty()) {
            return
        }
        
        // 기존 검색 작업 취소
        searchJob?.cancel()
        searchGroups()
    }
    
    fun searchGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            
            try {
                val results = searchGroupsUseCase(
                    currentUserId,
                    _uiState.value.searchQuery,
                    _uiState.value.filters
                )
                _uiState.value = _uiState.value.copy(
                    searchResults = results,
                    isLoading = false,
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    searchResults = emptyList(),
                    errorMessage = "검색에 실패했습니다. 다시 시도해주세요."
                )
            }
        }
    }
    
    fun updateFilter(key: String, value: Any) {
        val updatedFilters = _uiState.value.filters.toMutableMap()
        updatedFilters[key] = value
        _uiState.value = _uiState.value.copy(filters = updatedFilters)
    }

    fun resetFilters() {
        _uiState.value = _uiState.value.copy(
            filters = mapOf(
                "region" to "전체",
                "memberRange" to "전체"
            )
        )
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
    
    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}
