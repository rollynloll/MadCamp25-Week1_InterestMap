package com.example.madclass01.presentation.profile.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    userId: String? = null,  // userId 추가
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

    // 수정 플로우는 ProfileEditScreen을 사용합니다.
    
    // 다중 이미지 선택
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        android.util.Log.d("ProfileSetup", "이미지 선택됨: ${uris.size}개")
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                android.util.Log.d("ProfileSetup", "이미지 추가: $uri")
                val fileName = getFileName(context, uri)
                viewModel.addImage(uri.toString(), fileName)
            }
        } else {
            android.util.Log.w("ProfileSetup", "선택된 이미지 없음")
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        if (!uiState.isLoading) {
                            onBack()
                        }
                    },
                    enabled = !uiState.isLoading
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = if (uiState.isLoading) Color(0xFFCCCCCC) else Color(0xFFFF9945)
                    )
                }

                Text(
                    text = "프로필 설정",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            
            Text(
                text = "기초 정보를 입력해주세요 (1/2)",
                fontSize = 14.sp,
                color = Color(0xFF999999),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 32.dp)
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
                        contentDescription = "닉네임",
                        tint = Color(0xFF666666)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9945),
                    unfocusedBorderColor = Color(0xFFDDDDDD),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9945),
                    unfocusedBorderColor = Color(0xFFDDDDDD)
                )
            )
            
            // 성별 선택
            Text(
                text = "성별",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.gender == "male",
                    onClick = { viewModel.updateGender("male") },
                    label = { Text("남") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF9945),
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = uiState.gender == "female",
                    onClick = { viewModel.updateGender("female") },
                    label = { Text("여") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF9945),
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = uiState.gender == "undecided",
                    onClick = { viewModel.updateGender("undecided") },
                    label = { Text("미정") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF9945),
                        selectedLabelColor = Color.White
                    )
                )
            }
            
            // 지역 선택
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
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
                                text = uiState.region.ifBlank { "지역을 선택하세요" },
                                fontSize = 15.sp,
                                color = if (uiState.region.isBlank()) Color(0xFF999999) else Color(0xFF1A1A1A)
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
                        regions.forEach { region ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = region,
                                        fontSize = 15.sp,
                                        color = if (region == uiState.region) Color(0xFFFF9945) else Color(0xFF1A1A1A),
                                        fontWeight = if (region == uiState.region) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.updateRegion(if (region == "선택 안함") "" else region)
                                    regionExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // 관심사 섹션
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "관심사",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    
                    val totalSelected = uiState.interests.size
                    Text(
                        text = "$totalSelected 개 선택 (최소 3개)",
                        fontSize = 12.sp,
                        color = if (totalSelected >= 3) Color(0xFFFF9945) else Color(0xFF999999),
                        fontWeight = if (totalSelected >= 3) FontWeight.Bold else FontWeight.Normal
                    )
                }
                
                Text(
                    text = "관심사를 선택하거나 직접 입력해주세요",
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 직접 입력
                TagInputField(
                    onAddTag = { viewModel.addHobby(it) },
                    placeholderText = "직접 입력",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 선택된 태그 표시
                val allSelectedTags = uiState.interests
                if (allSelectedTags.isNotEmpty()) {
                    Text(
                        text = "선택된 관심사",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp
                    ) {
                        allSelectedTags.forEach { tag ->
                            TagChip(
                                label = tag.name,
                                isSelected = true,
                                onRemove = { 
                                    viewModel.removeHobby(tag.id)
                                }
                            )
                        }
                    }
                }
                
                // 추천 태그
                Text(
                    text = "추천 관심사",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val displayTags = if (showAllTags) PREDEFINED_INTEREST_TAGS else PREDEFINED_INTEREST_TAGS.take(12)
                val selectedTagNames = uiState.interests.map { it.name }
                
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    displayTags.forEach { tagName ->
                        val isSelected = selectedTagNames.contains(tagName)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    val tag = uiState.interests.find { it.name == tagName }
                                    tag?.let { viewModel.removeHobby(it.id) }
                                } else {
                                    viewModel.addHobby(tagName)
                                }
                            },
                            label = { Text(tagName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF9945),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF5F5F5),
                                labelColor = Color(0xFF666666)
                            )
                        )
                    }
                }
                
                // 더보기/접기 버튼
                TextButton(
                    onClick = { showAllTags = !showAllTags },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (showAllTags) "접기 ▲" else "더보기 ▼",
                        color = Color(0xFFFF9945),
                        fontSize = 13.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 프로필 사진 섹션
            Text(
                text = "프로필 사진",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )
            
            Text(
                text = "최소 1개, 최대 20개까지 선택 가능",
                fontSize = 12.sp,
                color = Color(0xFF999999),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )
            
            // 에러 메시지
            if (uiState.errorMessage.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            // 이미지 갤러리
            ImageGalleryGrid(
                images = uiState.images,
                onRemoveImage = { viewModel.removeImage(it) },
                onAddImage = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )
            
            // 다음 버튼
            Button(
                onClick = { 
                    if (!uiState.isLoading) {
                        viewModel.proceedToNextStep(context)
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9945),
                    disabledContainerColor = Color(0xFFCCCCCC)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "다음",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // 이미지 분석 로딩 오버레이
        if (uiState.isAnalyzingImages) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFFF9945),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "사진 업로드 중...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AI가 사진을 분석하여\n관심사를 추천하고 있어요",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center
                        )
                    }
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
