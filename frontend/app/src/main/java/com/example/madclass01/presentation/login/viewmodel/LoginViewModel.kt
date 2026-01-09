package com.example.madclass01.presentation.login.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.domain.model.User
import com.example.madclass01.domain.usecase.LoginUseCase
import com.example.madclass01.domain.usecase.ValidateEmailUseCase
import com.example.madclass01.domain.usecase.ValidatePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isEmailError: Boolean = false,
    val isPasswordError: Boolean = false,
    val emailErrorMessage: String = "",
    val passwordErrorMessage: String = "",
    val loginErrorMessage: String = "",
    val isLoginSuccess: Boolean = false,
    val loginToken: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun updateEmail(newEmail: String) {
        _uiState.value = _uiState.value.copy(
            email = newEmail,
            isEmailError = false,
            emailErrorMessage = ""
        )
    }
    
    fun updatePassword(newPassword: String) {
        _uiState.value = _uiState.value.copy(
            password = newPassword,
            isPasswordError = false,
            passwordErrorMessage = ""
        )
    }
    
    fun login() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            // 유효성 검사
            val isEmailValid = validateEmailUseCase(currentState.email)
            val isPasswordValid = validatePasswordUseCase(currentState.password)
            
            if (!isEmailValid || !isPasswordValid) {
                _uiState.value = currentState.copy(
                    isEmailError = !isEmailValid,
                    emailErrorMessage = if (!isEmailValid) "올바른 이메일을 입력해주세요" else "",
                    isPasswordError = !isPasswordValid,
                    passwordErrorMessage = if (!isPasswordValid) "비밀번호는 6자 이상이어야 합니다" else ""
                )
                return@launch
            }
            
            // 로그인 시작
            _uiState.value = currentState.copy(isLoading = true)
            
            try {
                val user = User(
                    email = currentState.email,
                    password = currentState.password
                )
                
                val result = loginUseCase(user)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        loginToken = result.token
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginErrorMessage = result.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loginErrorMessage = "로그인 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }
    
    fun resetLoginState() {
        _uiState.value = _uiState.value.copy(
            isLoginSuccess = false,
            loginToken = null,
            loginErrorMessage = ""
        )
    }
}
