package com.example.madclass01.presentation.profile.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.presentation.profile.viewmodel.LoadingViewModel
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    userId: String?,
    imageUrls: List<String>,
    viewModel: LoadingViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onLoadingComplete: (List<String>) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 이미지 분석 시작
    LaunchedEffect(userId, imageUrls) {
        if (userId != null && imageUrls.isNotEmpty()) {
            viewModel.startImageAnalysis(userId, imageUrls)
        }
    }
    
    // 분석 완료 시 다음 화면으로 이동
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete && uiState.errorMessage.isEmpty()) {
            delay(500)  // UI 효과
            onLoadingComplete(uiState.recommendedTags)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // 상단 뒤로가기
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 8.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color(0xFFFF9945)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 진행률 표시
            if (uiState.isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 32.dp),
                    color = Color(0xFFFF9945),
                    strokeWidth = 4.dp,
                    progress = uiState.progress
                )
            } else if (uiState.isComplete && uiState.errorMessage.isEmpty()) {
                // 완료 표시
                Text(
                    text = "✓",
                    fontSize = 80.sp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
            
            // 메인 텍스트
            Text(
                text = if (uiState.errorMessage.isNotEmpty()) "분석 실패" else "AI 이미지 분석",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (uiState.errorMessage.isNotEmpty()) Color(0xFFE53935) else Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // 상세 메시지
            Text(
                text = if (uiState.errorMessage.isNotEmpty()) uiState.errorMessage else uiState.statusMessage,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // 진행률 바
            if (uiState.isAnalyzing) {
                LinearProgressIndicator(
                    progress = uiState.progress,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(vertical = 16.dp),
                    color = Color(0xFFFF9945),
                    trackColor = Color(0xFFFFE0CC)
                )
            }
            
            // 분석 중인 이미지 수
            if (uiState.isAnalyzing && uiState.imageUrls.isNotEmpty()) {
                Text(
                    text = "${uiState.imageUrls.size}개의 이미지 분석 중...",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }
            
            // 로딩 닷
            if (uiState.isAnalyzing) {
                LoadingDots()
            }
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
