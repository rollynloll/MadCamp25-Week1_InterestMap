package com.example.madclass01.presentation.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 채팅 버튼 컴포넌트
 * - 배경: 그라데이션 (#667EEA → #764BA2)
 * - 텍스트: "채팅"
 * - 아이콘: 메시지 아이콘 (원형)
 */
@Composable
fun ChatButtonComponent(
    selectedUserName: String? = null,
    onChatClick: () -> Unit = {}
) {
    val buttonText = if (selectedUserName != null) {
        "${selectedUserName}와 채팅"
    } else {
        "채팅"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(53.dp)
            .padding(horizontal = 43.dp)
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onChatClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // 메시지 아이콘
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💬",
                    fontSize = 16.sp
                )
            }

            // 텍스트
            Text(
                text = buttonText,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * 그룹 채팅 버튼
 */
@Composable
fun GroupChatButtonComponent(
    groupName: String,
    onChatClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(53.dp)
            .padding(horizontal = 43.dp)
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onChatClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // 메시지 아이콘
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💬",
                    fontSize = 16.sp
                )
            }

            // 텍스트
            Text(
                text = "$groupName 그룹 채팅",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
