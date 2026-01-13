package com.example.madclass01.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.domain.model.ChatMessage as DomainChatMessage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatRoomId: String,
    chatRoomName: String = "채팅방",
    memberCount: Int = 0,
    userId: String,
    onBackPress: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val nearBottomThreshold = 2
    val messagesByDate = remember(uiState.messages) {
        uiState.messages.groupBy { message -> formatDateDivider(message.timestamp) }
    }
    val totalItems = messagesByDate.size + uiState.messages.size
    val isNearBottom by remember {
        derivedStateOf {
            val total = listState.layoutInfo.totalItemsCount
            if (total == 0) {
                true
            } else {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisible >= total - 1 - nearBottomThreshold
            }
        }
    }
    var unreadCount by remember { mutableStateOf(0) }
    var previousMessageCount by remember { mutableStateOf(0) }
    
    // 사진 선택을 위한 이미지 피커
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            // URI를 File로 변환해서 업로드
            val file = uriToFile(context, it)
            if (file != null) {
                viewModel.sendImageMessage(chatRoomId, userId, file)
            }
        }
    }

    // 채팅방 초기화
    LaunchedEffect(chatRoomId, userId) {
        viewModel.initializeChatRoom(chatRoomId, userId)
    }

    // 새 메시지 수신 시 스크롤
    val lastMessageId = uiState.messages.lastOrNull()?.id
    LaunchedEffect(lastMessageId) {
        if (lastMessageId != null && totalItems > 0) {
            val newItems = (uiState.messages.size - previousMessageCount).coerceAtLeast(0)
            if (isNearBottom) {
                listState.animateScrollToItem(totalItems - 1)
                unreadCount = 0
            } else if (newItems > 0) {
                unreadCount += newItems
            }
            previousMessageCount = uiState.messages.size
        }
    }

    LaunchedEffect(isNearBottom) {
        if (isNearBottom) {
            unreadCount = 0
        }
    }

    // 에러 표시
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            android.util.Log.e("ChatScreen", "Error: $it")
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .background(Color.White)
    ) {
        // Chat Header
        ChatHeader(
            groupName = chatRoomName,
            memberCount = memberCount,
            onBackPress = onBackPress
        )

        if (uiState.isLoading && uiState.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF9945))
            }
        } else {
            // Messages Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFF9FAFB))
                    .padding(16.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 날짜별로 그룹화하여 표시
                    messagesByDate.forEach { (date, messagesForDate) ->
                        // 날짜 구분선
                        item(key = "date_$date") {
                            DateDivider(date = date)
                        }

                        // 해당 날짜의 메시지들
                        items(
                            items = messagesForDate,
                            key = { it.id }
                        ) { message ->
                            val isMe = message.userId == userId
                            val timestamp = formatTimestamp(message.timestamp)
                            val isImageMessage =
                                message.type == DomainChatMessage.MessageType.IMAGE &&
                                    !message.imageUrl.isNullOrBlank()
                            if (isMe) {
                                if (isImageMessage) {
                                    MyImageMessageItem(
                                        imageUrl = message.imageUrl ?: "",
                                        timestamp = timestamp
                                    )
                                } else {
                                    MyMessageItem(
                                        message = message.content ?: "",
                                        timestamp = timestamp
                                    )
                                }
                            } else {
                                if (isImageMessage) {
                                    OtherImageMessageItem(
                                        userName = message.userName ?: "알 수 없음",
                                        imageUrl = message.imageUrl ?: "",
                                        timestamp = timestamp,
                                        avatarUrl = null
                                    )
                                } else {
                                    OtherMessageItem(
                                        userName = message.userName ?: "알 수 없음",
                                        message = message.content ?: "",
                                        timestamp = timestamp,
                                        avatarUrl = null // TODO: 프로필 사진 URL
                                    )
                                }
                            }
                        }
                    }
                }

                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        ExtendedFloatingActionButton(
                        onClick = {
                            if (totalItems > 0) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(totalItems - 1)
                                }
                            }
                            unreadCount = 0
                        },
                        containerColor = Color(0xFFFF9945),
                        contentColor = Color.White,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "새 메시지",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "새 메시지",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                        val badgeText = if (unreadCount > 99) "99+" else unreadCount.toString()
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-6).dp)
                                .background(color = Color(0xFFE11D48), shape = CircleShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Input Area
        ChatInputArea(
            messageText = messageText,
            onMessageTextChange = { messageText = it },
            onSendClick = {
                if (messageText.isNotBlank()) {
                    viewModel.sendMessage(chatRoomId, userId, messageText)
                    messageText = ""
                }
            },
            onImageClick = {
                imagePickerLauncher.launch("image/*")
            },
            enabled = !uiState.isSending
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("a h:mm", Locale.KOREAN)
    return sdf.format(Date(timestamp))
}

private fun formatDateDivider(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREAN)
    return sdf.format(Date(timestamp))
}

// URI를 File로 변환하는 헬퍼 함수
private fun uriToFile(context: android.content.Context, uri: android.net.Uri): java.io.File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = java.io.File(context.cacheDir, "chat_image_${System.currentTimeMillis()}.jpg")
        val outputStream = java.io.FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        android.util.Log.e("ChatScreen", "Error converting URI to File", e)
        null
    }
}

@Composable
fun ChatHeader(
    groupName: String,
    memberCount: Int,
    onBackPress: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left - Back button + Chat info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBackPress,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color(0xFF111827),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = groupName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "${memberCount}명",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
            
            // Right - Add member button
            IconButton(
                onClick = { /* TODO: Add member */ },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "초대하기",
                    tint = Color(0xFF111827),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun DateDivider(date: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = date,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
fun OtherMessageItem(
    userName: String,
    message: String,
    timestamp: String,
    avatarUrl: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // Avatar (36dp)
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = Color(0xFFFF9945),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "프로필 사진",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = userName.take(1),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Message content
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = userName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Message bubble
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = message,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                
                // Timestamp
                Text(
                    text = timestamp,
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

@Composable
fun MyMessageItem(
    message: String,
    timestamp: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        // Timestamp
        Text(
            text = timestamp,
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // Message bubble
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 4.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = Color(0xFFFF9945)
        ) {
            Text(
                text = message,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun OtherImageMessageItem(
    userName: String,
    imageUrl: String,
    timestamp: String,
    avatarUrl: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = Color(0xFFFF9945),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "프로필 사진",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = userName.take(1),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = userName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "이미지 메시지",
                        modifier = Modifier.size(180.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = timestamp,
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

@Composable
fun MyImageMessageItem(
    imageUrl: String,
    timestamp: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = timestamp,
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 4.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "이미지 메시지",
                modifier = Modifier.size(180.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun ChatInputArea(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onImageClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // 사진 버튼 (44dp)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = Color(0xFFF3F4F6),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onImageClick,
                    enabled = enabled,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "사진 전송",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            // Text Input Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(
                        color = Color(0xFFF9FAFB),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE5E7EB),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = messageText,
                    onValueChange = onMessageTextChange,
                    enabled = enabled,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = Color(0xFF111827)
                    ),
                    decorationBox = { innerTextField ->
                        if (messageText.isEmpty()) {
                            Text(
                                text = "메시지를 입력하세요",
                                fontSize = 15.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Send Button (44dp)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (messageText.isNotBlank() && enabled) Color(0xFFFF9945) else Color(0xFFF3F4F6),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onSendClick,
                    enabled = messageText.isNotBlank() && enabled,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "전송",
                        tint = if (messageText.isNotBlank() && enabled) Color.White else Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
