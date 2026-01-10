package com.example.madclass01.presentation.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CopyLinkButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.838f)  // 326/390
            .height(52.dp)
            .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = Color(0xFFE5E7EB),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = true) { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "🔗",
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "초대 링크 복사",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
        }
    }
}

@Composable
fun ShareButtonsRow(
    onKakaoClick: () -> Unit,
    onInstagramClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(0.838f)
            .height(72.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShareButton(
            icon = "💬",
            label = "카카오톡",
            labelColor = Color(0xFF3C1E1E),
            backgroundColor = Color(0xFFFEE500),
            onClick = onKakaoClick,
            modifier = Modifier.weight(1f)
        )
        
        ShareButton(
            icon = "📸",
            label = "인스타",
            labelColor = Color(0xFF111827),
            backgroundColor = Color(0xFFE1306C),
            onClick = onInstagramClick,
            modifier = Modifier.weight(1f)
        )
        
        ShareButton(
            icon = "⋯",
            label = "더보기",
            labelColor = Color(0xFF6B7280),
            backgroundColor = Color(0xFFF3F4F6),
            onClick = onMoreClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ShareButton(
    icon: String,
    label: String,
    labelColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(enabled = true) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(backgroundColor, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
        }
        
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = labelColor
        )
    }
}
