package com.example.madclass01.presentation.profile.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.presentation.common.component.TagChip
import com.example.madclass01.presentation.common.component.TagInputField
import com.example.madclass01.presentation.profile.component.ImageGalleryGrid
import com.example.madclass01.presentation.profile.viewmodel.ProfileSetupViewModel

@Composable
fun ProfileSetupScreen(
    userId: String? = null,  // userId 추가
    viewModel: ProfileSetupViewModel = hiltViewModel(),
    onProfileComplete: (nickname: String, age: Int, region: String, images: List<String>) -> Unit = { _, _, _, _ -> },
    onProceedToTagSelection: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
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
    
    LaunchedEffect(uiState.isProfileComplete) {
        if (uiState.isProfileComplete) {
            onProceedToTagSelection()
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
            Text(
                text = "프로필 설정",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )
            
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
            
            // 지역 입력
            OutlinedTextField(
                value = uiState.region,
                onValueChange = { viewModel.updateRegion(it) },
                label = { Text("지역") },
                placeholder = { Text("지역을 입력하세요") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9945),
                    unfocusedBorderColor = Color(0xFFDDDDDD)
                )
            )
            
            // 취미 섹션
            Text(
                text = "취미",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp)
            )
            
            TagInputField(
                onAddTag = { viewModel.addHobby(it) },
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            if (uiState.hobbies.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.hobbies) { hobby ->
                        TagChip(
                            label = hobby.name,
                            isSelected = true,
                            onRemove = { viewModel.removeHobby(hobby.id) }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // 흥미 섹션
            Text(
                text = "흥미",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp)
            )
            
            TagInputField(
                onAddTag = { viewModel.addInterest(it) },
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            if (uiState.interests.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.interests) { interest ->
                        TagChip(
                            label = interest.name,
                            isSelected = true,
                            onRemove = { viewModel.removeInterest(interest.id) }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
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
                onClick = { viewModel.proceedToNextStep() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9945),
                    disabledContainerColor = Color(0xFFCCCCCC)
                )
            ) {
                Text(
                    text = "다음",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
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
