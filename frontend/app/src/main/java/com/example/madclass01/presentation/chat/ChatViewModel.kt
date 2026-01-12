package com.example.madclass01.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.data.repository.ApiResult
import com.example.madclass01.data.repository.BackendRepository
import com.example.madclass01.data.remote.dto.toDomain
import com.example.madclass01.domain.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSending: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var currentGroupId: String? = null

    /**
     * 채팅방 초기화 및 메시지 로드
     */
    fun initializeChatRoom(groupId: String, userId: String) {
        currentGroupId = groupId
        loadMessages(groupId, userId)
        startPolling(groupId, userId)
    }

    /**
     * 메시지 목록 로드
     */
    private fun loadMessages(groupId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = backendRepository.getGroupMessages(groupId)) {
                is ApiResult.Success -> {
                    val messages: List<ChatMessage> = result.data.map { it.toDomain() }
                        .map { message ->
                            message.copy(userId = message.userId ?: userId)
                        }
                    _uiState.value = _uiState.value.copy(
                        messages = messages.reversed(), // 최신 메시지가 아래로
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    /**
     * 폴링으로 새 메시지 확인 (3초마다)
     */
    private fun startPolling(groupId: String, userId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(3000) // 3초마다 폴링
                when (val result = backendRepository.getGroupMessages(groupId)) {
                    is ApiResult.Success -> {
                        val messages: List<ChatMessage> = result.data.map { it.toDomain() }
                            .map { message ->
                                message.copy(userId = message.userId ?: userId)
                            }
                        _uiState.value = _uiState.value.copy(
                            messages = messages.reversed()
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * 텍스트 메시지 전송
     */
    fun sendMessage(groupId: String, userId: String, message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)

            when (val result = backendRepository.sendGroupMessage(groupId, message)) {
                is ApiResult.Success -> {
                    // 메시지 전송 성공 후 즉시 새로고침
                    loadMessages(groupId, userId)
                    _uiState.value = _uiState.value.copy(isSending = false)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * 이미지 메시지 전송
     */
    fun sendImageMessage(groupId: String, userId: String, imageFile: java.io.File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)

            when (val result = backendRepository.sendGroupImageMessage(groupId, userId, imageFile)) {
                is ApiResult.Success -> {
                    // 이미지 전송 성공 후 새로고침
                    loadMessages(groupId, userId)
                    _uiState.value = _uiState.value.copy(isSending = false)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    /**
     * 에러 초기화
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
