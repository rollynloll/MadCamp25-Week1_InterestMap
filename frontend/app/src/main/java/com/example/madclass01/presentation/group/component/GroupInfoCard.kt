package com.example.madclass01.presentation.group.component

import androidx.compose.foundation.background
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
import com.example.madclass01.domain.model.Group

@Composable
fun GroupInfoCard(
    group: Group,
    memberCount: Int = 24,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(0.838f)  // 326/390
            .height(151.dp),
        color = Color(0xFFF9FAFB),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Group Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = Color(0xFF10B981),  // 초대는 초록색
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (group.id) {
                        else -> "👥"
                    },
                    fontSize = 28.sp
                )
            }
            
            // Group Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = group.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "${memberCount}명의 멤버",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

// Surface 컴포저블을 위한 import 추가
@Composable
fun Surface(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(0.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(color = color, shape = shape),
        contentAlignment = Alignment.TopCenter
    ) {
        content()
    }
}
