package com.example.madclass01.presentation.group.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.madclass01.domain.model.ChatMessage
import com.example.madclass01.presentation.group.component.*
import com.example.madclass01.presentation.group.viewmodel.GroupChatUiState
import com.example.madclass01.presentation.group.viewmodel.GroupChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GroupChatScreen(
    groupId: String,
    groupName: String,
    memberCount: Int,
    currentUserId: String,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onMore: () -> Unit,
    viewModel: GroupChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(groupId) {
        viewModel.initialize(groupId, groupName, memberCount, currentUserId)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        ChatHeader(groupName, memberCount, onBack, onSearch, onMore)

        // Messages Area
        Box(modifier = Modifier.fillMaxWidth().height(600.dp).background(Color(0xFFF9FAFB))) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Optional: date divider based on timestamps
                item { DateDivider(formatDate(System.currentTimeMillis())) }

                items(uiState.messages) { msg ->
                    when (msg.type) {
                        ChatMessage.MessageType.SYSTEM -> {
                            SystemMessageBadge(text = msg.content ?: "")
                        }
                        ChatMessage.MessageType.TEXT -> {
                            if (msg.userId == currentUserId) {
                                MyMessageBubble(text = msg.content ?: "", timeReadText = formatTime(msg.timestamp) + " · 읽음 ${msg.readCount}", bubbleWidth = 155)
                            } else {
                                val initial = (msg.userName ?: "").take(1).ifEmpty { "?" }
                                OtherMessageBubble(userInitial = initial, userName = msg.userName ?: "", text = msg.content ?: "", timeText = formatTime(msg.timestamp), bubbleWidth = 195)
                            }
                        }
                        ChatMessage.MessageType.IMAGE -> {
                            // Simple representation of image message
                            if (msg.userId == currentUserId) {
                                MyMessageBubble(text = "[이미지]", timeReadText = formatTime(msg.timestamp) + " · 읽음 ${msg.readCount}", bubbleWidth = 155)
                            } else {
                                val initial = (msg.userName ?: "").take(1).ifEmpty { "?" }
                                OtherMessageBubble(userInitial = initial, userName = msg.userName ?: "", text = "[이미지]", timeText = formatTime(msg.timestamp), bubbleWidth = 195)
                            }
                        }
                    }
                }
            }
        }

        // Input Area
        val context = LocalContext.current
        val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val bytes = readBytes(context, uri)
                val name = uri.lastPathSegment ?: "image.jpg"
                viewModel.sendImage(currentUserId, bytes, name)
            }
        }
        ChatInputRow(
            inputText = uiState.inputText,
            onChangeText = { viewModel.updateInput(it) },
            onPickImage = { imagePicker.launch("image/*") },
            onSend = { viewModel.sendText(currentUserId) }
        )
    }
}

private fun formatDate(ts: Long): String {
    val sdf = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
    return sdf.format(Date(ts))
}

private fun formatTime(ts: Long): String {
    val sdf = SimpleDateFormat("a h:mm", Locale.KOREA)
    return sdf.format(Date(ts))
}

private fun readBytes(context: Context, uri: Uri): ByteArray {
    return context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
}
