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
        "category" to "모든종류",
        "region" to "전체",
        "age" to "전체",
        "memberRange" to "전체",
        "activity" to "전체",
        "matchPercentage" to 70
    )
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchGroupsUseCase: SearchGroupsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    
    init {
        searchGroups()
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        // 검색어가 비어있으면 검색 취소
        if (query.isEmpty()) {
            searchJob?.cancel()
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        
        // 기존 검색 작업 취소 후 새로운 작업 시작 (500ms 디바운스)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            searchGroups()
        }
    }
    
    fun searchGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val results = searchGroupsUseCase(_uiState.value.searchQuery, _uiState.value.filters)
                _uiState.value = _uiState.value.copy(
                    searchResults = results,
                    isLoading = false,
                    errorMessage = if (results.isEmpty() && _uiState.value.searchQuery.isNotEmpty()) "검색 결과가 없습니다" else ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "검색에 실패했습니다"
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
                "category" to "모든종류",
                "region" to "전체",
                "age" to "전체",
                "memberRange" to "전체",
                "activity" to "전체",
                "matchPercentage" to 70
            )
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}
