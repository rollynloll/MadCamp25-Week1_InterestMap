package com.example.madclass01.presentation.profile.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.presentation.common.component.TagChip
import com.example.madclass01.presentation.common.component.TagInputField
import com.example.madclass01.presentation.profile.component.ImageGalleryGrid
import com.example.madclass01.presentation.profile.viewmodel.ProfileSetupViewModel

// 미리 정의된 관심사 태그 목록
private val PREDEFINED_INTEREST_TAGS = listOf(
    "여행", "운동", "음악", "영화", "독서", "요리",
    "게임", "사진", "그림", "춤", "노래", "쇼핑",
    "패션", "뷰티", "카페", "맛집", "등산", "캠핑",
    "자전거", "수영", "러닝", "헬스", "요가", "필라테스",
    "악기연주", "영화감상", "드라마", "애니메이션", "만화",
    "반려동물", "식물키우기", "DIY", "베이킹", "낚시",
    "보드게임", "카드게임", "볼링", "당구", "클라이밍"
)

@Composable
fun ProfileSetupScreen(
    userId: String? = null,
    viewModel: ProfileSetupViewModel = hiltViewModel(),
    isEditMode: Boolean = false,
    onBack: () -> Unit = {},
    onProfileComplete: (nickname: String, age: Int, region: String, bio: String, images: List<String>, recommendedTags: List<String>, interests: List<String>) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAllTags by remember { mutableStateOf(false) }

    // userId 설정
    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.setUserId(userId)
        }
    }

    // 다중 이미지 선택
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                val fileName = getFileName(context, uri)
                viewModel.addImage(uri.toString(), fileName)
            }
        }
    }

    LaunchedEffect(uiState.isProfileComplete, uiState.isAnalyzingImages, uiState.isLoading) {
        if (uiState.isProfileComplete && !uiState.isAnalyzingImages && !uiState.isLoading) {
            onProfileComplete(
                uiState.nickname,
                uiState.age,
                uiState.region,
                uiState.bio,
                uiState.images.map { it.uri },
                uiState.recommendedTags,
                uiState.interests.map { it.name }
            )
            viewModel.resetCompleteState()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearErrorMessage()
        }
    }

    // Defines a modern gradient for the header
    val headerBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFF9945), // Original Orange
            Color(0xFFFFB775)  // Lighter Orange
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9)) // Slightly off-white background
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Header Section ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(headerBrush, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(bottom = 24.dp, top = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    // Back Button
                    IconButton(
                        onClick = { if (!uiState.isLoading) onBack() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.offset(x = (-12).dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "프로필 만들기",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "나를 멋지게 소개해볼까요? \uD83D\uDE0E",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- Section 1: Basic Info ---
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "기본 정보",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )

                        // 닉네임 입력
                        OutlinedTextField(
                            value = uiState.nickname,
                            onValueChange = { viewModel.updateNickname(it) },
                            label = { Text("닉네임") },
                            placeholder = { Text("2자 이상 입력") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9945)
                                )
                            },
                            isError = uiState.nicknameError.isNotEmpty(),
                            supportingText = {
                                if (uiState.nicknameError.isNotEmpty()) {
                                    Text(
                                        text = uiState.nicknameError,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9945),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                errorBorderColor = Color(0xFFD32F2F)
                            )
                        )

                        // 나이 입력
                        OutlinedTextField(
                            value = if (uiState.age == 0) "" else uiState.age.toString(),
                            onValueChange = {
                                val age = it.toIntOrNull() ?: 0
                                if (age <= 120) viewModel.updateAge(age)
                            },
                            label = { Text("나이") },
                            placeholder = { Text("나이를 입력하세요") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9945),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )

                        // 성별 선택
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "성별",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF666666)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("male" to "남성", "female" to "여성", "undecided" to "미정").forEach { (key, label) ->
                                    val isSelected = uiState.gender == key
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateGender(key) },
                                        label = {
                                            Text(
                                                text = label,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth(),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(50),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFFF9945),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFFF5F5F5),
                                            labelColor = Color(0xFF666666)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = if (isSelected) Color.Transparent else Color(0xFFE0E0E0)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // --- Section 2: Region ---
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "거주 지역",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )

                        var regionExpanded by remember { mutableStateOf(false) }
                        val regions = listOf(
                            "선택 안함", "서울특별시", "부산광역시", "대구광역시", "인천광역시",
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
                                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                    .clickable { regionExpanded = true }
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (uiState.region.isNotBlank()) Color(0xFFFF9945) else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = uiState.region.ifBlank { "지역 선택하기" },
                                        fontSize = 16.sp,
                                        color = if (uiState.region.isBlank()) Color.Gray else Color(0xFF333333)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                            DropdownMenu(
                                expanded = regionExpanded,
                                onDismissRequest = { regionExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .background(Color.White)
                                    .heightIn(max = 300.dp)
                            ) {
                                regions.forEach { region ->
                                    DropdownMenuItem(
                                        text = { Text(region) },
                                        onClick = {
                                            viewModel.updateRegion(if (region == "선택 안함") "" else region)
                                            regionExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // --- Section 3: Interests ---
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "관심사",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Text(
                                text = "${uiState.interests.size} / 3+",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.interests.size >= 3) Color(0xFFFF9945) else Color.Gray
                            )
                        }

                        // Input Field
                        TagInputField(
                            onAddTag = { viewModel.addHobby(it) },
                            placeholderText = "관심사를 직접 입력해보세요",
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Selected Tags
                        if (uiState.interests.isNotEmpty()) {
                            Text(
                                text = "선택된 관심사",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            FlowRow(
                                mainAxisSpacing = 8.dp,
                                crossAxisSpacing = 8.dp
                            ) {
                                uiState.interests.forEach { tag ->
                                    TagChip(
                                        label = tag.name,
                                        isSelected = true,
                                        onRemove = { viewModel.removeHobby(tag.id) }
                                    )
                                }
                            }
                            Divider(color = Color(0xFFEEEEEE))
                        }

                        // Recommended Tags
                        Text(
                            text = "이런 관심사는 어때요?",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        
                        val displayTags = if (showAllTags) PREDEFINED_INTEREST_TAGS else PREDEFINED_INTEREST_TAGS.take(12)
                        FlowRow(
                            mainAxisSpacing = 8.dp,
                            crossAxisSpacing = 8.dp
                        ) {
                            displayTags.forEach { tagName ->
                                val isSelected = uiState.interests.any { it.name == tagName }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) {
                                            uiState.interests.find { it.name == tagName }?.let { viewModel.removeHobby(it.id) }
                                        } else {
                                            viewModel.addHobby(tagName)
                                        }
                                    },
                                    label = { Text(tagName) },
                                    shape = RoundedCornerShape(50),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFF9945),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF5F5F5),
                                        labelColor = Color(0xFF666666)
                                    ),
                                    border = null
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = { showAllTags = !showAllTags },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = if (showAllTags) "접기 ▲" else "더보기 ▼",
                                color = Color(0xFFFF9945)
                            )
                        }
                    }
                }

                // --- Section 4: Photos ---
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                         Text(
                            text = "나를 표현하는 사진",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                         Text(
                            text = "사진을 올리면 AI가 관심사를 자동으로 추천해줘요!\n(최소 1장 필수)",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )

                        ImageGalleryGrid(
                            images = uiState.images,
                            onRemoveImage = { viewModel.removeImage(it) },
                            onAddImage = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // --- Error Message Block ---
                if (uiState.errorMessage.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = Color(0xFFD32F2F),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Submit Button ---
                Button(
                    onClick = { if (!uiState.isLoading) viewModel.proceedToNextStep(context) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9945),
                        disabledContainerColor = Color(0xFFFFCCAA)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "완료하고 시작하기",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Overlay for AI Analysis
        if (uiState.isAnalyzingImages) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(enabled = false) {}, // Block touches
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFF9945),
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "AI 분석 중...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "사진에서 관심사를 찾고 있어요 \uD83D\uDD0D",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

fun getFileName(context: Context, uri: Uri): String {
    var name = ""
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex("_display_name")
        if (nameIndex >= 0) {
            it.moveToFirst()
            name = it.getString(nameIndex)
        }
    }
    return name
}
