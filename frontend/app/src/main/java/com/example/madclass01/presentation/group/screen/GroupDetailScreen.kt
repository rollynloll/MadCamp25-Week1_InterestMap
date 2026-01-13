package com.example.madclass01.presentation.group.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.madclass01.R
import com.example.madclass01.presentation.group.component.GroupDetailHeaderComponent
import com.example.madclass01.presentation.group.component.RelationshipGraphComponent
import com.example.madclass01.presentation.group.viewmodel.GroupDetailViewModel
import kotlin.math.pow

@Composable
fun GroupDetailScreen(
    groupId: String,
    currentUserId: String,
    onBackPress: () -> Unit = {},
    onQRCodeClick: (com.example.madclass01.domain.model.Group) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    onChatRoomCreated: (chatRoomId: String, groupName: String, memberCount: Int) -> Unit = { _, _, _ -> },
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle system back press
    BackHandler(onBack = onBackPress)

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
            val memberCount = uiState.group?.memberCount ?: 0
            onChatRoomCreated(uiState.chatRoomId!!, groupName, memberCount)
            viewModel.resetChatState()
        }
    }

    val isMockMode = uiState.errorMessage == "mock_mode"
    val hasError = uiState.errorMessage.isNotEmpty() && !isMockMode
    
    // 에러 발생 시 뒤로가기 핸들링
    if (hasError) {
        BackHandler(onBack = onBackPress)
    }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            // 로딩 중이 아니고 에러가 없거나(혹은 목업 모드일 때) FAB 표시
            if (!uiState.isLoading && (!hasError || isMockMode)) {
                GradientExtendedFloatingActionButton(
                    onClick = {
                        if (uiState.selectedUserId != null && uiState.selectedUserId != currentUserId) {
                            viewModel.startChatWithSelectedUser(currentUserId)
                        } else {
                            viewModel.startGroupChat(groupId)
                        }
                    },
                    text = if (uiState.selectedUserId != null && uiState.selectedUserId != currentUserId) "1:1 채팅하기" else "그룹 채팅방 입장",
                    icon = Icons.AutoMirrored.Filled.Chat
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                LoadingView()
            } else if (hasError) {
                ErrorView(
                    errorMessage = uiState.errorMessage,
                    onRetry = { viewModel.initializeWithGroup(groupId, currentUserId) },
                    onBack = onBackPress
                )
            } else {
                // 정상 콘텐츠 (또는 목업 모드)
                val groupName = uiState.group?.name ?: if (isMockMode) "몰입캠프 분반4" else "그룹 상세"
                val memberCount = uiState.group?.memberCount ?: if (isMockMode) 21 else 0
                val activityStatus = if (isMockMode) "테스트 모드" else "오늘 활동"
                val iconType = uiState.group?.iconType ?: "users"
                val groupIcon = when (iconType) {
                    "users" -> "👥"
                    "coffee" -> "☕"
                    "camera" -> "📷"
                    "mountain" -> "⛰️"
                    "music" -> "🎵"
                    "book" -> "📚"
                    "sports" -> "⚽"
                    "food" -> "🍔"
                    else -> "👥"
                }
                val profileImageUrl = uiState.group?.imageUrl

                Column(modifier = Modifier.fillMaxSize()) {
                    // 헤더
                    GroupDetailHeaderComponent(
                        groupName = groupName,
                        memberCount = memberCount,
                        activityStatus = activityStatus,
                        groupIcon = groupIcon,
                        profileImageUrl = profileImageUrl,
                        onBackClick = onBackPress,
                        onQRCodeClick = {
                            val targetGroup = uiState.group ?: com.example.madclass01.domain.model.Group(
                                id = groupId,
                                name = groupName,
                                description = "",
                                memberCount = memberCount
                            )
                            onQRCodeClick(targetGroup)
                        }
                    )

                    // 그래프 영역 (남은 공간 채우기)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFFAFBFC)) // 그래프 배경색
                    ) {
                        ZoomableGraphContainer(
                            modifier = Modifier.fillMaxSize()
                        ) { scale ->
                            val nodeScale = (1f / scale.pow(1.3f)).coerceIn(0.2f, 2f)
                            
                            if (isMockMode || uiState.relationshipGraph == null) {
                                MockRelationshipGraphCanvas(
                                    currentUserName = "나",
                                    currentUserImageModel = "https://picsum.photos/seed/me_star/200/200",
                                    nodeScale = nodeScale
                                )
                            } else {
                                RelationshipGraphComponent(
                                    relationshipGraph = uiState.relationshipGraph!!,
                                    selectedUserId = uiState.selectedUserId,
                                    nodeScale = nodeScale,
                                    onNodeClick = { userId -> viewModel.selectUser(userId) },
                                    onNodeLongClick = { userId -> viewModel.selectUser(userId) }
                                )
                            }
                        }
                    }
                }

                // 선택된 사용자 바텀 시트
                if (uiState.selectedUserId != null && uiState.selectedUserId != currentUserId && uiState.relationshipGraph != null) {
                    val selectedUser = uiState.relationshipGraph!!.embeddings[uiState.selectedUserId]
                    val selectedNode = uiState.relationshipGraph!!.otherUserNodes.find { it.userId == uiState.selectedUserId }
                    
                    if (selectedUser != null && selectedNode != null) {
                        SelectedUserProfileBottomSheet(
                            userName = selectedUser.userName,
                            profileImageUrl = selectedUser.profileImageUrl,
                            similarity = selectedNode.similarityScore,
                            onProfileClick = { onProfileClick(uiState.selectedUserId!!) },
                            onDismiss = { viewModel.deselectUser() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "그룹 정보를 불러오는 중...",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "오류가 발생했습니다",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack) {
                Text("뒤로 가기")
            }
            Button(onClick = onRetry) {
                Text("다시 시도")
            }
        }
    }
}

@Composable
fun GradientExtendedFloatingActionButton(
    onClick: () -> Unit,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        shadowElevation = 8.dp,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                    )
                )
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ZoomableGraphContainer(
    modifier: Modifier = Modifier,
    content: @Composable (Float) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.1f, 8f)
        offset += panChange
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .transformable(transformState),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                }
        ) {
            content(scale)
        }
    }
}

