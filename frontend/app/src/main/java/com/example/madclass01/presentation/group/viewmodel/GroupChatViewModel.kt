package com.example.madclass01.presentation.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.domain.model.ChatMessage
import com.example.madclass01.domain.usecase.JoinGroupChatUseCase
import com.example.madclass01.domain.usecase.LeaveGroupChatUseCase
import com.example.madclass01.domain.usecase.ObserveGroupChatUseCase
import com.example.madclass01.domain.usecase.SendGroupImageMessageUseCase
import com.example.madclass01.domain.usecase.SendGroupTextMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupChatUiState(
    val groupId: String = "",
    val groupName: String = "",
    val memberCount: Int = 0,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val isConnected: Boolean = false,
    val errorMessage: String = ""
)

@HiltViewModel
class GroupChatViewModel @Inject constructor(
    private val observeGroupChatUseCase: ObserveGroupChatUseCase,
    private val sendGroupTextMessageUseCase: SendGroupTextMessageUseCase,
    private val sendGroupImageMessageUseCase: SendGroupImageMessageUseCase,
    private val joinGroupChatUseCase: JoinGroupChatUseCase,
    private val leaveGroupChatUseCase: LeaveGroupChatUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupChatUiState())
    val uiState: StateFlow<GroupChatUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun initialize(groupId: String, groupName: String, memberCount: Int, currentUserId: String) {
        _uiState.value = _uiState.value.copy(groupId = groupId, groupName = groupName, memberCount = memberCount)

        viewModelScope.launch {
            joinGroupChatUseCase(groupId, currentUserId)
            _uiState.value = _uiState.value.copy(isConnected = true)
        }

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeGroupChatUseCase(groupId).collectLatest { list ->
                _uiState.value = _uiState.value.copy(messages = list)
            }
        }
    }

    fun updateInput(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendText(currentUserId: String) {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            val res = sendGroupTextMessageUseCase(_uiState.value.groupId, currentUserId, text)
            _uiState.value = _uiState.value.copy(isSending = false, inputText = "")
            res.onFailure { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "메시지 전송 실패")
            }
        }
    }

    fun sendImage(currentUserId: String, imageBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            val res = sendGroupImageMessageUseCase(_uiState.value.groupId, currentUserId, imageBytes, fileName)
            _uiState.value = _uiState.value.copy(isSending = false)
            res.onFailure { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "이미지 전송 실패")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
    }

    fun leave(currentUserId: String) {
        viewModelScope.launch {
            leaveGroupChatUseCase(_uiState.value.groupId, currentUserId)
            _uiState.value = _uiState.value.copy(isConnected = false)
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = "") }
}
