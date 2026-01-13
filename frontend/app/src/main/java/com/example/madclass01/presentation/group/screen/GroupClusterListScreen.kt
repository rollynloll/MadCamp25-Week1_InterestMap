package com.example.madclass01.presentation.group.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.madclass01.domain.model.Group
import com.example.madclass01.domain.model.UserEmbedding
import com.example.madclass01.presentation.group.viewmodel.ClusterGroup
import com.example.madclass01.presentation.group.viewmodel.GroupClusterViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GroupClusterListScreen(
    groupId: String,
    currentUserId: String,
    onBackPress: () -> Unit = {},
    onClusterGroupSaved: (String) -> Unit = {},
    viewModel: GroupClusterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedClusterForDetail by remember { mutableStateOf<ClusterGroup?>(null) }

    // Consistent Color Palette
    val clusterColors = listOf(
        Color(0xFFFF9F45), // Orange
        Color(0xFF2CB1BC), // Teal
        Color(0xFF4C6EF5), // Blue
        Color(0xFF9B59B6), // Purple
        Color(0xFF27AE60), // Green
        Color(0xFFE67E22)  // Dark Orange
    )

    LaunchedEffect(groupId, currentUserId) {
        viewModel.load(groupId, currentUserId)
    }

    val parentGroup = uiState.group
    if (selectedClusterForDetail != null) {
        val cluster = selectedClusterForDetail!!
        val color = clusterColors.getOrNull(cluster.id) ?: Color.Gray
        val customName = uiState.customClusterNames[cluster.id] ?: "Group ${cluster.id + 1}"

        ClusterDetailDialog(
            cluster = cluster,
            initialName = customName,
            color = color,
            currentUserId = currentUserId,
            isMine = cluster.members.any { it.userId == currentUserId },
            isSavingCluster = uiState.isSavingCluster,
            parentGroup = parentGroup,
            onDismiss = { selectedClusterForDetail = null },
            onSaveName = { newName ->
                viewModel.updateClusterName(cluster.id, newName)
                selectedClusterForDetail = null
            },
            onSaveClusterAsGroup = { newName, description, iconType ->
                viewModel.saveClusterAsGroup(cluster, newName, description, iconType, currentUserId) { result ->
                    if (result.isSuccess) {
                        onClusterGroupSaved(result.getOrThrow())
                        selectedClusterForDetail = null
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "소그룹 리스트",
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
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingView(MaterialTheme.colorScheme.primary)
                }
                uiState.errorMessage.isNotEmpty() -> {
                    ErrorView(uiState.errorMessage)
                }
                else -> {
                    Text(
                        text = "생성된 소그룹을 확인해보세요",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                    Text(
                        text = "클릭하여 그룹 이름을 수정하거나 상세 멤버를 확인할 수 있습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(uiState.clusters) { cluster ->
                            val isMine = cluster.members.any { it.userId == currentUserId }
                            val color = clusterColors.getOrNull(cluster.id) ?: Color.Gray
                            val customName = uiState.customClusterNames[cluster.id] ?: "Group ${cluster.id + 1}"

                            ClusterGridItem(
                                cluster = cluster,
                                customName = customName,
                                color = color,
                                isMine = isMine,
                                onClick = { selectedClusterForDetail = cluster }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClusterGridItem(
    cluster: ClusterGroup,
    customName: String,
    color: Color,
    isMine: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f), // Square-ish card
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // FacePile
            FacePile(
                members = cluster.members,
                maxVisible = 4,
                itemSize = 40.dp,
                overlap = 12.dp
            )

            Column {
                if (isMine) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        )
                    }
                }
                
                Text(
                    text = customName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                
                Text(
                    text = "${cluster.members.size} members",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun FacePile(
    members: List<UserEmbedding>,
    maxVisible: Int = 4,
    itemSize: androidx.compose.ui.unit.Dp = 40.dp,
    overlap: androidx.compose.ui.unit.Dp = 12.dp
) {
    val visibleMembers = members.take(maxVisible)
    val remainingCount = (members.size - maxVisible).coerceAtLeast(0)
    
    // Using a Box to render overlapping items manually for better control
    Box(
        modifier = Modifier.height(itemSize + if (visibleMembers.size > 2) itemSize / 2 else 0.dp)
            .width(itemSize * 2 + overlap * 2) // Approximation
    ) {
        // First Row
        val firstRow = visibleMembers.take(2)
        firstRow.forEachIndexed { index, member ->
            Box(
                modifier = Modifier
                    .offset(x = (itemSize - overlap) * index)
                    .size(itemSize)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            ) {
                AsyncImage(
                    model = member.profileImageUrl ?: fallbackAvatarUrl(member.userId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Second Row (if needed)
        val secondRow = if (visibleMembers.size > 2) visibleMembers.subList(2, visibleMembers.size) else emptyList()
        secondRow.forEachIndexed { index, member ->
            Box(
                modifier = Modifier
                    .offset(
                        x = (itemSize - overlap) * index + (itemSize / 2),
                        y = itemSize * 0.7f
                    )
                    .size(itemSize)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            ) {
                AsyncImage(
                    model = member.profileImageUrl ?: fallbackAvatarUrl(member.userId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        if (remainingCount > 0) {
             Box(
                modifier = Modifier
                    .offset(
                        x = (itemSize - overlap) * (secondRow.size) + (itemSize / 2),
                        y = itemSize * 0.7f
                    )
                    .size(itemSize)
                    .clip(CircleShape)
                    .background(Color(0xFFEEEEEE))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$remainingCount",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClusterDetailDialog(
    cluster: ClusterGroup,
    initialName: String,
    color: Color,
    currentUserId: String,
    parentGroup: Group?,
    isMine: Boolean,
    isSavingCluster: Boolean,
    onDismiss: () -> Unit,
    onSaveName: (String) -> Unit,
    onSaveClusterAsGroup: (String, String?, String?) -> Unit
) {
    var editingName by remember { mutableStateOf(initialName) }
    var editingDescription by remember {
        mutableStateOf(parentGroup?.description.takeIf { !it.isNullOrBlank() } ?: "")
    }
    var selectedIcon by remember { mutableStateOf(parentGroup?.iconType ?: "users") }
    val region = parentGroup?.region ?: "전체"
    val iconOptions = listOf("users", "coffee", "camera", "mountain", "music", "book", "sports", "food")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "그룹 상세",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name Edit
                Text(
                    text = "그룹 이름",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = color,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    trailingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "설명",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = editingDescription,
                    onValueChange = { editingDescription = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = color,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    placeholder = { Text("그룹 설명을 입력해보세요") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "지역",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = region,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.LightGray,
                        disabledTextColor = Color(0xFF333333)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "아이콘",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    iconOptions.forEach { icon ->
                        AssistChip(
                            onClick = { selectedIcon = icon },
                            label = { Text(icon) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selectedIcon == icon) color else Color(0xFFF0F0F0),
                                labelColor = if (selectedIcon == icon) Color.White else Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Members List
                Text(
                    text = "멤버 (${cluster.members.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        cluster.members.forEach { member ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = member.profileImageUrl ?: fallbackAvatarUrl(member.userId),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = member.userName.ifBlank { "User" },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    if (member.userId == currentUserId) {
                                        Text(
                                            text = "Me",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = color
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Button
                Button(
                    onClick = {
                        if (isMine && !isSavingCluster) {
                            onSaveClusterAsGroup(editingName, editingDescription, selectedIcon)
                        } else if (!isMine) {
                            onSaveName(editingName)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMine) Color(0xFF47A3FF) else color
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                        if (isMine && isSavingCluster) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            if (isMine) "내 그룹 만들기" else "저장하기",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

private fun fallbackAvatarUrl(seed: String): String {
    val stableSeed = abs(seed.hashCode())
    return "https://picsum.photos/seed/$stableSeed/200/200"
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