@Composable
private fun MockRelationshipGraphCanvas(
    currentUserName: String,
    currentUserImageModel: Any? = null,
    nodeScale: Float = 1f
) {
    // 캔버스 크기를 정의 (좌표계 기준)
    val canvasWidth = 390.dp
    val canvasHeight = 520.dp

    Box(
        modifier = Modifier
            .size(canvasWidth, canvasHeight),
        contentAlignment = Alignment.TopStart
    ) {
        // Center Node (Me)
        StarNode(
            modifier = Modifier.align(Alignment.Center),
            name = currentUserName,
            imageModel = currentUserImageModel,
            nodeScale = nodeScale
        )

        // 주변 노드들 (절대 좌표 대신 상대적 위치나 정해진 오프셋 사용)
        // 여기서는 기존 오프셋을 유지하되, 캔버스 중앙을 기준으로 배치하는 것이 좋겠지만
        // 간단히 기존 오프셋을 유지합니다.
        
        // Node 1
        PlanetNode(
            modifier = Modifier.offset(x = 129.dp, y = 182.dp),
            size = 56.dp,
            backgroundColor = Color(0xFF10B981),
            borderWidth = 3.dp,
            elevation = 12.dp,
            name = "김OO",
            textColor = Color.White,
            imageModel = "https://picsum.photos/seed/node1/200/200",
            nodeScale = nodeScale
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
            imageModel = "https://picsum.photos/seed/node2/200/200",
            nodeScale = nodeScale
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
            imageModel = "https://picsum.photos/seed/node3/200/200",
            nodeScale = nodeScale
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
            imageModel = "https://picsum.photos/seed/node4/200/200",
            nodeScale = nodeScale
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
            imageModel = "https://picsum.photos/seed/node5/200/200",
            nodeScale = nodeScale
        )
    }
}

@Composable
private fun StarNode(
    modifier: Modifier = Modifier,
    name: String,
    imageModel: Any? = null,
    nodeScale: Float = 1f
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .graphicsLayer {
                scaleX = nodeScale
                scaleY = nodeScale
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
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
    imageModel: Any? = null,
    nodeScale: Float = 1f
) {
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = nodeScale
                scaleY = nodeScale
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedUserProfileBottomSheet(
    userName: String,
    profileImageUrl: String? = null,
    similarity: Float = 0.5f,
    onProfileClick: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 프로필 이미지
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(elevation = 4.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "프로필 사진",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 이름
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 유사도 표시
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF3F4F6) // Neutral background
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "취향 유사도",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = "${(similarity * 100).toInt()}% 일치",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = when {
                                similarity >= 0.7f -> Color(0xFF10B981) // Green
                                similarity >= 0.5f -> Color(0xFF3B82F6) // Blue
                                else -> Color(0xFFF59E0B) // Amber
                            }
                        )
                    }

                    Text(
                        text = when {
                            similarity >= 0.7f -> "🔥 천생연분"
                            similarity >= 0.5f -> "✨ 비슷해요"
                            else -> "🤝 알아가요"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 프로필 보기 버튼
            Button(
                onClick = {
                    onProfileClick()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9945)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "프로필 보기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
