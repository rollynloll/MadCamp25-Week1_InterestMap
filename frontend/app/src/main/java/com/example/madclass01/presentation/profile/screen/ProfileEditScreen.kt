package com.example.madclass01.presentation.profile.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madclass01.domain.model.ImageItem
import com.example.madclass01.presentation.profile.component.ImageGalleryGrid

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProfileEditScreen(
    initialNickname: String,
    initialAge: Int?,
    initialRegion: String?,
    initialBio: String,
    initialImages: List<String>,
    onBack: () -> Unit,
    onSave: (nickname: String, age: Int?, region: String?, bio: String, images: List<String>) -> Unit
) {
    var nickname by remember { mutableStateOf(initialNickname) }
    var ageText by remember { mutableStateOf(initialAge?.toString() ?: "") }
    var region by remember { mutableStateOf(initialRegion ?: "") }
    var bio by remember { mutableStateOf(initialBio) }
    var nicknameError by remember { mutableStateOf("") }

    val images = remember {
        mutableStateListOf<String>().apply {
            addAll(initialImages)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult

        val maxCount = 20
        val remaining = (maxCount - images.size).coerceAtLeast(0)
        uris.take(remaining).forEach { uri ->
            val value = uri.toString()
            if (!images.contains(value)) {
                images.add(value)
            }
        }
    }

    Scaffold(
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

                            val parsedAge = ageText.trim().toIntOrNull()
                            val finalRegion = region.trim().ifBlank { null }
                            val finalBio = bio.trim()

                            onSave(
                                trimmed,
                                parsedAge,
                                finalRegion,
                                finalBio,
                                images.toList()
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
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

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "사진",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Text(
                text = "최대 20개까지 선택 가능",
                fontSize = 12.sp,
                color = Color(0xFF999999),
                modifier = Modifier.padding(top = 6.dp)
            )

            ImageGalleryGrid(
                images = images.map { ImageItem(uri = it) },
                onRemoveImage = { uri -> images.remove(uri) },
                onAddImage = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
