package com.example.madclass01.presentation.profile.screen

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.madclass01.domain.model.ImageItem
import com.example.madclass01.presentation.common.component.TagChip
import com.example.madclass01.presentation.profile.component.ImageGalleryGrid
import com.example.madclass01.presentation.profile.viewmodel.ProfileEditViewModel
import java.io.File
import java.io.FileOutputStream

// URI를 File로 변환하는 헬퍼 함수
private fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        android.util.Log.e("ProfileEditScreen", "Error converting URI to File", e)
        null
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
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

    // Handle system back press
    androidx.activity.compose.BackHandler(onBack = onBack)

    var profileImage by remember { mutableStateOf(initialProfileImage) }
    var nickname by remember { mutableStateOf(initialNickname) }
    var ageText by remember { mutableStateOf(initialAge?.toString() ?: "") }
    var region by remember { mutableStateOf(initialRegion ?: "") }
    var bio by remember { mutableStateOf(initialBio) }
    var nicknameError by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(initialTags.toSet()) }
    var photoInterestTags by remember { mutableStateOf(initialPhotoInterests.toSet()) }
    var showTagSelector by remember { mutableStateOf(false) }
    
    // 프로필 사진 선택을 위한 이미지 피커
    val profileImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(context, it)
            if (file != null) {
                viewModel.uploadProfileImage(userId, file)
            }
        }
    }
    
    LaunchedEffect(uiState.uploadedProfileImageUrl) {
        if (uiState.uploadedProfileImageUrl != null) {
            profileImage = uiState.uploadedProfileImageUrl
            Toast.makeText(context, "프로필 사진이 변경되었습니다", Toast.LENGTH_SHORT).show()
        }
    }

    val images = remember {
        mutableStateListOf<String>().apply {
            addAll(initialImages)
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
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
    
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, "저장 실패: ${uiState.error}", Toast.LENGTH_LONG).show()
        }
    }

    // Defines a modern gradient for the header
    val headerBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFF9945),
            Color(0xFFFFB775)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
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
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.offset(x = (-12).dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        
                         Text(
                            text = "프로필 수정",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        TextButton(
                            onClick = {
                                val trimmed = nickname.trim()
                                if (trimmed.length < 2) {
                                    nicknameError = "닉네임은 2자 이상이어야 합니다"
                                    return@TextButton
                                }
                                val parsedAge = ageText.trim().toIntOrNull()
                                val finalRegion = region.trim().ifBlank { null }
                                val finalBio = bio.trim()

                                val keptImageUrls = mutableListOf<String>()
                                val newImageFiles = mutableListOf<File>()
                                
                                images.forEach { uriString ->
                                    if (uriString.startsWith("http")) {
                                        keptImageUrls.add(uriString)
                                    } else {
                                        // Assume it's a local URI (content:// or file://)
                                        try {
                                            val uri = Uri.parse(uriString)
                                            val file = uriToFile(context, uri)
                                            if (file != null) {
                                                newImageFiles.add(file)
                                            }
                                        } catch (e: Exception) {
                                            // ignore
                                        }
                                    }
                                }

                                viewModel.updateProfile(
                                    userId = userId,
                                    nickname = trimmed,
                                    profileImageUrl = profileImage, // Pass current profile image
                                    age = parsedAge,
                                    region = finalRegion,
                                    bio = finalBio,
                                    tags = selectedTags.toList(),
                                    keptImageUrls = keptImageUrls,
                                    newImageFiles = newImageFiles
                                )
                            },
                            enabled = !uiState.isLoading,
                             modifier = Modifier.offset(x = 12.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "완료",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Column(
                 modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Profile Photo
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .shadow(8.dp, CircleShape)
                            .background(Color.White, CircleShape)
                            .clickable { profileImagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                         if (profileImage != null) {
                            AsyncImage(
                                model = profileImage,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                             Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        
                        // Edit Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.1f), CircleShape),
                             contentAlignment = Alignment.BottomCenter
                        ) {
                             Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (uiState.isUploadingImage) {
                            CircularProgressIndicator(color = Color(0xFFFF9945))
                        }
                    }
                }
                
                // 2. Basic Info
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
                        
                        // Nickname
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = {
                                nickname = it
                                nicknameError = ""
                            },
                            label = { Text("닉네임") },
                            isError = nicknameError.isNotEmpty(),
                            supportingText = { if (nicknameError.isNotEmpty()) Text(nicknameError) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9945),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Age
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { if (it.isBlank() || it.all { c -> c.isDigit() }) ageText = it },
                            label = { Text("나이") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9945),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Region
                        var regionExpanded by remember { mutableStateOf(false) }
                        val regions = listOf("선택 안함", "서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시", "대전광역시", "울산광역시", "세종특별자치시", "경기도", "강원특별자치도", "충청북도", "충청남도", "전북특별자치도", "전라남도", "경상북도", "경상남도", "제주특별자치도")

                        Box {
                            OutlinedTextField(
                                value = region.ifBlank { "선택 안함" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("지역") },
                                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF9945),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { regionExpanded = true }
                            )
                            // Overlay transparent clickable box to handle click properly over TextField
                            Box(modifier = Modifier.matchParentSize().clickable { regionExpanded = true })
                            
                            DropdownMenu(
                                expanded = regionExpanded,
                                onDismissRequest = { regionExpanded = false },
                                modifier = Modifier.heightIn(max = 300.dp).background(Color.White)
                            ) {
                                regions.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = {
                                            region = if (item == "선택 안함") "" else item
                                            regionExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Bio
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { if (it.length <= 500) bio = it },
                            label = { Text("자기소개") },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9945),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // 3. Tags
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
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "관심 태그",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            TextButton(onClick = { showTagSelector = true }) {
                                Text("수정", color = Color(0xFFFF9945), fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        if (selectedTags.isEmpty() && photoInterestTags.isEmpty()) {
                            Text(
                                text = "태그를 추가해서 나를 표현해보세요!",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        } else {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                (selectedTags + photoInterestTags).forEach { tag ->
                                    TagChip(
                                        label = tag,
                                        isSelected = true,
                                        onToggle = {
                                            if (tag in selectedTags) selectedTags = selectedTags - tag
                                            if (tag in photoInterestTags) photoInterestTags = photoInterestTags - tag
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Gallery (Read-onlyish)
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "내 갤러리",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
    // 갤러리 사진 추가를 위한 이미지 피커 (다중 선택)
    val galleryImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val file = uriToFile(context, uri)
            if (file != null) {
                // TODO: Upload mechanism for gallery images if needed imediately, 
                // or just add local URI to display and upload on Save.
                // Currently assuming we just add to list and ViewModel handles upload/sync on Save/Update.
                // But wait, the current `images` list is String (URL or URI).
                // If it's a new local file, we should keep it as URI string.
                images.add(uri.toString())
            }
        }
    }

                        ImageGalleryGrid(
                             images = images.map { ImageItem(uri = it) },
                             onRemoveImage = { uriToRemove -> 
                                 images.remove(uriToRemove)
                             },
                             onAddImage = { 
                                 galleryImagePicker.launch("image/*")
                             },
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .heightIn(max = 280.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Tag Dialog
    if (showTagSelector) {
        AlertDialog(
            onDismissRequest = { showTagSelector = false },
            containerColor = Color.White,
            title = { Text("관심사 선택", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    var customTag by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customTag,
                            onValueChange = { customTag = it },
                            placeholder = { Text("직접 입력") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                if (customTag.isNotBlank()) {
                                    selectedTags = selectedTags + customTag.trim()
                                    customTag = ""
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9945))
                        ) {
                            Text("추가")
                        }
                    }
                    Divider()
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allAvailableTags.forEach { tag ->
                            val isSelected = tag in selectedTags
                             FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) selectedTags = selectedTags - tag else selectedTags = selectedTags + tag
                                },
                                label = { Text(tag) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF9945),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTagSelector = false }) {
                    Text("완료", color = Color(0xFFFF9945))
                }
            }
        )
    }
}
