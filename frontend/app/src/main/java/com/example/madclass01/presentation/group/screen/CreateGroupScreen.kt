@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.madclass01.presentation.group.screen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
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

    // 프로필 사진 선택 런처
    val profileImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateProfileImageUri(it.toString())
        }
    }

    LaunchedEffect(uiState.isCreateSuccess) {
        if (uiState.isCreateSuccess && uiState.createdGroupId != null) {
            onCreateSuccess(uiState.createdGroupId!!)
            viewModel.resetCreateState()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close Button
                IconButton(
                    onClick = { 
                        if (!uiState.isLoading) {
                            onBackPress()
                        }
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(28.dp),
                        tint = if (uiState.isLoading) Color(0xFFCCCCCC) else Color(0xFF111827)
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
                        containerColor = Color(0xFFFF9945),
                        disabledContainerColor = Color(0xFFFFCBA4)
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 그룹 프로필 선택 (아이콘 또는 사진)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                        Text(
                            text = "그룹 프로필",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827)
                        )

                        // 타입 선택 버튼
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.updateProfileImageUri(null) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!uiState.useCustomImage) Color(0xFFFF9945) else Color(
                                        0xFFF3F4F6
                                    ),
                                    contentColor = if (!uiState.useCustomImage) Color.White else Color(
                                        0xFF6B7280
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("아이콘 선택", fontSize = 14.sp)
                            }

                            Button(
                                onClick = { profileImageLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.useCustomImage) Color(0xFFFF9945) else Color(
                                        0xFFF3F4F6
                                    ),
                                    contentColor = if (uiState.useCustomImage) Color.White else Color(
                                        0xFF6B7280
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("사진 업로드", fontSize = 14.sp)
                            }
                        }

                        // 미리보기 영역
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.CenterHorizontally)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5))
                                .border(2.dp, Color(0xFFE0E0E0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.useCustomImage && uiState.profileImageUri != null) {
                                AsyncImage(
                                    model = uiState.profileImageUri,
                                    contentDescription = "그룹 프로필 사진",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                IconPreview(iconType = uiState.selectedIconType)
                            }
                        }

                        // 아이콘 그리드 (아이콘 모드일 때만 표시)
                        if (!uiState.useCustomImage) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconSelectButton(
                                        iconType = "music",
                                        iconResId = R.drawable.omo,
                                        isSelected = uiState.selectedIconType == "music",
                                        onClick = { viewModel.selectIconType("music") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconSelectButton(
                                        iconType = "book",
                                        iconResId = R.drawable.omo,
                                        isSelected = uiState.selectedIconType == "book",
                                        onClick = { viewModel.selectIconType("book") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconSelectButton(
                                        iconType = "sports",
                                        iconResId = R.drawable.omo,
                                        isSelected = uiState.selectedIconType == "sports",
                                        onClick = { viewModel.selectIconType("sports") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconSelectButton(
                                        iconType = "food",
                                        iconResId = R.drawable.omo,
                                        isSelected = uiState.selectedIconType == "food",
                                        onClick = { viewModel.selectIconType("food") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // 안내문구
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFFF4E6),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "한 번 등록한 그룹 프로필은 이후 수정이 불가능합니다.",
                                    fontSize = 13.sp,
                                    color = Color(0xFFFF9945),
                                    lineHeight = 18.sp
                                )
                            }
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
                                focusedIndicatorColor = Color(0xFFFF9945)
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
                                focusedIndicatorColor = Color(0xFFFF9945)
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
                                .height(52.dp)
                                .background(
                                    color = Color(0xFFF9FAFB),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFE5E7EB),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9945)
                            )

                            TextField(
                                value = tagInputValue,
                                onValueChange = { tagInputValue = it },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 14.sp,
                                    color = Color(0xFF111827)
                                ),
                                placeholder = {
                                    Text(
                                        text = "태그 추가",
                                        fontSize = 14.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedTextColor = Color(0xFF111827),
                                    focusedTextColor = Color(0xFF111827),
                                    cursorColor = Color(0xFFFF9945)
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
                                    containerColor = Color(0xFFFF9945)
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
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
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
                            onClick = { viewModel.setPublic(true) },
                            modifier = Modifier.fillMaxWidth()
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
                            onClick = { viewModel.setPublic(false) },
                            modifier = Modifier.fillMaxWidth()
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

