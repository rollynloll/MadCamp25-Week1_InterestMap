package com.example.madclass01.presentation.profile.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    onLoadingComplete: () -> Unit
) {
    var loadingMessage by remember { mutableStateOf("이미지 분석 중입니다") }
    
    LaunchedEffect(Unit) {
        delay(1000)
        loadingMessage = "AI가 취향을 학습하고 있습니다"
        delay(1500)
        loadingMessage = "핵심 키워드를 추출 중입니다"
        delay(1500)
        onLoadingComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 진행률 표시
            CircularProgressIndicator(
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 32.dp),
                color = Color(0xFFFF9945),
                strokeWidth = 4.dp
            )
            
            // 메인 텍스트
            Text(
                text = "취향 분석 중",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // 상세 메시지
            Text(
                text = loadingMessage,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // 로딩 닷
            LoadingDots()
        }
    }
}

@Composable
fun LoadingDots() {
    var animationState by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            animationState = (animationState + 1) % 4
        }
    }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (index < animationState) Color(0xFFFF9945) else Color(0xFFDDDDDD),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}
