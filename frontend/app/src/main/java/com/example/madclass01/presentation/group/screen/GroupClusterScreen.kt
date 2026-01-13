package com.example.madclass01.presentation.group.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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
    onEnterSubgroup: (String, String, Int) -> Unit = { _, _, _ -> },
    viewModel: GroupClusterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedClusterId by remember { mutableStateOf<Int?>(null) }
    val clusterCount = uiState.clusterCount

    // Consistent Color Palette
    val clusterColors = listOf(
        Color(0xFFFF9F45), // Orange
        Color(0xFF2CB1BC), // Teal
        Color(0xFF4C6EF5), // Blue
        Color(0xFF9B59B6), // Purple
        Color(0xFF27AE60), // Green
        Color(0xFFE67E22)  // Dark Orange
    )

    // Primary Theme Colors
    val primaryColor = Color(0xFFB85A16)
    val primaryContainer = Color(0xFFFFE4D0)

    LaunchedEffect(groupId, currentUserId) {
        viewModel.load(groupId, currentUserId)
    }

    val defaultClusterId = uiState.clusters.firstOrNull()?.id
    val activeClusterId = selectedClusterId ?: defaultClusterId
    val showSubgroupButton = uiState.relationshipGraph != null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = uiState.group?.name ?: "소그룹 나누기",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White,
        floatingActionButton = {
            if (!uiState.isLoading && uiState.errorMessage.isEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onViewGroups,
                    containerColor = primaryColor,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                    text = { Text(text = "멤버 확인하기", fontWeight = FontWeight.Bold) }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingView(primaryColor)
                }
                uiState.errorMessage.isNotEmpty() -> {
                    ErrorView(uiState.errorMessage)
                }
                else -> {
                    // Header Section
                    Text(
                        text = "취향지도 기반 ${clusterCount}개 소그룹",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                    Text(
                        text = "취향이 비슷한 멤버끼리 뭉쳐보세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    if (uiState.relationshipGraph != null) {
                        val memberColorMap = remember(uiState.clusters) {
                            uiState.clusters.flatMap { cluster ->
                                val color = clusterColors.getOrNull(cluster.id) ?: Color.Gray
                                cluster.members.map { it.userId to color }
                            }.toMap()
                        }
                        val memberClusterMap = remember(uiState.clusters) {
                            uiState.clusters.flatMap { cluster ->
                                cluster.members.map { it.userId to cluster.id }
                            }.toMap()
                        }

                        // 1. Control Panel (Cluster Count)
                        ClusterControlRow(
                            clusterCount = clusterCount,
                            onDecrease = {
                                viewModel.updateClusterCount((clusterCount - 1).coerceAtLeast(2))
                                selectedClusterId = null
                            },
                            onIncrease = {
                                viewModel.updateClusterCount((clusterCount + 1).coerceAtMost(6))
                                selectedClusterId = null
                            },
                            primaryColor = primaryColor,
                            containerColor = primaryContainer
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isAllSelected = selectedClusterId == null
                            FilterChip(
                                selected = isAllSelected,
                                onClick = { selectedClusterId = null },
                                label = { Text("전체 보기") },
                                enabled = true,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryContainer,
                                    selectedLabelColor = primaryColor,
                                    containerColor = Color.Transparent,
                                    labelColor = Color.Gray
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isAllSelected,
                                    borderColor = if (isAllSelected) primaryColor else Color.LightGray,
                                    selectedBorderColor = primaryColor,
                                    borderWidth = 1.dp
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )

                            uiState.clusters.forEach { cluster ->
                                val isSelected = selectedClusterId == cluster.id
                                val color = clusterColors.getOrNull(cluster.id) ?: Color.Gray
                                
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedClusterId = if (isSelected) null else cluster.id
                                    },
                                    label = { Text("그룹 ${cluster.id + 1}") },
                                    enabled = true,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = color.copy(alpha = 0.2f),
                                        selectedLabelColor = color, // Use cluster color specifically
                                        containerColor = Color.Transparent,
                                        labelColor = Color.Gray
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) color else Color.LightGray,
                                        selectedBorderColor = color,
                                        borderWidth = 1.dp
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Graph Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f) // Occupy remaining space
                                .padding(bottom = 80.dp) // Space for FAB
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFF8F9FA)) // Softer background
                                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(24.dp))
                        ) {
                            ZoomableGraphContainer(modifier = Modifier.fillMaxSize()) { scale ->
                                val nodeScale = (1f / scale.pow(1.3f)).coerceIn(0.2f, 2f)
                                RelationshipGraphComponent(
                                    relationshipGraph = uiState.relationshipGraph!!,
                                    nodeScale = nodeScale,
                                    nodeColorProvider = { userId ->
                                        val clusterId = memberClusterMap[userId]
                                        if (selectedClusterId == null) {
                                            memberColorMap[userId] ?: Color.Gray
                                        } else if (clusterId == selectedClusterId) {
                                            memberColorMap[userId] ?: Color.Gray
                                        } else {
                                            Color(0xFFE0E0E0) // De-emphasized color
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
                            
                            // Interaction Hint
                            Text(
                                text = "💡 핀치하여 확대/축소",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                            )
                        }

                    } else {
                        Spacer(modifier = Modifier.height(32.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "(데이터 없음)",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ClusterControlRow(
    clusterCount: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    primaryColor: Color,
    containerColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "소그룹 개수 설정",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                enabled = clusterCount > 2,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = containerColor,
                    contentColor = primaryColor,
                    disabledContainerColor = Color(0xFFF5F5F5),
                    disabledContentColor = Color.LightGray
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "$clusterCount",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.width(20.dp).align(Alignment.CenterVertically),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            IconButton(
                onClick = onIncrease,
                enabled = clusterCount < 6,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = containerColor,
                    contentColor = primaryColor,
                    disabledContainerColor = Color(0xFFF5F5F5),
                    disabledContentColor = Color.LightGray
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingView(color: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = 3.dp
        )
    }
}

@Composable
private fun ErrorView(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
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
