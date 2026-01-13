package com.example.madclass01.presentation.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.presentation.common.component.TagChip
import com.example.madclass01.presentation.profile.viewmodel.TagSelectionViewModel
import kotlinx.coroutines.delay

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun TagSelectionScreen(
    userId: String?,
    nickname: String,
    age: Int?,
    region: String?,
    bio: String? = null,
    recommendedTags: List<String>,
    initialCustomTags: List<String> = emptyList(),
    viewModel: TagSelectionViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onComplete: (selectedTags: List<String>) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 초기 커스텀 태그 설정
    LaunchedEffect(initialCustomTags) {
        if (initialCustomTags.isNotEmpty()) {
            viewModel.setCustomTags(initialCustomTags)
        }
    }
    
    // 추천 태그 설정
    LaunchedEffect(recommendedTags) {
        viewModel.setRecommendedTags(recommendedTags)
    }
    
    LaunchedEffect(uiState.isSelectionComplete) {
        if (uiState.isSelectionComplete) {
            onComplete(viewModel.getSelectedTags().map { it.name })
            viewModel.resetCompleteState()
        }
    }
    
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            delay(2000)
            viewModel.clearErrorMessage()
        }
    }
    
    // Gradient Brush
    val headerBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFF9945),
            Color(0xFFFFB775)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- Header ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(headerBrush, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(bottom = 24.dp, top = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { 
                                if (!uiState.errorMessage.contains("로딩")) {
                                    onBack()
                                }
                            },
                             modifier = Modifier.offset(x = (-12).dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로가기",
                                tint = Color.White
                            )
                        }
                    }
                    
                    Text(
                        text = "취향 분석 결과",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AI가 회원님을 위해 찾아낸 관심사예요\n자유롭게 선택하고 수정해보세요!",
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )
                }
            }
            
            // --- Main Content ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // AI Suggestion Banner
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFFFF8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✨ AI 추천 관심사",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9945)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "사진에서 이런 활동들을 발견했어요",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
                
                // Active Tags Section
                if (uiState.recommendedTags.isNotEmpty()) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                             Text(
                                text = "추천 태그 ${uiState.recommendedTags.count { it.isSelected }}개 선택됨",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.recommendedTags.forEach { tag ->
                                    TagChip(
                                        label = tag.name,
                                        isSelected = tag.isSelected,
                                        onToggle = { viewModel.toggleRecommendedTag(tag.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Error Display
                if (uiState.errorMessage.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = Color(0xFFD32F2F),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Complete Button
                Button(
                    onClick = { 
                        if (userId != null && !uiState.isLoading) {
                            viewModel.saveProfile(
                                userId = userId,
                                nickname = nickname,
                                age = age,
                                region = region,
                                bio = bio,
                                photoInterests = recommendedTags 
                            )
                        } else if (userId == null) {
                             viewModel.completeSelection()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9945),
                        disabledContainerColor = Color(0xFFFFCCAA)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "완료하고 시작하기",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "나중에 프로필 수정에서 언제든지 변경할 수 있어요",
                        fontSize = 13.sp,
                        color = Color(0xFF999999)
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
