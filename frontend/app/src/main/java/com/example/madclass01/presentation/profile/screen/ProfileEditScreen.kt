package com.example.madclass01.presentation.profile.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.madclass01.domain.model.ImageItem
import com.example.madclass01.presentation.common.component.TagChip
import com.example.madclass01.presentation.profile.component.ImageGalleryGrid

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProfileEditScreen(
    initialProfileImage: String? = null,
    initialNickname: String,
    initialAge: Int?,
    initialRegion: String?,
    initialBio: String,
    initialImages: List<String>,
    initialTags: List<String> = emptyList(),
    initialPhotoInterests: List<String> = emptyList(),
    allAvailableTags: List<String> = listOf(
        "운동", "여행", "음악", "영화", "독서", "게임",
        "요리", "사진", "그림", "춤", "노래", "악기",
        "등산", "러닝", "수영", "자전거", "요가", "필라테스",
        "맛집", "카페", "베이킹", "바리스타", "와인",
        "반려동물", "고양이", "강아지", "식물", "원예"
    ),
    onBack: () -> Unit,
    onSave: (
        profileImage: String?,
        nickname: String,
        age: Int?,
        region: String?,
        bio: String,
        images: List<String>,
        tags: List<String>
    ) -> Unit
) {
    LaunchedEffect(initialTags, initialPhotoInterests) {
        android.util.Log.d("ProfileEditScreen", "Initial Tags: $initialTags")
        android.util.Log.d("ProfileEditScreen", "Initial Photo Interests: $initialPhotoInterests")
    }

    var profileImage by remember { mutableStateOf(initialProfileImage) }
    var nickname by remember { mutableStateOf(initialNickname) }
    var ageText by remember { mutableStateOf(initialAge?.toString() ?: "") }
    var region by remember { mutableStateOf(initialRegion ?: "") }
    var bio by remember { mutableStateOf(initialBio) }
    var nicknameError by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(initialTags.toSet()) }  // 사용자가 선택한 관심사
    var photoInterestTags by remember { mutableStateOf(initialPhotoInterests.toSet()) }  // 사진에서 추출한 관심사
    var showTagSelector by remember { mutableStateOf(false) }

    val images = remember {
        mutableStateListOf<String>().apply {
            addAll(initialImages)
        }
    }

    // 프로필 사진 선택 런처
    val profileImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileImage = it.toString()
        }
    }

    // 갤러리 이미지 다중 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult

        val maxCount = 30
        val remaining = (maxCount - images.size).coerceAtLeast(0)
        uris.take(remaining).forEach { uri ->
            val value = uri.toString()
            if (!images.contains(value)) {
                images.add(value)
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .imePadding(),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text(text = "프로필 수정", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val trimmed = nickname.trim()
                            if (trimmed.length < 2) {
                                nicknameError = "닉네임은 2자 이상이어야 합니다"
                                return@TextButton
                            }

                            // 최소 20장 검증
                            if (images.size < 20) {
                                return@TextButton
                            }

                            val parsedAge = ageText.trim().toIntOrNull()
                            val finalRegion = region.trim().ifBlank { null }
                            val finalBio = bio.trim()

                            onSave(
                                profileImage,
                                trimmed,
                                parsedAge,
                                finalRegion,
                                finalBio,
                                images.toList(),
                                selectedTags.toList()
                            )
                        }
                    ) {
                        Text(text = "저장", color = Color(0xFFFF9945), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 프로필 사진 섹션
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "프로필 사진",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.align(Alignment.Start)
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5))
                        .border(2.dp, Color(0xFFE0E0E0), CircleShape)
                        .clickable { profileImageLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImage != null) {
                        AsyncImage(
                            model = profileImage,
                            contentDescription = "프로필 사진",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "사진 추가",
                            tint = Color(0xFFBBBBBB),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Text(
                    text = "탭하여 프로필 사진 변경",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }

            // 기본 정보
            OutlinedTextField(
                value = nickname,
                onValueChange = {
                    nickname = it
                    nicknameError = ""
                },
                label = { Text("닉네임") },
                isError = nicknameError.isNotEmpty(),
                supportingText = {
                    if (nicknameError.isNotEmpty()) {
                        Text(text = nicknameError, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9945),
                    unfocusedBorderColor = Color(0xFFDDDDDD),
                    errorBorderColor = Color(0xFFD32F2F)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ageText,
                onValueChange = { value ->
                    // 숫자만 허용 (빈 값은 허용)
                    if (value.isBlank() || value.all { it.isDigit() }) {
                        ageText = value
                    }
                },
                label = { Text("나이") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9945),
                    unfocusedBorderColor = Color(0xFFDDDDDD)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = region,
                onValueChange = { region = it },
                label = { Text("지역") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9945),
                    unfocusedBorderColor = Color(0xFFDDDDDD)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 500) bio = it },
                label = { Text("자기소개") },
                minLines = 4,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9945),
                    unfocusedBorderColor = Color(0xFFDDDDDD)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // 관심 태그 섹션
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "관심 태그",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    TextButton(onClick = { showTagSelector = true }) {
                        Text(
                            text = if (selectedTags.isEmpty() && photoInterestTags.isEmpty()) "태그 추가" else "태그 수정",
                            fontSize = 14.sp,
                            color = Color(0xFFFF9945)
                        )
                    }
                }

                if (selectedTags.isEmpty() && photoInterestTags.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "관심 태그를 선택해주세요",
                            fontSize = 14.sp,
                            color = Color(0xFF999999),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
1                        // 모든 관심사 태그 (사용자 선택 + 사진 추출)를 같은 스타일로 통합 표시
                        (selectedTags + photoInterestTags).forEach { tag ->
                            TagChip(
                                label = tag,
                                isSelected = true,
                                onToggle = {
                                    // 클릭하면 태그 제거
                                    if (tag in selectedTags) {
                                        selectedTags = selectedTags - tag
                                    } else if (tag in photoInterestTags) {
                                        photoInterestTags = photoInterestTags - tag
                                    }
                                },
                                modifier = Modifier
                            )
                        }
                    }
                }
            }

            // 갤러리 사진 섹션
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "사진 갤러리 (${images.size}/30)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                if (images.size < 20) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFF4E6),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⚠️ 최소 20장의 사진이 필요합니다 (현재 ${images.size}장)",
                            fontSize = 14.sp,
                            color = Color(0xFFFF9945),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "✓ 최소 사진 개수를 충족했습니다",
                            fontSize = 14.sp,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Text(
                    text = "최대 30장까지 선택 가능 (20장 이상 필수)",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )

                ImageGalleryGrid(
                    images = images.map { ImageItem(uri = it) },
                    onRemoveImage = { uri ->
                        images.remove(uri)
                    },
                    onAddImage = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 태그 선택 다이얼로그
    if (showTagSelector) {
        var customTagInput by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showTagSelector = false },
            title = {
                Text(
                    text = "관심 태그 선택",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 직접 입력 필드
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customTagInput,
                            onValueChange = { customTagInput = it },
                            placeholder = { Text("직접 입력", fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9945),
                                unfocusedBorderColor = Color(0xFFDDDDDD)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        
                        Button(
                            onClick = {
                                val trimmed = customTagInput.trim()
                                if (trimmed.isNotEmpty() && trimmed !in selectedTags && trimmed !in photoInterestTags) {
                                    selectedTags = selectedTags + trimmed
                                    customTagInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9945)
                            ),
                            enabled = customTagInput.trim().isNotEmpty(),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("추가")
                        }
                    }
                    
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    
                    // 모든 관심사 통합 섹션 (사용자 선택 + 사진 추출)
                    Text(
                        text = "관심사 태그",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        allAvailableTags.forEach { tag ->
                            val isSelected = tag in selectedTags || tag in photoInterestTags
                            TagChip(
                                label = tag,
                                isSelected = isSelected,
                                onToggle = {
                                    if (isSelected) {
                                        // 둘 중 어디에 있든 제거
                                        selectedTags = selectedTags - tag
                                        photoInterestTags = photoInterestTags - tag
                                    } else {
                                        // 사용자 선택 태그에 추가
                                        selectedTags = selectedTags + tag
                                    }
                                },
                                modifier = Modifier
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTagSelector = false }) {
                    Text("확인", color = Color(0xFFFF9945))
                }
            }
        )
    }
}
