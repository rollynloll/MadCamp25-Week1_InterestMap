package com.example.madclass01.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.presentation.search.component.SearchResultCard
import com.example.madclass01.presentation.search.viewmodel.SearchViewModel

import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SearchScreen(
    userId: String? = null,
    viewModel: SearchViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    onGroupClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val refreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = { viewModel.searchGroups() }
    )

    // 에러 메시지 표시
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = uiState.errorMessage,
                duration = SnackbarDuration.Short
            )
            viewModel.clearErrorMessage()
        }
    }
    
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
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
            .pullRefresh(refreshState)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- Gradient Header with Search Bar ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(headerBrush, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .statusBarsPadding()
                    .padding(bottom = 24.dp, top = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "그룹 찾기",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    
                    // Search Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Input Field
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "검색",
                                tint = Color(0xFFFF9945),
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            BasicTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color(0xFF333333)
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = { viewModel.performSearch() }
                                ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text(
                                            text = "관심사, 지역으로 검색",
                                            fontSize = 15.sp,
                                            color = Color(0xFFAAAAAA)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                        
                        // Filter Button
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
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
                }
            }
            
            // --- Search Results ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    uiState.isLoading -> {
                         // Loading
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFFF9945),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    uiState.searchResults.isEmpty() && uiState.searchQuery.isNotEmpty() && !uiState.isLoading -> {
                         // No Results
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color(0xFFEEEEEE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔍", fontSize = 40.sp)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                             Text(
                                text = "검색 결과가 없습니다",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "다른 키워드로 검색해보세요",
                                fontSize = 14.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    }
                    else -> {
                        // Results List
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.searchResults) { group ->
                                 ElevatedCard(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    SearchResultCard(
                                        group = group,
                                        onClick = { onGroupClick(group.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        PullRefreshIndicator(
            refreshing = uiState.isLoading,
            state = refreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = Color(0xFFFF9945)
        )
        
        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFF323232),
                contentColor = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
        }
        
        // Filter Bottom Sheet
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    filters: Map<String, Any>,
    onFilterUpdate: (String, Any) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    val primary = Color(0xFFFF9945)
    val regions = listOf("전체", "서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시", "대전광역시", "울산광역시", "세종특별자치시", "경기도", "강원특별자치도", "충청북도", "충청남도", "전북특별자치도", "전라남도", "경상북도", "경상남도", "제주특별자치도")
    val memberRanges = listOf("전체", "10명 이하", "10-30명", "30명 이상")
    
    var regionExpanded by remember { mutableStateOf(false) }

    val selectedRegion = filters["region"] as? String ?: "전체"
    val selectedMemberRange = filters["memberRange"] as? String ?: "전체"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Header
            Text(
                text = "상세 필터", 
                fontSize = 20.sp, 
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Region Section
            Text(
                text = "지역", 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Box {
                // Dropdown Trigger Field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                        .clickable { regionExpanded = true }
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedRegion,
                        fontSize = 15.sp,
                        color = if (selectedRegion == "전체") Color(0xFF999999) else Color(0xFF333333)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown, 
                        contentDescription = null,
                        tint = Color(0xFF999999)
                    )
                }
                
                // Dropdown Menu
                DropdownMenu(
                    expanded = regionExpanded,
                    onDismissRequest = { regionExpanded = false },
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .background(Color.White)
                        .width(300.dp) // Set a reasonable width
                ) {
                    regions.forEach { region ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = region,
                                    fontSize = 14.sp,
                                    fontWeight = if(selectedRegion == region) FontWeight.Bold else FontWeight.Normal,
                                    color = if(selectedRegion == region) primary else Color(0xFF333333)
                                ) 
                            },
                            onClick = {
                                onFilterUpdate("region", region)
                                regionExpanded = false
                            },
                            colors = MenuDefaults.itemColors()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Member Count Section
            Text(
                text = "인원 수", 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                memberRanges.forEach { range ->
                    val isSelected = selectedMemberRange == range
                     Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) primary else Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onFilterUpdate("memberRange", range) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = range,
                            color = if (isSelected) Color.White else Color(0xFF666666),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                 // Reset Button
                 Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
                        .clickable { onReset() },
                    contentAlignment = Alignment.Center
                 ) {
                     Text(
                         text = "초기화",
                         fontSize = 16.sp,
                         color = Color(0xFF666666), // Darker grey for readability
                         fontWeight = FontWeight.SemiBold
                     )
                 }
                
                // Apply Button
                Box(
                    modifier = Modifier
                         .weight(2f) // Give more weight to primary action
                        .height(56.dp)
                        .background(primary, RoundedCornerShape(16.dp))
                        .clickable { onApply() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "결과보기",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
             Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
