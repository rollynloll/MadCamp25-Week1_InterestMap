@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.madclass01.presentation.group.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.madclass01.presentation.group.viewmodel.CreateGroupViewModel

@Composable
fun CreateGroupScreen(
    userId: String,
    viewModel: CreateGroupViewModel = hiltViewModel(),
    onCreateSuccess: (String) -> Unit = { },
    onBackPress: () -> Unit = { }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    var tagInputValue by remember { mutableStateOf("") }
    var regionExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val regions = listOf(
        "전체", "서울특별시", "부산광역시", "대구광역시", "인천광역시",
        "광주광역시", "대전광역시", "울산광역시", "세종특별자치시",
        "경기도", "강원특별자치도", "충청북도", "충청남도",
        "전북특별자치도", "전라남도", "경상북도", "경상남도",
        "제주특별자치도"
    )

    val profileImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateProfileImageUri(it.toString()) }
    }

    LaunchedEffect(uiState.isCreateSuccess) {
        if (uiState.isCreateSuccess && uiState.createdGroupId != null) {
            onCreateSuccess(uiState.createdGroupId!!)
            viewModel.resetCreateState()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "새 그룹 만들기",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { if (!uiState.isLoading) onBackPress() }) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.createGroup(userId, context) },
                        enabled = !uiState.isLoading && uiState.groupName.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "완료",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // 1. 프로필 이미지 섹션
            ProfileSection(
                uiState = uiState,
                onIconTypeSelected = { viewModel.selectIconType(it) },
                onImagePickRequested = { profileImageLauncher.launch("image/*") },
                onModeChanged = { useCustomImage ->
                     if (useCustomImage) {
                         // 이미지 모드로 전환 시 이미지가 없으면 런처 실행
                         if (uiState.profileImageUri == null) {
                             profileImageLauncher.launch("image/*")
                         }
                     } else {
                         // 아이콘 모드로 전환 (이미지 URI는 유지하거나 null 처리 - ViewModel 로직 따름)
                         viewModel.updateProfileImageUri(null)
                     }
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 2. 기본 정보 섹션 (이름, 설명)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionHeader("기본 정보")
                
                OutlinedTextField(
                    value = uiState.groupName,
                    onValueChange = { viewModel.updateGroupName(it) },
                    label = { Text("그룹 이름") },
                    placeholder = { Text("예: 서울 러너스") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = uiState.groupDescription,
                    onValueChange = { viewModel.updateGroupDescription(it) },
                    label = { Text("그룹 설명") },
                    placeholder = { Text("그룹의 활동 내용과 목표를 적어주세요.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // 3. 지역 및 태그 섹션
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionHeader("상세 설정")

                // 지역 선택
                Box {
                    OutlinedTextField(
                        value = uiState.selectedRegion,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("지역") },
                        trailingIcon = { 
                            Icon(
                                Icons.Default.ArrowDropDown, 
                                contentDescription = null,
                                modifier = Modifier.rotate(if (regionExpanded) 180f else 0f)
                            ) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { regionExpanded = true },
                        enabled = false, // 클릭 이벤트는 Box가 처리
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledContainerColor = Color.Transparent
                        )
                    )
                    // 투명 버튼으로 TextField 위를 덮어 클릭 감지
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { regionExpanded = true }
                    )

                    DropdownMenu(
                        expanded = regionExpanded,
                        onDismissRequest = { regionExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .heightIn(max = 300.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        regions.forEach { region ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        region,
                                        fontWeight = if (region == uiState.selectedRegion) FontWeight.Bold else FontWeight.Normal,
                                        color = if (region == uiState.selectedRegion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    ) 
                                },
                                onClick = {
                                    viewModel.updateRegion(region)
                                    regionExpanded = false
                                }
                            )
                        }
                    }
                }

                // 태그 입력
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "관련 태그",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = tagInputValue,
                        onValueChange = { tagInputValue = it },
                        placeholder = { Text("태그 입력 후 엔터 (예: #운동)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (tagInputValue.isNotBlank()) {
                                    viewModel.addTag(tagInputValue)
                                    tagInputValue = ""
                                }
                            }
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (tagInputValue.isNotBlank()) {
                                        viewModel.addTag(tagInputValue)
                                        tagInputValue = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "추가")
                            }
                        }
                    )

                    AnimatedVisibility(visible = uiState.selectedTags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            uiState.selectedTags.forEach { tag ->
                                InputChip(
                                    selected = false,
                                    onClick = { viewModel.removeTag(tag) },
                                    label = { Text("#$tag") },
                                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    colors = InputChipDefaults.inputChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    border = null
                                )
                            }
                        }
                    }
                }
            }

            // 4. 공개 설정
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("공개 설정")
                
                PrivacyOptionCard(
                    title = "공개 그룹",
                    description = "누구나 검색하고 그룹에 참여할 수 있습니다.",
                    icon = Icons.Outlined.Public,
                    isSelected = uiState.isPublic,
                    onClick = { viewModel.setPublic(true) }
                )
                
                PrivacyOptionCard(
                    title = "비공개 그룹",
                    description = "초대 링크를 통해서만 참여할 수 있습니다.",
                    icon = Icons.Outlined.Lock,
                    isSelected = !uiState.isPublic,
                    onClick = { viewModel.setPublic(false) }
                )
            }

            // 에러 메시지
            AnimatedVisibility(
                visible = uiState.errorMessage.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileSection(
    uiState: com.example.madclass01.presentation.group.viewmodel.CreateGroupUiState,
    onIconTypeSelected: (String) -> Unit,
    onImagePickRequested: () -> Unit,
    onModeChanged: (Boolean) -> Unit // true: custom image, false: icon
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 미리보기 및 모드 선택
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                shape = CircleShape,
                modifier = Modifier.size(120.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                if (uiState.useCustomImage && uiState.profileImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uiState.profileImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "프로필 이미지",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconVector(uiState.selectedIconType),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 편집 버튼
            SmallFloatingActionButton(
                onClick = { 
                    if (uiState.useCustomImage) onImagePickRequested()
                    else onModeChanged(true) // 아이콘 모드에서 클릭 시 이미지 모드로 변경 유도
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.offset(x = 4.dp, y = 4.dp)
            ) {
                Icon(
                    if (uiState.useCustomImage) Icons.Default.Edit else Icons.Default.Image,
                    contentDescription = "편집"
                )
            }
        }

        // 모드 선택 탭
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = !uiState.useCustomImage,
                onClick = { onModeChanged(false) },
                shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                icon = { Icon(Icons.Outlined.EmojiEmotions, null, modifier = Modifier.size(18.dp)) }
            ) {
                Text("기본 아이콘")
            }
            SegmentedButton(
                selected = uiState.useCustomImage,
                onClick = { onModeChanged(true) },
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                icon = { Icon(Icons.Outlined.Image, null, modifier = Modifier.size(18.dp)) }
            ) {
                Text("앨범에서 선택")
            }
        }

        // 아이콘 선택 그리드 (아이콘 모드일 때만)
        AnimatedVisibility(
            visible = !uiState.useCustomImage,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val icons = listOf(
                "users" to Icons.Default.Group,
                "coffee" to Icons.Default.Coffee,
                "camera" to Icons.Default.CameraAlt,
                "mountain" to Icons.Default.Landscape,
                "music" to Icons.Default.MusicNote,
                "book" to Icons.Default.MenuBook,
                "sports" to Icons.Default.SportsSoccer,
                "food" to Icons.Default.Restaurant
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp) // 2줄 정도의 높이
            ) {
                items(icons) { (type, icon) ->
                    IconSelectionItem(
                        icon = icon,
                        isSelected = uiState.selectedIconType == type,
                        onClick = { onIconTypeSelected(type) }
                    )
                }
            }
        }
    }
}

@Composable
fun IconSelectionItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.aspectRatio(1f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PrivacyOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                             else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = null // Card click handles this
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold
    )
}

fun Modifier.rotate(degrees: Float): Modifier = this.then(
    Modifier.graphicsLayer { rotationZ = degrees }
)

fun getIconVector(type: String): ImageVector {
    return when (type) {
        "users" -> Icons.Default.Group
        "coffee" -> Icons.Default.Coffee
        "camera" -> Icons.Default.CameraAlt
        "mountain" -> Icons.Default.Landscape
        "music" -> Icons.Default.MusicNote
        "book" -> Icons.Default.MenuBook
        "sports" -> Icons.Default.SportsSoccer
        "food" -> Icons.Default.Restaurant
        else -> Icons.Default.Group
    }
}
