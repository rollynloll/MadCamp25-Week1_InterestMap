package com.example.madclass01.presentation.group.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.presentation.group.component.ChatButtonComponent
import com.example.madclass01.presentation.group.component.GroupChatButtonComponent
import com.example.madclass01.presentation.group.component.GroupDetailHeaderComponent
import com.example.madclass01.presentation.group.component.RelationshipGraphComponent
import com.example.madclass01.presentation.group.viewmodel.GroupDetailViewModel

/**
 * 그룹 상세 화면 (Group Detail Screen)
 * - 헤더: 그룹 정보, 뒤로가기, QR 코드, 더보기
 * - 콘텐츠: 관계 그래프 (중앙 사용자 중심, 유사도 기반 거리)
 * - 하단: 채팅 버튼
 *
 * 그래프 특징:
 * - 중앙: 접속한 사용자 (자신)
 * - 주변: 그룹 내 다른 사용자들
 * - 거리: 코사인 유사도 기반 (가까울수록 취향이 비슷)
 * - 색상: 유사도에 따라 다름
 *   - 초록색: 유사도 높음 (0.5~1.0)
 *   - 주황색: 유사도 보통 (0.3~0.5)
 *   - 회색: 유사도 낮음 (0~0.3)
 */
@Composable
fun GroupDetailScreen(
    groupId: String,
    currentUserId: String,
    onBackPress: () -> Unit = {},
    onQRCodeClick: () -> Unit = {},
    onChatRoomCreated: (chatRoomId: String) -> Unit = {},
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 화면 초기화
    LaunchedEffect(groupId, currentUserId) {
        viewModel.initializeWithGroup(groupId, currentUserId)
    }

    // 채팅 룸 생성 감시
    LaunchedEffect(uiState.chatRoomId) {
        if (uiState.chatRoomId != null) {
            onChatRoomCreated(uiState.chatRoomId!!)
            viewModel.resetChatState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (uiState.isLoading) {
            // 로딩 상태
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color(0xFF667EEA))
                Text(
                    text = "그룹 정보를 불러오는 중...",
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else if (uiState.errorMessage.isNotEmpty()) {
            // 에러 상태
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "오류 발생",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
                Text(
                    text = uiState.errorMessage,
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else if (uiState.group != null && uiState.relationshipGraph != null) {
            // 정상 상태
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 헤더
                GroupDetailHeaderComponent(
                    groupName = uiState.group!!.name,
                    memberCount = uiState.group!!.memberCount,
                    activityStatus = "오늘 활동",
                    groupIcon = "👥",
                    onBackClick = onBackPress,
                    onQRCodeClick = onQRCodeClick,
                    onMoreClick = { /* TODO: 더보기 메뉴 */ }
                )

                // 관계 그래프
                RelationshipGraphComponent(
                    relationshipGraph = uiState.relationshipGraph!!,
                    selectedUserId = uiState.selectedUserId,
                    onNodeClick = { userId ->
                        viewModel.selectUser(userId)
                    },
                    onNodeLongClick = { userId ->
                        viewModel.selectUser(userId)
                    }
                )

                // 채팅 버튼
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color(0xFFFAFBFC))
                        .padding(vertical = 23.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (uiState.selectedUserId != null && 
                        uiState.selectedUserId != currentUserId) {
                        // 개인 채팅 버튼
                        val selectedUser = uiState.relationshipGraph!!.embeddings[uiState.selectedUserId]
                        ChatButtonComponent(
                            selectedUserName = selectedUser?.userName ?: "사용자",
                            onChatClick = {
                                viewModel.startChatWithSelectedUser(currentUserId)
                            }
                        )
                    } else {
                        // 그룹 채팅 버튼
                        GroupChatButtonComponent(
                            groupName = uiState.group!!.name,
                            onChatClick = {
                                viewModel.startGroupChat(groupId)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 선택된 사용자 프로필 시트 (모달)
 * - 사용자 정보 표시
 * - 채팅 버튼
 * - 팔로우 버튼 (선택사항)
 */
@Composable
fun SelectedUserProfileBottomSheet(
    userName: String,
    profileImageUrl: String? = null,
    similarity: Float = 0.5f,
    onChatClick: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    // BottomSheet 구현 (선택사항)
}
