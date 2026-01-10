package com.example.madclass01.presentation.group.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madclass01.domain.model.ChatMessage

@Composable
fun ChatHeader(
    groupName: String,
    memberCount: Int,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onMore: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "←",
                    color = Color(0xFF111827),
                    fontSize = 24.sp,
                    modifier = Modifier.clickable { onBack() }
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(text = groupName, color = Color(0xFF111827), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "${memberCount}명", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔍", color = Color(0xFF6B7280), fontSize = 18.sp, modifier = Modifier.clickable { onSearch() })
                Text(text = "⋮", color = Color(0xFF6B7280), fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp).clickable { onMore() })
            }
        }
    }
}

@Composable
fun DateDivider(dateText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .height(28.dp)
                .background(Color.White, shape = RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = dateText, color = Color(0xFF6B7280), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun OtherMessageBubble(userInitial: String, userName: String, text: String, timeText: String, bubbleWidth: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier.size(36.dp).background(Color(0xFFF59E0B), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(text = userInitial, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text = userName, color = Color(0xFF6B7280), fontSize = 12.sp)
            Box(
                modifier = Modifier
                    .width(bubbleWidth.dp)
                    .background(Color.White, shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(text = text, color = Color(0xFF111827), fontSize = 15.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            Text(text = timeText, color = Color(0xFF9CA3AF), fontSize = 11.sp)
        }
    }
}

@Composable
fun MyMessageBubble(text: String, timeReadText: String, bubbleWidth: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .width(bubbleWidth.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(text = text, color = Color.White, fontSize = 15.sp)
            }
            Text(text = timeReadText, color = Color(0xFF9CA3AF), fontSize = 11.sp)
        }
    }
}

@Composable
fun SystemMessageBadge(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .height(32.dp)
                .background(Color(0xFFFEF2F2), shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = Color(0xFF991B1B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ChatInputRow(
    inputText: String,
    onChangeText: (String) -> Unit,
    onPickImage: () -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Plus Button
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFF3F4F6), shape = CircleShape)
                .clickable { onPickImage() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "+", color = Color(0xFF6B7280), fontSize = 22.sp)
        }

        // Text Input (simple, placeholder)
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .height(44.dp)
                .weight(1f)
                .background(Color(0xFFF9FAFB), shape = CircleShape)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = inputText,
                onValueChange = onChangeText,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF111827), fontSize = 15.sp),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text(text = "메시지를 입력하세요...", color = Color(0xFF9CA3AF), fontSize = 15.sp)
                    }
                    innerTextField()
                }
            )
        }

        // Send Button
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(44.dp)
                .background(Color(0xFF667EEA), shape = CircleShape)
                .clickable { onSend() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "➤", color = Color.White, fontSize = 18.sp)
        }
    }
}
