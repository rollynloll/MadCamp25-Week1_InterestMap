package com.example.madclass01.presentation.group.screen

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.R
import com.example.madclass01.presentation.group.component.IconPreview
import com.example.madclass01.presentation.group.component.IconSelectButton
import com.example.madclass01.presentation.group.component.PrivacyOption
import com.example.madclass01.presentation.group.component.TagChip
import com.example.madclass01.presentation.group.viewmodel.CreateGroupViewModel

@Composable
fun CreateGroupScreen(
    userId: String,
    viewModel: CreateGroupViewModel = hiltViewModel(),
    onCreateSuccess: (String) -> Unit = { },
    onBackPress: () -> Unit = { }
) {
    val uiState by viewModel.uiState.collectAsState()
    var tagInputValue by remember { mutableStateOf("") }
    
    LaunchedEffect(uiState.isCreateSuccess) {
        if (uiState.isCreateSuccess && uiState.createdGroupId != null) {
            onCreateSuccess(uiState.createdGroupId!!)
            viewModel.resetCreateState()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close Button
                IconButton(onClick = onBackPress) {
                    Icon(
                        painter = painterResource(id = R.drawable.omo),
                        contentDescription = "Close",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF111827)
                    )
                }
                
                // Title
                Text(
                    text = "새 그룹 만들기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
                
                // Create Button
                Button(
                    onClick = {
                        viewModel.createGroup(userId)
                    },
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF667EEA),
                        disabledContainerColor = Color(0xFFB4B4F1)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(58.dp)
                        .height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "완료",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Divider(
                color = Color(0xFFE5E7EB),
                thickness = 1.dp
            )
            
            // Form Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Icon Selection
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconPreview(iconType = uiState.selectedIconType)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconSelectButton(
                            iconType = "users",
                            iconResId = R.drawable.omo,
                            isSelected = uiState.selectedIconType == "users",
                            onClick = { viewModel.selectIconType("users") },
                            modifier = Modifier.weight(1f)
                        )
                        IconSelectButton(
                            iconType = "coffee",
                            iconResId = R.drawable.omo,
                            isSelected = uiState.selectedIconType == "coffee",
                            onClick = { viewModel.selectIconType("coffee") },
                            modifier = Modifier.weight(1f)
                        )
                        IconSelectButton(
                            iconType = "camera",
                            iconResId = R.drawable.omo,
                            isSelected = uiState.selectedIconType == "camera",
                            onClick = { viewModel.selectIconType("camera") },
                            modifier = Modifier.weight(1f)
                        )
                        IconSelectButton(
                            iconType = "mountain",
                            iconResId = R.drawable.omo,
                            isSelected = uiState.selectedIconType == "mountain",
                            onClick = { viewModel.selectIconType("mountain") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Group Name
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "그룹 이름",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                    TextField(
                        value = uiState.groupName,
                        onValueChange = { viewModel.updateGroupName(it) },
                        placeholder = {
                            Text(
                                text = "예: 서울 러너스",
                                fontSize = 15.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedIndicatorColor = Color(0xFFE5E7EB),
                            focusedIndicatorColor = Color(0xFF667EEA)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
                
                // Group Description
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "그룹 설명",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                    TextField(
                        value = uiState.groupDescription,
                        onValueChange = { viewModel.updateGroupDescription(it) },
                        placeholder = {
                            Text(
                                text = "그룹에 대해 간단히 설명해주세요...",
                                fontSize = 15.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedIndicatorColor = Color(0xFFE5E7EB),
                            focusedIndicatorColor = Color(0xFF667EEA)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Group Tags
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "관련 태그",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                    
                    // Add Tag Input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                color = Color(0xFFF9FAFB),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF667EEA)
                        )
                        
                        TextField(
                            value = tagInputValue,
                            onValueChange = { tagInputValue = it },
                            placeholder = {
                                Text(
                                    text = "태그 추가",
                                    fontSize = 14.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                        
                        Button(
                            onClick = {
                                if (tagInputValue.isNotBlank()) {
                                    viewModel.addTag(tagInputValue)
                                    tagInputValue = ""
                                }
                            },
                            modifier = Modifier.size(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF667EEA)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "+",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Added Tags
                    if (uiState.selectedTags.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.selectedTags.forEach { tag ->
                                    TagChip(
                                        tag = tag,
                                        onRemove = { viewModel.removeTag(it) }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Privacy Setting
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "공개 설정",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                    
                    // Public Option
                    PrivacyOption(
                        title = "공개",
                        description = "누구나 검색하고 가입할 수 있어요",
                        isSelected = uiState.isPublic,
                        icon = {
                            Text(
                                text = "🌐",
                                fontSize = 16.sp
                            )
                        },
                        onClick = { viewModel.setPublic(true) }
                    )
                    
                    // Private Option
                    PrivacyOption(
                        title = "비공개",
                        description = "초대받은 사람만 가입할 수 있어요",
                        isSelected = !uiState.isPublic,
                        icon = {
                            Text(
                                text = "🔒",
                                fontSize = 16.sp
                            )
                        },
                        onClick = { viewModel.setPublic(false) }
                    )
                }
                
                // Error Message
                if (uiState.errorMessage.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            fontSize = 14.sp,
                            color = Color(0xFFDC2626),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// FlowRow Composable for better tag layout
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        val maxWidth = constraints.maxWidth
        var x = 0
        var y = 0
        var rowHeight = 0

        placeables.forEach { placeable ->
            if (x + placeable.width > maxWidth && x > 0) {
                x = 0
                y += rowHeight + 8
                rowHeight = 0
            }
            x += placeable.width + 8
            rowHeight = maxOf(rowHeight, placeable.height)
        }

        val totalHeight = y + rowHeight

        layout(maxWidth, totalHeight) {
            var x = 0
            var y = 0
            var maxHeight = 0
            
            placeables.forEach { placeable ->
                if (x + placeable.width > constraints.maxWidth && x > 0) {
                    x = 0
                    y += maxHeight + 8
                    maxHeight = 0
                }
                
                placeable.place(x, y)
                x += placeable.width + 8
                maxHeight = maxOf(maxHeight, placeable.height)
            }
        }
    }
}
