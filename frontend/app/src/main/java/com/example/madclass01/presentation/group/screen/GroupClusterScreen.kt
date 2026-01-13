package com.example.madclass01.presentation.group.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.presentation.group.component.RelationshipGraphComponent
import com.example.madclass01.presentation.group.viewmodel.GroupClusterViewModel
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupClusterScreen(
    groupId: String,
    currentUserId: String,
    onBackPress: () -> Unit = {},
    onViewGroups: () -> Unit = {},
    viewModel: GroupClusterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedClusterId by remember { mutableStateOf<Int?>(null) }
    val clusterCount = uiState.clusterCount

    LaunchedEffect(groupId, currentUserId) {
        viewModel.load(groupId, currentUserId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = uiState.group?.name ?: "소그룹 나누기",
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
                    Text(
                        text = "취향지도 기반 3개 소그룹",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                    )

                    if (uiState.relationshipGraph != null) {
                        val clusterColors = listOf(
                            Color(0xFFFF9F45),
                            Color(0xFF2CB1BC),
                            Color(0xFF4C6EF5),
                            Color(0xFF9B59B6),
                            Color(0xFF27AE60),
                            Color(0xFFE67E22)
                        )
                        val memberColorMap = uiState.clusters.flatMap { cluster ->
                            val color = clusterColors.getOrNull(cluster.id) ?: Color(0xFF9CA3AF)
                            cluster.members.map { it.userId to color }
                        }.toMap()
                        val memberClusterMap = uiState.clusters.flatMap { cluster ->
                            cluster.members.map { it.userId to cluster.id }
                        }.toMap()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "소그룹 수: $clusterCount",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFFB85A16)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.updateClusterCount((clusterCount - 1).coerceAtLeast(2))
                                        selectedClusterId = null
                                    },
                                    enabled = clusterCount > 2,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE4D0)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = "-",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFB85A16)
                                    )
                                }
                                Button(
                                    onClick = {
                                        viewModel.updateClusterCount((clusterCount + 1).coerceAtMost(6))
                                        selectedClusterId = null
                                    },
                                    enabled = clusterCount < 6,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE4D0)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = "+",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFB85A16)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { selectedClusterId = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedClusterId == null) Color(0xFFB85A16) else Color(0xFFFFE4D0),
                                    contentColor = if (selectedClusterId == null) Color.White else Color(0xFFB85A16)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = "전체 보기",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                            uiState.clusters.forEach { cluster ->
                                val isSelected = selectedClusterId == cluster.id
                                val color = clusterColors.getOrNull(cluster.id) ?: Color(0xFF9CA3AF)
                                Button(
                                    onClick = {
                                        selectedClusterId = if (isSelected) null else cluster.id
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) color else Color(0xFFFFE4D0),
                                        contentColor = if (isSelected) Color.White else Color(0xFFB85A16)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = "그룹 ${cluster.id + 1}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(520.dp)
                                .background(Color(0xFFFAFBFC))
                        ) {
                            ZoomableGraphContainer(modifier = Modifier.fillMaxSize()) { scale ->
                                val nodeScale = (1f / scale.pow(1.3f)).coerceIn(0.2f, 2f)
                                RelationshipGraphComponent(
                                    relationshipGraph = uiState.relationshipGraph!!,
                                    nodeScale = nodeScale,
                                    nodeColorProvider = { userId ->
                                        val clusterId = memberClusterMap[userId]
                                        if (selectedClusterId == null) {
                                            memberColorMap[userId]
                                        } else if (clusterId == selectedClusterId) {
                                            memberColorMap[userId]
                                        } else {
                                            Color(0xFFE5E7EB)
                                        }
                                    },
                                    nodeZIndexProvider = { userId ->
                                        val clusterId = memberClusterMap[userId]
                                        if (selectedClusterId != null && clusterId == selectedClusterId) 1f else 0f
                                    },
                                    onNodeClick = {},
                                    onNodeLongClick = {}
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "그래프 데이터를 불러오지 못했어요.",
                            color = Color(0xFF777777),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    Card(
                        onClick = onViewGroups,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4D0)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "소그룹 보기",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color(0xFFB85A16)
                            )
                            Text(
                                text = "멤버 리스트 확인",
                                fontSize = 12.sp,
                                color = Color(0xFFB85A16)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomableGraphContainer(
    modifier: Modifier = Modifier,
    content: @Composable (Float) -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.1f, 8f)
        offset += panChange
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .transformable(transformState),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                }
        ) {
            content(scale)
        }
    }
}
