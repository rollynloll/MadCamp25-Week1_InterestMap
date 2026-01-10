package com.example.madclass01.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
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
                onReset = { viewModel.resetFilters() },
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
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    val primary = Color(0xFFFF9945)
    val border = Color(0xFFE8E8E8)
    val textPrimary = Color(0xFF1A1A1A)
    val textSecondary = Color(0xFF8C8C8C)
    val inactiveText = Color(0xFF595959)

    val categories = listOf("운동", "카페", "예술", "음악", "사진", "등산")
    val regions = listOf("전체", "서울특별시", "경기도", "인천광역시", "부산광역시")
    val memberRanges = listOf("전체", "10명 이하", "10-30명", "30명 이상")
    val activities = listOf("전체", "활발함", "보통", "조용함")

    val selectedCategory = filters["category"] as? String ?: "모든종류"
    val selectedRegion = filters["region"] as? String ?: "전체"
    val selectedMemberRange = filters["memberRange"] as? String ?: "전체"
    val selectedActivity = filters["activity"] as? String ?: "전체"
    val matchPercent = (filters["matchPercentage"] as? Int ?: 70).coerceIn(0, 100)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 0.dp, max = 745.dp)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.size(24.dp))

                    Text(
                        text = "필터",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = inactiveText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "나에게 맞는 그룹을 찾아보세요",
                    fontSize = 14.sp,
                    color = textSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Category
                @OptIn(ExperimentalLayoutApi::class)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "카테고리",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { label ->
                            val isSelected = selectedCategory == label
                            FilterPill(
                                label = label,
                                isSelected = isSelected,
                                onClick = { onFilterUpdate("category", label) },
                                selectedColor = primary,
                                borderColor = border,
                                selectedTextColor = Color.White,
                                unselectedTextColor = inactiveText
                            )
                        }
                    }
                }

                // Location
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "지역",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(Color(0xFFFFFAF5), RoundedCornerShape(12.dp))
                            .border(1.dp, border, RoundedCornerShape(12.dp))
                            .clickable {
                                val currentIndex = regions.indexOf(selectedRegion).let { if (it < 0) 0 else it }
                                val next = regions[(currentIndex + 1) % regions.size]
                                onFilterUpdate("region", next)
                            }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "지역",
                                tint = primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedRegion,
                                fontSize = 15.sp,
                                color = textPrimary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "선택",
                            tint = inactiveText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Member Count
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "인원",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        memberRanges.forEach { label ->
                            val isSelected = selectedMemberRange == label
                            FilterPill(
                                label = label,
                                isSelected = isSelected,
                                onClick = { onFilterUpdate("memberRange", label) },
                                selectedColor = primary,
                                borderColor = border,
                                selectedTextColor = Color.White,
                                unselectedTextColor = inactiveText,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Activity
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "활동 정도",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activities.forEach { label ->
                            val isSelected = selectedActivity == label
                            FilterPill(
                                label = label,
                                isSelected = isSelected,
                                onClick = { onFilterUpdate("activity", label) },
                                selectedColor = primary,
                                borderColor = border,
                                selectedTextColor = Color.White,
                                unselectedTextColor = inactiveText,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Match Slider
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "매칭도",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Slider(
                            value = matchPercent.toFloat(),
                            onValueChange = { onFilterUpdate("matchPercentage", it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = primary,
                                activeTrackColor = primary,
                                inactiveTrackColor = Color(0xFFFFFAF5)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "0%", fontSize = 12.sp, color = textSecondary)
                            Text(text = "70% 이상", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = primary)
                            Text(text = "100%", fontSize = 12.sp, color = textSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp, top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = inactiveText
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(border)
                    )
                ) {
                    Text(text = "초기화", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onApply,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text(text = "적용하기", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    borderColor: Color,
    selectedTextColor: Color,
    unselectedTextColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .background(
                color = if (isSelected) selectedColor else Color.White,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) selectedTextColor else unselectedTextColor
        )
    }
}
