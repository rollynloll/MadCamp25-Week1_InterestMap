package com.example.madclass01.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.presentation.search.component.SearchResultCard
import com.example.madclass01.presentation.search.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onGroupClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 헤더
            Text(
                text = "그룹 찾기",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
            
            // 검색 바
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 검색 입력창
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(
                            color = Color(0xFFF8F8F8),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "검색",
                        tint = Color(0xFF999999),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    BasicTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        ),
                        decorationBox = { innerTextField ->
                            if (uiState.searchQuery.isEmpty()) {
                                Text(
                                    text = "관심사, 지역으로 검색해보세요",
                                    fontSize = 14.sp,
                                    color = Color(0xFF999999)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
                
                // 필터 버튼
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFFFF9945),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { showFilterSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "필터",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 검색 결과
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.searchResults) { group ->
                    SearchResultCard(
                        group = group,
                        onClick = { onGroupClick(group.id) }
                    )
                    Divider(
                        color = Color(0xFFF5F5F5),
                        thickness = 1.dp
                    )
                }
            }
        }
        
        // 필터 바텀 시트
        if (showFilterSheet) {
            FilterBottomSheet(
                filters = uiState.filters,
                onFilterUpdate = { key, value -> viewModel.updateFilter(key, value) },
                onDismiss = { showFilterSheet = false },
                onApply = {
                    viewModel.searchGroups()
                    showFilterSheet = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    filters: Map<String, Any>,
    onFilterUpdate: (String, Any) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "필터",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            // 지역 필터
            FilterSection(
                title = "지역",
                selectedValue = filters["region"] as? String ?: "전체",
                options = listOf("전체", "서울", "경기", "인천", "부산", "대구", "광주", "대전"),
                onSelect = { onFilterUpdate("region", it) }
            )
            
            // 활동도 필터
            FilterSection(
                title = "활동도",
                selectedValue = filters["activity"] as? String ?: "전체",
                options = listOf("전체", "매우활발", "활발", "보통", "느긋"),
                onSelect = { onFilterUpdate("activity", it) }
            )
            
            // 적용 버튼
            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9945)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "적용하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    selectedValue: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedValue
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) Color(0xFFFFF3E0) else Color(0xFFF8F8F8),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFFFF9945) else Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelect(option) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = option,
                        fontSize = 12.sp,
                        color = if (isSelected) Color(0xFFFF9945) else Color(0xFF666666)
                    )
                }
            }
        }
    }
}
