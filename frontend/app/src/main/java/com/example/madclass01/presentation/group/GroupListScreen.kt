package com.example.madclass01.presentation.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.presentation.group.component.GroupCard
import com.example.madclass01.presentation.group.viewmodel.GroupListViewModel
import java.lang.System.currentTimeMillis

@Composable
fun GroupListScreen(
    userId: String? = null,
    viewModel: GroupListViewModel = hiltViewModel(),
    refreshTrigger: Int = 0,
    onGroupClick: (String) -> Unit,
    onCreateGroupClick: () -> Unit = {},
    onQRScanClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.setUserId(userId)
        }
    }

    LaunchedEffect(refreshTrigger) {
        viewModel.loadMyGroups()
    }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    var backPressedTime by remember { androidx.compose.runtime.mutableLongStateOf(0L) }

    // Double back to exit logic
    androidx.activity.compose.BackHandler {
        val currentTime = currentTimeMillis()
        if (currentTime - backPressedTime <= 2000) {
            // Exit app
            (context as? android.app.Activity)?.finish()
        } else {
            backPressedTime = currentTime
            android.widget.Toast.makeText(context, "'뒤로' 버튼을 한번 더 누르면 종료됩니다.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    // Gradient Brush
    val headerBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFF9945),
            Color(0xFFFFB775)
        )
    )

    Scaffold(
        containerColor = Color(0xFFF9F9F9),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateGroupClick,
                containerColor = Color(0xFFFF9945),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "그룹 생성"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- Gradient Header ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(headerBrush, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .statusBarsPadding()
                        .padding(bottom = 24.dp, top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "내 그룹",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // QR Scan Button inside Header
                            IconButton(
                                onClick = onQRScanClick,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "QR 스캔",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = "함께하는 즐거움, 취향을 공유하세요!",
                            fontSize = 15.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                
                // --- Content List ---
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFFFF9945),
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                        
                        uiState.errorMessage.isNotEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 48.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = uiState.errorMessage,
                                    color = Color(0xFF666666),
                                    textAlign = TextAlign.Center,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        
                        uiState.myGroups.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Empty State Illustration Placeholder
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .background(Color(0xFFEEEEEE), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👋", fontSize = 50.sp)
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Text(
                                    text = "참여 중인 그룹이 없어요",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "새로운 그룹을 만들거나\nQR코드를 스캔해 참여해보세요!",
                                    fontSize = 15.sp,
                                    color = Color(0xFF999999),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                        
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(uiState.myGroups) { group ->
                                    // Use ElevatedCard for each group item
                                    ElevatedCard(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Using subtle elevation via shadow modifier usually, or just default
                                        modifier = Modifier.shadow(2.dp, RoundedCornerShape(16.dp))
                                    ) {
                                        GroupCard(
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
        }
    }
}
