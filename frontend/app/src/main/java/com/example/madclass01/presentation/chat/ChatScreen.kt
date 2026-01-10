package com.example.madclass01.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
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
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String,
    val message: String,
    val timestamp: String,
    val isMe: Boolean,
    val avatarGradient: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatRoomId: String,
    chatRoomName: String = "채팅방",
    onBackPress: () -> Unit = {}
) {
    remember(chatRoomId) { Unit }

    var messageText by remember { mutableStateOf("") }
    
    // Mock data - 그룹 인원 수
    val memberCount = 8
    
    // Mock messages with gradient colors
    val messages = remember {
        listOf(
            ChatMessage(
                id = "1",
                userId = "user1",
                userName = "이OO",
                userAvatarUrl = "https://picsum.photos/seed/user1/200/200",
                message = "오늘 한강 러닝 어때요?",
                timestamp = "오후 2:30",
                isMe = false,
                avatarGradient = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
            ),
            ChatMessage(
                id = "2",
                userId = "user2",
                userName = "김OO",
                userAvatarUrl = "https://picsum.photos/seed/user2/200/200",
                message = "좋아요! 몇 시에 모일까요?",
                timestamp = "오후 2:32",
                isMe = false,
                avatarGradient = listOf(Color(0xFF10B981), Color(0xFF059669))
            ),
            ChatMessage(
                id = "3",
                userId = "me",
                userName = "나",
                userAvatarUrl = "https://picsum.photos/seed/me/200/200",
                message = "5시쯤 만날까요? 🏃‍♂️",
                timestamp = "오후 2:35",
                isMe = true,
                avatarGradient = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
            ),
            ChatMessage(
                id = "4",
                userId = "user3",
                userName = "박OO",
                userAvatarUrl = "https://picsum.photos/seed/user3/200/200",
                message = "안녕하세요! 저도 같이 해도 될까요?",
                timestamp = "오후 2:40",
                isMe = false,
                avatarGradient = listOf(Color(0xFFEF4444), Color(0xFFDC2626))
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .background(Color.White)
    ) {
        // Chat Header (60dp height)
        ChatHeader(
            groupName = chatRoomName,
            memberCount = memberCount,
            onBackPress = onBackPress
        )
        
        // Messages Area (weight to fill space)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF9FAFB))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Divider
            item {
                DateDivider(date = "2024년 1월 15일")
            }
            
            // Messages
            items(messages) { message ->
                if (message.isMe) {
                    MyMessageItem(message = message)
                } else {
                    OtherMessageItem(message = message)
                }
                
                // System message after third message
                if (message.id == "3") {
                    Spacer(modifier = Modifier.height(0.dp))
                    SystemMessage(text = "박OO님이 입장했습니다")
                }
            }
        }
        
        // Input Area (68dp height)
        ChatInputArea(
            messageText = messageText,
            onMessageTextChange = { messageText = it },
            onSendClick = {
                if (messageText.isNotBlank()) {
                    // TODO: 메시지 전송 로직
                    messageText = ""
                }
            }
        )
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
            
            // Right - More menu
            IconButton(
                onClick = { /* TODO: More menu */ },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "더보기",
                    tint = Color(0xFF6B7280),
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
fun OtherMessageItem(message: ChatMessage) {
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
                    brush = Brush.linearGradient(
                        colors = message.avatarGradient,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(100f, 100f)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = message.userAvatarUrl,
                contentDescription = "프로필 사진",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Message content
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = message.userName,
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
                        text = message.message,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                
                // Timestamp
                Text(
                    text = message.timestamp,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MyMessageItem(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Timestamp (no read count as per user request)
            Text(
                text = message.timestamp,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            // Message bubble with gradient
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 4.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = Color.Transparent,
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier.background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF667EEA),
                                Color(0xFF764BA2)
                            )
                        )
                    )
                ) {
                    Text(
                        text = message.message,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SystemMessage(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFEF2F2)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF991B1B)
                )
            }
        }
    }
}

@Composable
fun ChatInputArea(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit
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
            // Plus Button (44dp)
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
                    onClick = { /* TODO: Attachment */ },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "첨부",
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
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = messageText,
                    onValueChange = onMessageTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = Color(0xFF111827)
                    ),
                    decorationBox = { innerTextField ->
                        if (messageText.isEmpty()) {
                            Text(
                                text = "메시지를 입력하세요...",
                                fontSize = 15.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        innerTextField()
                    }
                )
            }
            
            // Send Button (44dp) with gradient
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF667EEA),
                                Color(0xFF764BA2)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onSendClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "전송",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
