package com.example.madclass01.presentation.group.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.madclass01.presentation.group.component.ChatButtonComponent
import com.example.madclass01.presentation.group.component.GroupChatButtonComponent
import com.example.madclass01.presentation.group.component.GroupDetailHeaderComponent
import com.example.madclass01.presentation.group.component.RelationshipGraphComponent
import com.example.madclass01.presentation.group.viewmodel.GroupDetailViewModel
import com.example.madclass01.R

@Composable
fun GroupDetailScreen(
    groupId: String,
    currentUserId: String,
    onBackPress: () -> Unit = {},
    onQRCodeClick: (com.example.madclass01.domain.model.Group) -> Unit = {},
    onChatRoomCreated: (chatRoomId: String, groupName: String) -> Unit = { _, _ -> },
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 화면 초기화
    LaunchedEffect(groupId, currentUserId) {
        viewModel.initializeWithGroup(groupId, currentUserId)
    }

    // 채팅방 생성 감시
    LaunchedEffect(uiState.chatRoomId) {
        if (uiState.chatRoomId != null) {
            val groupName = uiState.group?.name ?: if (groupId.contains("molip", ignoreCase = true)) {
                "몰입캠프 분반4"
            } else {
                "그룹 채팅"
            }
            onChatRoomCreated(uiState.chatRoomId!!, groupName)
            viewModel.resetChatState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(Color.White)
    ) {
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
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
            BackHandler(enabled = true) {
                onBackPress()
            }

            val isMockMode = uiState.errorMessage == "mock_mode"
            // 테스트 사용자는 에러 다이얼로그 안 띄움, 실제 사용자는 무조건 띄움
            var showErrorDialog by remember(uiState.errorMessage, uiState.isTestUser) { 
                mutableStateOf(!uiState.isTestUser && !isMockMode) 
            }

            val fallbackGroupName = if (groupId.contains("molip", ignoreCase = true)) {
                "몰입캠프 분반4"
            } else {
                "그룹 상세"
            }
            val fallbackMemberCount = if (groupId.contains("molip", ignoreCase = true)) 21 else 0

            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            viewModel.startGroupChat(groupId)
                        },
                        modifier = Modifier
                            .padding(bottom = 16.dp, end = 16.dp),
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 335.dp, height = 56.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Absolute.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                                    contentDescription = "채팅",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "채팅",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    GroupDetailHeaderComponent(
                        groupName = fallbackGroupName,
                        memberCount = fallbackMemberCount,
                        activityStatus = if (isMockMode) "테스트 모드 (목업 데이터)" else "목업 데이터",
                        groupIcon = "👥",
                        onBackClick = onBackPress,
                        onQRCodeClick = {
                            // 목업 모드에서는 더미 그룹 전달
                            val dummyGroup = com.example.madclass01.domain.model.Group(
                                id = groupId,
                                name = fallbackGroupName,
                                description = "",
                                memberCount = fallbackMemberCount
                            )
                            onQRCodeClick(dummyGroup)
                        }
                    )

                    MockRelationshipGraphCanvas(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .align(Alignment.CenterHorizontally),
                        currentUserName = "나",
                        currentUserImageModel = "https://picsum.photos/seed/me_star/200/200"
                    )
                }
            }

            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = onBackPress,
                    title = {
                        Text(
                            text = "오류 발생",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(text = uiState.errorMessage)
                    },
                    confirmButton = {
                        Button(onClick = onBackPress) {
                            Text(text = "확인")
                        }
                    }
                )
            }
        } else if (uiState.group != null && uiState.relationshipGraph != null) {
            // 정상 상태
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            if (uiState.selectedUserId != null && uiState.selectedUserId != currentUserId) {
                                viewModel.startChatWithSelectedUser(currentUserId)
                            } else {
                                viewModel.startGroupChat(groupId)
                            }
                        },
                        modifier = Modifier
                            .padding(bottom = 16.dp, end = 16.dp),
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 335.dp, height = 56.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                                    contentDescription = "채팅",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "채팅",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // 헤더
                    GroupDetailHeaderComponent(
                        groupName = uiState.group!!.name,
                        memberCount = uiState.group!!.memberCount,
                        activityStatus = "오늘 활동",
                        groupIcon = "👥",
                        onBackClick = onBackPress,
                        onQRCodeClick = { onQRCodeClick(uiState.group!!) }
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
                }
            }

            // 선택된 사용자 프로필 표시
            if (uiState.selectedUserId != null && uiState.selectedUserId != currentUserId) {
                val selectedUser = uiState.relationshipGraph!!.embeddings[uiState.selectedUserId]
                val selectedNode = uiState.relationshipGraph!!.otherUserNodes.find { it.userId == uiState.selectedUserId }
                if (selectedUser != null && selectedNode != null) {
                    SelectedUserProfileBottomSheet(
                        userName = selectedUser.userName,
                        profileImageUrl = selectedUser.profileImageUrl,
                        similarity = selectedNode.similarityScore,
                        onChatClick = {
                            viewModel.startChatWithSelectedUser(currentUserId)
                        },
                        onDismiss = {
                            viewModel.deselectUser()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MockRelationshipGraphCanvas(
    modifier: Modifier = Modifier,
    currentUserName: String,
    currentUserImageModel: Any? = null
) {
    // CSS 스펙에 맞춘 예시 목업 배치 (390x520 캔버스, absolute 배치)
    val avatar1 = "https://picsum.photos/seed/node1/200/200"
    val avatar2 = "https://picsum.photos/seed/node2/200/200"
    val avatar3 = "https://picsum.photos/seed/node3/200/200"
    val avatar4 = "https://picsum.photos/seed/node4/200/200"
    val avatar5 = "https://picsum.photos/seed/node5/200/200"

    Box(
        modifier = modifier
            .size(width = 390.dp, height = 520.dp)
            .background(Color(0xFFFAFBFC)),
        contentAlignment = Alignment.TopStart
    ) {
        // Center Node (Me)
        StarNode(
            modifier = Modifier.offset(x = 159.dp, y = 224.dp),
            name = currentUserName,
            imageModel = currentUserImageModel
        )

        // Node 1
        PlanetNode(
            modifier = Modifier.offset(x = 129.dp, y = 182.dp),
            size = 56.dp,
            backgroundColor = Color(0xFF10B981),
            borderWidth = 3.dp,
            elevation = 12.dp,
            name = "김OO",
            textColor = Color.White,
            imageModel = avatar1
        )

        // Node 2
        PlanetNode(
            modifier = Modifier.offset(x = 204.dp, y = 182.dp),
            size = 56.dp,
            backgroundColor = Color(0xFF10B981),
            borderWidth = 3.dp,
            elevation = 12.dp,
            name = "이OO",
            textColor = Color.White,
            imageModel = avatar2
        )

        // Node 3
        PlanetNode(
            modifier = Modifier.offset(x = 230.dp, y = 381.dp),
            size = 48.dp,
            backgroundColor = Color(0xFFF59E0B),
            borderWidth = 2.dp,
            elevation = 10.dp,
            name = "박OO",
            textColor = Color.White,
            imageModel = avatar3
        )

        // Node 4
        PlanetNode(
            modifier = Modifier.offset(x = 93.dp, y = 381.dp),
            size = 48.dp,
            backgroundColor = Color(0xFFF59E0B),
            borderWidth = 2.dp,
            elevation = 10.dp,
            name = "최OO",
            textColor = Color.White,
            imageModel = avatar4
        )

        // Node 5
        PlanetNode(
            modifier = Modifier.offset(x = 313.dp, y = 469.dp),
            size = 40.dp,
            backgroundColor = Color(0xFFE5E7EB),
            borderWidth = 2.dp,
            elevation = 8.dp,
            name = "정OO",
            textColor = Color(0xFF6B7280),
            imageModel = avatar5
        )
    }
}

@Composable
private fun StarNode(
    modifier: Modifier = Modifier,
    name: String,
    imageModel: Any? = null
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .shadow(elevation = 20.dp, shape = CircleShape, clip = false)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                ),
                shape = CircleShape
            )
            .border(4.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageModel ?: R.drawable.omo,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Text(
            text = name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PlanetNode(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp,
    backgroundColor: Color,
    borderWidth: androidx.compose.ui.unit.Dp,
    elevation: androidx.compose.ui.unit.Dp,
    name: String,
    textColor: Color,
    imageModel: Any? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = elevation, shape = CircleShape, clip = false)
            .background(backgroundColor, CircleShape)
            .border(borderWidth, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageModel ?: R.drawable.omo,
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Text(
            text = name,
            color = textColor,
            fontSize = when {
                size >= 56.dp -> 13.sp
                size >= 48.dp -> 12.sp
                else -> 11.sp
            },
            fontWeight = if (size >= 56.dp) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * 선택된 사용자 프로필 시트(모달)
 * - 사용자 정보 표시
 * - 채팅 버튼
 * - 유사도 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedUserProfileBottomSheet(
    userName: String,
    profileImageUrl: String? = null,
    similarity: Float = 0.5f,
    onChatClick: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 헤더 (닫기 버튼)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = Color(0xFF595959)
                    )
                }
            }

            // 프로필 이미지
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(elevation = 12.dp, shape = CircleShape, clip = false)
                    .background(Color(0xFFF5F5F5), CircleShape)
                    .border(4.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "프로필 사진",
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            // 이름
            Text(
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(top = 16.dp)
            )

            // 유사도 표시
            Surface(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFAFBFC)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "취향 유사도",
                            fontSize = 14.sp,
                            color = Color(0xFF8C8C8C)
                        )
                        Text(
                            text = "${(similarity * 100).toInt()}% 일치",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                similarity >= 0.7 -> Color(0xFF10B981)
                                similarity >= 0.5 -> Color(0xFF10B981)
                                similarity >= 0.3 -> Color(0xFFF59E0B)
                                else -> Color(0xFF8C8C8C)
                            },
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Text(
                        text = when {
                            similarity >= 0.7 -> "매우 유사"
                            similarity >= 0.5 -> "유사"
                            similarity >= 0.3 -> "보통"
                            else -> "다름"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF595959)
                    )
                }
            }

            // 채팅 버튼
            Button(
                onClick = {
                    onChatClick()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9945)
                )
            ) {
                Text(
                    text = "$userName 와 채팅하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // 하단 여백
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

