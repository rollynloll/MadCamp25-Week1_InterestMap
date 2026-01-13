package com.example.madclass01.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.core.UrlResolver
import com.example.madclass01.data.repository.ApiResult
import com.example.madclass01.data.repository.BackendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userId: String? = null,
    val nickname: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val region: String? = null,
    val bio: String? = null,
    val birthdate: String? = null,
    val images: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val photoInterests: List<String> = emptyList()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // NOTE:
            // This app currently uses DB-backed legacy endpoints for user/profile creation & photo upload:
            // - POST /api/users
            // - GET /api/photos/user/{userId}
            // The /me endpoint requires an auth token from /auth/*, which the current login flow doesn't store.

            val userResult = backendRepository.getUser(userId)
            val photosResult = backendRepository.getUserPhotos(userId)

            if (userResult is ApiResult.Error) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = userResult.message
                )
                return@launch
            }

            val user = (userResult as ApiResult.Success).data
            val profileData = user.profileData

            val ageObj = profileData["age"]
            val age = when (ageObj) {
                is Number -> ageObj.toInt()
                is String -> ageObj.toDoubleOrNull()?.toInt()
                else -> null
            }
            val gender = profileData["gender"]?.toString()
            val region = profileData["region"]?.toString()
            val birthdate = profileData["birthdate"]?.toString()
            val bio = profileData["bio"]?.let { value ->
                when (value) {
                    is List<*> -> value.mapNotNull { it?.toString()?.takeIf { it.isNotBlank() } }.joinToString("\n")
                    is String -> value
                    else -> value.toString()
                }
            }
            
            // interests와 photo_interests를 모두 합쳐서 tags로 반환
            val interests = profileData.stringListValue("interests")
            val photoInterests = profileData.stringListValue("photo_interests")
            val tags = (interests + photoInterests).distinct()

            val photoUrls: List<String> = when (photosResult) {
                is ApiResult.Success -> {
                    // /api/photos/user/{userId} returns relative fileUrl -> resolve to base URL
                    photosResult.data.mapNotNull { photo ->
                        UrlResolver.resolve(photo.fileUrl.ifBlank { photo.filePath })
                    }.distinct()
                }
                else -> emptyList()
            }

            val images = buildList {
                // Prefer profile image first if exists
                UrlResolver.resolve(user.profileImageUrl)?.takeIf { it.isNotBlank() }?.let { add(it) }
                addAll(photoUrls)
            }.distinct()

            _uiState.value = ProfileUiState(
                isLoading = false,
                userId = user.id,
                nickname = user.nickname,
                age = age,
                gender = gender,
                region = region,
                bio = bio,
                birthdate = birthdate,
                images = images,
                tags = tags,
                interests = interests,
                photoInterests = photoInterests
            )
        }
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
