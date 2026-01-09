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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.presentation.common.component.TagChip
import com.example.madclass01.presentation.common.component.TagInputField
import com.example.madclass01.presentation.profile.viewmodel.TagSelectionViewModel

@Composable
fun TagSelectionScreen(
    viewModel: TagSelectionViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onComplete: (selectedTags: Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.analyzeImages()
    }
    
    LaunchedEffect(uiState.isSelectionComplete) {
        if (uiState.isSelectionComplete) {
            onComplete(viewModel.getSelectedTags().size)
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
                            onClick = onBack,
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
                // 자동 추출된 태그
                TagSection(
                    title = "자동 추출된 태그",
                    tags = uiState.extractedTags,
                    onToggleTag = { viewModel.toggleExtractedTag(it) }
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // 직접 추가하기
                Column {
                    Text(
                        text = "직접 추가하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    TagInputField(
                        onAddTag = { viewModel.addCustomTag(it) },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // 추천 태그
                if (uiState.recommendedTags.isNotEmpty()) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "추천 태그",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            
                            Text(
                                text = "사진 기반 추천",
                                fontSize = 12.sp,
                                color = Color(0xFF999999)
                            )
                        }
                        
                        TagSection(
                            tags = uiState.recommendedTags,
                            onToggleTag = { viewModel.toggleRecommendedTag(it) }
                        )
                    }
                }
                
                // 커스텀 태그
                if (uiState.customTags.isNotEmpty()) {
                    Column {
                        Text(
                            text = "내가 추가한 태그",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        CustomTagSection(
                            tags = uiState.customTags,
                            onRemoveTag = { viewModel.removeCustomTag(it) }
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
                    onClick = { viewModel.completeSelection() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9945)
                    )
                ) {
                    Text(
                        text = "완료하고 시작하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
