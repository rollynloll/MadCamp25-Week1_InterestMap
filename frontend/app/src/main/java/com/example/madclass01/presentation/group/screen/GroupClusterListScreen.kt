package com.example.madclass01.presentation.group.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.presentation.group.viewmodel.GroupClusterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupClusterListScreen(
    groupId: String,
    currentUserId: String,
    onBackPress: () -> Unit = {},
    viewModel: GroupClusterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(groupId, currentUserId) {
        viewModel.load(groupId, currentUserId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "소그룹 보기",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(horizontal = 16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
                uiState.errorMessage.isNotEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    val clusterColors = listOf(
                        Color(0xFFFF9F45),
                        Color(0xFF2CB1BC),
                        Color(0xFF4C6EF5),
                        Color(0xFF9B59B6),
                        Color(0xFF27AE60),
                        Color(0xFFE67E22)
                    )
                    val myClusterId = uiState.clusters.firstOrNull { cluster ->
                        cluster.members.any { it.userId == currentUserId }
                    }?.id
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
                    ) {
                        items(uiState.clusters) { cluster ->
                            val isMine = cluster.id == myClusterId
                            val clusterColor = clusterColors.getOrNull(cluster.id) ?: Color(0xFF9CA3AF)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = clusterColor.copy(alpha = 0.18f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = if (isMine) BorderStroke(2.dp, clusterColor) else null
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    val title = if (isMine) {
                                        "그룹 ${cluster.id + 1} · 내 그룹"
                                    } else {
                                        "그룹 ${cluster.id + 1} · ${cluster.members.size}명"
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(clusterColor, RoundedCornerShape(999.dp))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = clusterColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (cluster.members.isEmpty()) {
                                        Text(
                                            text = "배정된 멤버가 없어요.",
                                            fontSize = 13.sp,
                                            color = Color(0xFF777777)
                                        )
                                    } else {
                                        cluster.members.forEach { member ->
                                            Text(
                                                text = "• ${member.userName.ifBlank { member.userId.take(6) }}",
                                                fontSize = 14.sp,
                                                color = Color(0xFF333333),
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}
