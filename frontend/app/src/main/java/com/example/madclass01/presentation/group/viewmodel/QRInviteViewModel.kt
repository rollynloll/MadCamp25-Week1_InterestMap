package com.example.madclass01.presentation.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.domain.model.Group
import com.example.madclass01.domain.model.InviteLink
import com.example.madclass01.domain.usecase.GenerateInviteLinkUseCase
import com.example.madclass01.domain.usecase.JoinGroupByInviteLinkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QRInviteUiState(
    val group: Group? = null,
    val inviteLink: InviteLink? = null,
    val qrCodeBitmap: String? = null,  // QR 코드 데이터
    val expiryTime: String = "24시간 후 만료",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val copySuccess: Boolean = false,
    val joinSuccess: Boolean = false,
    val joinGroupId: String? = null
)

@HiltViewModel
class QRInviteViewModel @Inject constructor(
    private val generateInviteLinkUseCase: GenerateInviteLinkUseCase,
    private val joinGroupByInviteLinkUseCase: JoinGroupByInviteLinkUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(QRInviteUiState())
    val uiState: StateFlow<QRInviteUiState> = _uiState.asStateFlow()
    
    fun initializeWithGroup(group: Group) {
        _uiState.value = _uiState.value.copy(group = group)
        generateInviteLink(group.id)
    }
    
    fun generateInviteLink(groupId: String, userId: String = "currentUser") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = ""
            )
            
            val result = generateInviteLinkUseCase(
                groupId = groupId,
                createdByUserId = userId
            )
            
            result.onSuccess { inviteLink ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    inviteLink = inviteLink,
                    qrCodeBitmap = inviteLink.qrCodeData
                )
            }
            
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "초대 링크 생성에 실패했습니다"
                )
            }
        }
    }
    
    fun copyInviteLink(): Boolean {
        val link = _uiState.value.inviteLink?.inviteUrl ?: return false
        
        // 클립보드에 복사 (실제 구현에서는 ClipboardManager 사용)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(copySuccess = true)
        }
        
        return true
    }
    fun joinGroupByLink(inviteUrl: String, userId: String) {
        viewModelScope.launch {
            val parsedGroupId = extractGroupIdFromInviteUrl(inviteUrl)
            if (parsedGroupId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "초대 링크 형식이 올바르지 않습니다"
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = ""
            )
            
            val result = joinGroupByInviteLinkUseCase(
                inviteUrl = inviteUrl,
                userId = userId
            )
            
            result.onSuccess { joined ->
                if (joined) {
                    // 그룹 정보 다시 로드
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        joinSuccess = true,
                        joinGroupId = parsedGroupId
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "그룹 가입에 실패했습니다"
                    )
                }
            }
            
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "그룹 가입에 실패했습니다"
                )
            }
        }
    }
    
    fun resetCopySuccess() {
        _uiState.value = _uiState.value.copy(copySuccess = false)
    }
    
    fun resetJoinSuccess() {
        _uiState.value = _uiState.value.copy(joinSuccess = false)
    }

    private fun extractGroupIdFromInviteUrl(inviteUrl: String): String? {
        return try {
            if (inviteUrl.startsWith("madcamp://invite/")) {
                inviteUrl.substringAfter("madcamp://invite/").takeIf { it.isNotBlank() }
            } else if (inviteUrl.contains("/invite/")) {
                inviteUrl.substringAfter("/invite/").takeIf { it.isNotBlank() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
