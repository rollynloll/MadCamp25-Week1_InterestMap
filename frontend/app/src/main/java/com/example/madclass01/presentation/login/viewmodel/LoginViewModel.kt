package com.example.madclass01.presentation.login.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.data.repository.ApiResult
import com.example.madclass01.data.repository.BackendRepository
import com.example.madclass01.domain.model.User
import com.example.madclass01.domain.usecase.LoginUseCase
import com.example.madclass01.domain.usecase.ValidateEmailUseCase
import com.example.madclass01.domain.usecase.ValidatePasswordUseCase
import com.example.madclass01.presentation.login.model.LoginSource
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
    val loginToken: String? = null,
    val userId: String? = null,  // 백엔드 userId 추가
    val nickname: String? = null,
    val profileAge: Int? = null,
    val profileGender: String? = null,  // gender 추가
    val profileRegion: String? = null,
    val profileBio: String? = null,
    val profileTags: List<String> = emptyList(),
    val profilePhotoInterests: List<String> = emptyList(),
    val isProfileComplete: Boolean = false,
    val loginSource: LoginSource = LoginSource.None
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val backendRepository: BackendRepository  // 백엔드 추가
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
                        loginSource = LoginSource.Email,
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
    
    /**
     * 카카오 로그인 성공 후 백엔드에 사용자 등록/조회
     */
    fun handleKakaoLoginSuccess(
        kakaoUserId: String,
        nickname: String?,
        profileImageUrl: String?
    ) {
        viewModelScope.launch {
            android.util.Log.d("LoginViewModel", "handleKakaoLoginSuccess 시작 - kakaoUserId: $kakaoUserId")
            _uiState.value = _uiState.value.copy(isLoading = true, loginErrorMessage = "")
            
            when (val result = backendRepository.createUser(
                provider = "kakao",
                providerUserId = kakaoUserId,
                nickname = nickname,
                profileImageUrl = profileImageUrl
            )) {
                is ApiResult.Success -> {
                    val user = result.data
                    android.util.Log.d("LoginViewModel", "백엔드 유저 생성 성공 - userId: ${user.id}, nickname: ${user.nickname}")
                    android.util.Log.d("LoginViewModel", "RAW ProfileData: ${user.profileData}")

                    val profileAge = user.profileData.intValue("age")
                    val profileGender = user.profileData.stringValue("gender")
                    val profileRegion = user.profileData.stringValue("region")
                    val profileBio = user.profileData.stringValue("bio")
                    val profileTags = user.profileData.stringListValue("interests")
                    val profilePhotoInterests = user.profileData.stringListValue("photo_interests")
                    val imageCount = user.profileData.intValue("image_count") ?: 0
                    val isProfileComplete = if (user.isNewUser) {
                        false
                    } else {
                        true
                    }

                    android.util.Log.d("LoginViewModel", "Extracted Tags (interests): $profileTags")
                    android.util.Log.d("LoginViewModel", "Extracted Photo Interests: $profilePhotoInterests")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        loginSource = LoginSource.Kakao,
                        userId = user.id,
                        nickname = user.nickname ?: nickname,
                        profileAge = profileAge,
                        profileGender = profileGender,
                        profileRegion = profileRegion,
                        profileBio = profileBio,
                        profileTags = profileTags,
                        profilePhotoInterests = profilePhotoInterests,
                        isProfileComplete = isProfileComplete,
                        loginToken = user.id  // userId를 token으로 사용
                    )
                }
                is ApiResult.Error -> {
                    android.util.Log.e("LoginViewModel", "백엔드 유저 생성 실패: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginErrorMessage = "백엔드 연동 실패: ${result.message}"
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    /**
     * 로그인 에러 설정 (외부에서 호출)
     */
    fun setLoginError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            loginErrorMessage = message
        )
    }
    
    fun resetLoginState() {
        _uiState.value = _uiState.value.copy(
            isLoginSuccess = false,
            loginToken = null,
            userId = null,
            nickname = null,
            profileAge = null,
            profileRegion = null,
            profileBio = null,
            isProfileComplete = false,
            loginSource = LoginSource.None,
            loginErrorMessage = ""
        )
    }

    /**
     * 프론트 전용 임시 로그인 (백엔드 연동 X)
     * - 다음(프로필) 화면으로 바로 이동시키기 위한 mock 처리
     */
    fun loginOffline(
        userId: String = "local_test_user",
        nickname: String = "테스트유저"
    ) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isLoginSuccess = true,
            loginSource = LoginSource.Test,
            userId = userId,
            nickname = nickname,
            isProfileComplete = true,
            loginToken = userId,
            loginErrorMessage = ""
        )
    }

    /**
     * 개발/테스트용 임시 로그인
     * - 카카오 SDK 없이도 바로 앱 진입 가능
     * - 가능하면 백엔드 test endpoint로 유저를 생성해 실제 userId를 받는다.
     */
    fun loginAsTestUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loginErrorMessage = "")

            val provider = "test"
            val providerUserId = "test_user_${System.currentTimeMillis()}"
            val nickname = "테스트유저"

            when (val result = backendRepository.createTestUser(
                provider = provider,
                providerUserId = providerUserId,
                nickname = nickname
            )) {
                is ApiResult.Success -> {
                    val user = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        loginSource = LoginSource.Test,
                        userId = user.id,
                        nickname = user.nickname ?: nickname,
                        isProfileComplete = true,
                        loginToken = user.id
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginErrorMessage = "테스트 로그인 실패: ${result.message}"
                    )
                }
                is ApiResult.Loading -> {
                    // no-op
                }
            }
        }
    }

    private fun Any?.toIntOrNull(): Int? {
        return when (this) {
            is Int -> this
            is Long -> this.toInt()
            is Double -> this.toInt()
            is Float -> this.toInt()
            is String -> this.toIntOrNull()
            else -> null
        }
    }

    private fun Map<String, Any>.stringValue(key: String): String? {
        val value = this[key] ?: return null
        return when (value) {
            is String -> value
            else -> value.toString().takeIf { it.isNotBlank() }
        }
    }

    private fun Map<String, Any>.intValue(key: String): Int? {
        return this[key].toIntOrNull()
    }

    private fun Map<String, Any>.stringListValue(key: String): List<String> {
        val value = this[key] ?: return emptyList()
        return when (value) {
            is List<*> -> value.mapNotNull { it?.toString()?.takeIf { item -> item.isNotBlank() } }
            is String -> if (value.isBlank()) emptyList() else listOf(value)
            else -> listOf(value.toString()).filter { it.isNotBlank() }
        }
    }
}
