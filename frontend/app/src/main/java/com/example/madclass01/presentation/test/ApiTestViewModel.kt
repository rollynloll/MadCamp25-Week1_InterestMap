package com.example.madclass01.presentation.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.data.repository.ApiResult
import com.example.madclass01.data.repository.BackendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiTestViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ApiTestUiState())
    val uiState: StateFlow<ApiTestUiState> = _uiState.asStateFlow()
    
    fun testHealthCheck() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = backendRepository.healthCheck()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        healthCheckResult = "✅ Success: ${result.data.message}"
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        healthCheckResult = "❌ Error: ${result.message} (Code: ${result.code})"
                    )
                }
                is ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    fun testCreateUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = backendRepository.createUser(
                provider = "kakao",
                providerUserId = "test-12345",
                nickname = "Android Test User"
            )) {
                is ApiResult.Success -> {
                    val user = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        createUserResult = """
                            ✅ Success!
                            User ID: ${user.id}
                            Nickname: ${user.nickname}
                            Provider: ${user.provider}
                        """.trimIndent()
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        createUserResult = "❌ Error: ${result.message} (Code: ${result.code})"
                    )
                }
                is ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    fun testGetUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = backendRepository.getUser("test-user-123")) {
                is ApiResult.Success -> {
                    val user = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        getUserResult = """
                            ✅ Success!
                            User ID: ${user.id}
                            Nickname: ${user.nickname}
                            Provider: ${user.provider}
                            Created: ${user.createdAt}
                        """.trimIndent()
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        getUserResult = "❌ Error: ${result.message} (Code: ${result.code})"
                    )
                }
                is ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
}
