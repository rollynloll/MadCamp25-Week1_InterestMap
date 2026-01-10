package com.example.madclass01.presentation.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.domain.model.Group
import com.example.madclass01.domain.model.RelationshipGraph
import com.example.madclass01.domain.usecase.GetGroupDetailUseCase
import com.example.madclass01.domain.usecase.GetRelationshipGraphUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 그룹 상세 화면의 UI 상태
 */
data class GroupDetailUiState(
    val group: Group? = null,
    val relationshipGraph: RelationshipGraph? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val selectedUserId: String? = null,  // 선택된 사용자 (채팅 등)
    val chatRoomId: String? = null  // 생성된 채팅 룸
)

/**
 * 그룹 상세 화면 ViewModel
 * - 그룹 정보 조회
 * - 관계 그래프 조회 및 계산
 * - 사용자 선택 및 채팅 시작
 */
@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val getGroupDetailUseCase: GetGroupDetailUseCase,
    private val getRelationshipGraphUseCase: GetRelationshipGraphUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    /**
     * 그룹 상세 정보와 관계 그래프 조회
     */
    fun initializeWithGroup(groupId: String, currentUserId: String) {
        // 테스트/목업 사용자인 경우 API 호출 없이 바로 목업 데이터 사용
        if (isTestUser(currentUserId)) {
            loadMockData(groupId)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

            try {
                // 1. 그룹 상세 정보 조회
                val groupResult = getGroupDetailUseCase(groupId)
                groupResult.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "그룹 정보 조회 실패"
                    )
                    return@launch
                }

                val group = groupResult.getOrNull() ?: return@launch

                // 2. 관계 그래프 조회
                val graphResult = getRelationshipGraphUseCase(groupId, currentUserId)
                graphResult.onSuccess { graph ->
                    _uiState.value = _uiState.value.copy(
                        group = group,
                        relationshipGraph = graph,
                        isLoading = false
                    )
                }

                graphResult.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        group = group,
                        isLoading = false,
                        errorMessage = e.message ?: "관계 그래프 조회 실패"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "알 수 없는 오류"
                )
            }
        }
    }

    /**
     * 테스트 사용자 여부 확인
     */
    private fun isTestUser(userId: String): Boolean {
        val normalized = userId.trim().lowercase()
        if (normalized.isEmpty()) return true

        return normalized in setOf(
            "test_user",
            "mock_user",
            "test",
            "mock",
            "local_test_user"
        ) ||
            normalized.startsWith("local_test_") ||
            normalized.startsWith("test_user_") ||
            normalized.startsWith("mock_user_") ||
            normalized.startsWith("test-") ||
            normalized.startsWith("mock-")
    }

    /**
     * 목업 데이터 로드 (테스트용)
     */
    private fun loadMockData(groupId: String) {
        // 목업 그룹 정보
        val mockGroup = Group(
            id = groupId,
            name = if (groupId.contains("molip", ignoreCase = true)) "몰입캠프 분반4" else "테스트 그룹",
            description = "테스트용 목업 그룹입니다",
            memberCount = 21,
            activity = "활발함",
            tags = emptyList(),
            imageUrl = "",
            lastActivityDate = "2024-01-10",
            messageCount = 156,
            matchPercentage = 85,
            region = "서울",
            memberAge = "20대",
            isJoined = true
        )

        // API 호출 없이 바로 목업 상태로 설정 (에러 메시지로 목업 모드 표시)
        _uiState.value = _uiState.value.copy(
            group = mockGroup,
            relationshipGraph = null,
            isLoading = false,
            errorMessage = "mock_mode" // 목업 모드 표시용
        )
    }

    /**
     * 사용자 선택 (노드 클릭)
     */
    fun selectUser(userId: String) {
        _uiState.value = _uiState.value.copy(selectedUserId = userId)
    }

    /**
     * 사용자 선택 취소
     */
    fun deselectUser() {
        _uiState.value = _uiState.value.copy(selectedUserId = null)
    }

    /**
     * 채팅 시작 (selectedUserId와 현재 사용자 간의 채팅 룸 생성)
     */
    fun startChatWithSelectedUser(currentUserId: String) {
        val selectedUserId = _uiState.value.selectedUserId ?: return
        
        // 두 사용자의 ID를 정렬하여 고유한 채팅 룸 ID 생성
        val userIds = listOf(currentUserId, selectedUserId).sorted()
        val chatRoomId = "${userIds[0]}_${userIds[1]}"

        _uiState.value = _uiState.value.copy(chatRoomId = chatRoomId)
    }

    /**
     * 그룹 채팅 시작 (전체 그룹과의 채팅)
     */
    fun startGroupChat(groupId: String) {
        val chatRoomId = "group_$groupId"
        _uiState.value = _uiState.value.copy(chatRoomId = chatRoomId)
    }

    /**
     * 채팅 상태 초기화 (네비게이션 후)
     */
    fun resetChatState() {
        _uiState.value = _uiState.value.copy(chatRoomId = null)
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}
