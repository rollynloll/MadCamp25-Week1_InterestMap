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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
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

import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.presentation.profile.viewmodel.ProfileEditViewModel

import android.widget.Toast

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProfileEditScreen(
    userId: String,
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
    viewModel: ProfileEditViewModel = hiltViewModel(),
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
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

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
    var selectedTags by remember { mutableStateOf(initialTags.toSet()) }
    var photoInterestTags by remember { mutableStateOf(initialPhotoInterests.toSet()) }
    var showTagSelector by remember { mutableStateOf(false) }

    val images = remember {
        mutableStateListOf<String>().apply {
            addAll(initialImages)
        }
    }

    // API 호출 성공 시 onSave 콜백 호출 (부모 화면 상태 업데이트 및 이동)
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            android.util.Log.d("ProfileEditScreen", "Update success, calling onSave")
            val parsedAge = ageText.trim().toIntOrNull()
            val finalRegion = region.trim().ifBlank { null }
            val finalBio = bio.trim()
            
            viewModel.resetSuccess()
            onSave(
                profileImage,
                nickname.trim(),
                parsedAge,
                finalRegion,
                finalBio,
                images.toList(),
                selectedTags.toList()
            )
        }
    }
    
    // 에러 발생 시 Toast 표시
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, "저장 실패: ${uiState.error}", Toast.LENGTH_LONG).show()
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
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                            color = Color(0xFFFF9945),
                            strokeWidth = 2.dp
                        )
                    } else {
                        TextButton(
                            onClick = {
                                android.util.Log.d("ProfileEditScreen", "저장 버튼 클릭됨")
                                
                                val trimmed = nickname.trim()
                                if (trimmed.length < 2) {
                                    nicknameError = "닉네임은 2자 이상이어야 합니다"
                                    android.util.Log.d("ProfileEditScreen", "닉네임 검증 실패: ${trimmed.length}자")
                                    return@TextButton
                                }

                                val parsedAge = ageText.trim().toIntOrNull()
                                val finalRegion = region.trim().ifBlank { null }
                                val finalBio = bio.trim()

                                android.util.Log.d("ProfileEditScreen", "API 호출 시작 - userId: $userId, nickname: $trimmed")
                                
                                viewModel.updateProfile(
                                    userId = userId,
                                    nickname = trimmed,
                                    age = parsedAge,
                                    region = finalRegion,
                                    bio = finalBio,
                                    tags = selectedTags.toList()
                                )
                            },
                            enabled = !uiState.isLoading
                        ) {
                            Text(text = "저장", color = Color(0xFFFF9945), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // Error Snackbar or Dialog logic could be added here
        if (uiState.error != null) {
            // Simple error handling for now - maybe a Toast or Snackbar if Scaffold state allowed
            // For now, let's just log it or maybe show a small error text
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 프로필 사진 섹션 (조회 전용)
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
                        .border(2.dp, Color(0xFFE0E0E0), CircleShape),
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
                            contentDescription = "사진 없음",
                            tint = Color(0xFFBBBBBB),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
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

            // 지역 선택
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "지역",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
                
                var regionExpanded by remember { mutableStateOf(false) }
                val regions = listOf(
                    "선택 안함",
                    "서울특별시", "부산광역시", "대구광역시", "인천광역시",
                    "광주광역시", "대전광역시", "울산광역시", "세종특별자치시",
                    "경기도", "강원특별자치도", "충청북도", "충청남도",
                    "전북특별자치도", "전라남도", "경상북도", "경상남도",
                    "제주특별자치도"
                )
                
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(8.dp))
                            .clickable { regionExpanded = true }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "지역",
                                tint = Color(0xFFFF9945),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = region.ifBlank { "지역을 선택하세요" },
                                fontSize = 15.sp,
                                color = if (region.isBlank()) Color(0xFF999999) else Color(0xFF1A1A1A)
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "선택",
                            tint = Color(0xFF999999),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = regionExpanded,
                        onDismissRequest = { regionExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .heightIn(max = 300.dp)
                    ) {
                        regions.forEach { regionOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = regionOption,
                                        fontSize = 15.sp,
                                        color = if (regionOption == region || (regionOption == "선택 안함" && region.isBlank())) Color(0xFFFF9945) else Color(0xFF1A1A1A),
                                        fontWeight = if (regionOption == region || (regionOption == "선택 안함" && region.isBlank())) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    region = if (regionOption == "선택 안함") "" else regionOption
                                    regionExpanded = false
                                }
                            )
                        }
                    }
                }
            }

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

            // 갤러리 사진 섹션 (조회 전용)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "사진 갤러리 (${images.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                ImageGalleryGrid(
                    images = images.map { ImageItem(uri = it) },
                    onRemoveImage = { /* 삭제 기능 비활성화 */ },
                    onAddImage = { /* 추가 기능 비활성화 */ },
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
