package com.example.madclass01.presentation.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.presentation.common.component.TagChip
import com.example.madclass01.presentation.profile.viewmodel.TagSelectionViewModel

@Composable
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
    
    // 초기 커스텀 태그 설정 (Step 1에서 넘어온 값)
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
            kotlinx.coroutines.delay(2000)
            viewModel.clearErrorMessage()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 헤더
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                color = Color(0xFFFF9945),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { 
                                if (!uiState.errorMessage.contains("로딩")) {
                                    onBack()
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로가기",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Text(
                            text = "취향 분석 결과",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    Text(
                        text = "AI가 분석한 취향이에요\n수정하거나 추가할 수 있어요",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                }
            }
            
            // 본문
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // AI 추천 태그 안내
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF4E6)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "✨ AI가 사진을 분석하여 추천한 관심사입니다",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF9945)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "원하는 관심사를 선택하거나 선택 해제할 수 있어요",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
                
                // 자동 추출된 태그 (AI 추천)
                TagSection(
                    title = "AI 추천 관심사",
                    tags = uiState.recommendedTags,
                    onToggleTag = { viewModel.toggleRecommendedTag(it) }
                )
                
                // 선택된 태그 개수 표시
                if (uiState.recommendedTags.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "선택된 관심사: ${uiState.recommendedTags.count { it.isSelected }}개",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF9945)
                        )
                    }
                }
                
                // 에러 메시지
                if (uiState.errorMessage.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                // 완료 버튼
                Button(
                    onClick = { 
                        if (userId != null && !uiState.isLoading) {
                            viewModel.saveProfile(
                                userId = userId,
                                nickname = nickname,
                                age = age,
                                region = region,
                                bio = bio,
                                photoInterests = recommendedTags // Step 1에서 받은 추천 태그는 photo_interests로 저장
                            )
                        } else if (userId == null) {
                            // userId가 없는 경우 (테스트 등)
                             viewModel.completeSelection()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = 16.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9945)
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                Text(
                    text = "나중에 프로필에서 수정할 수 있어요",
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(top = 12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PhotoInterestSection(
    tags: List<com.example.madclass01.domain.model.Tag>,
    onRemoveTag: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F8F4),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tag.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                    
                    IconButton(
                        onClick = { onRemoveTag(tag.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "제거",
                            tint = Color(0xFF999999),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TagSection(
    title: String? = null,
    tags: List<com.example.madclass01.domain.model.Tag>,
    onToggleTag: (String) -> Unit
) {
    Column {
        if (title != null) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.chunked(3).forEach { tagRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tagRow.forEach { tag ->
                        TagChip(
                            label = tag.name,
                            isSelected = tag.isSelected,
                            onToggle = { onToggleTag(tag.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    repeat(maxOf(0, 3 - tagRow.size)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CustomTagSection(
    tags: List<com.example.madclass01.domain.model.Tag>,
    onRemoveTag: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.chunked(3).forEach { tagRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tagRow.forEach { tag ->
                    TagChip(
                        label = tag.name,
                        isSelected = true,
                        onRemove = { onRemoveTag(tag.id) },
                        modifier = Modifier.weight(1f)
                    )
                }

                repeat(maxOf(0, 3 - tagRow.size)) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
