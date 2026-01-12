package com.example.madclass01.presentation.group.screen

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.domain.model.Group
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import com.example.madclass01.presentation.group.component.CopyLinkButton
import com.example.madclass01.presentation.group.component.GroupInfoCard
import com.example.madclass01.presentation.group.component.QRCodeContainer
import com.example.madclass01.presentation.group.viewmodel.QRInviteViewModel
import com.example.madclass01.utils.QRCodeGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("RememberReturnType")
@Composable
fun QRInviteScreen(
    group: Group,
    memberCount: Int = group.memberCount,
    userId: String = "",
    viewModel: QRInviteViewModel = hiltViewModel(),
    onBackPress: () -> Unit = { },
    onJoinSuccess: () -> Unit = { }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    remember(userId) { Unit }
    var showCopyToast by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.initializeWithGroup(group)
    }
    
    // 초대 링크 자동 복사
    LaunchedEffect(uiState.inviteLink?.inviteUrl) {
        if (uiState.inviteLink?.inviteUrl != null) {
            copyToClipboard(context, uiState.inviteLink!!.inviteUrl)
            viewModel.copyInviteLink()
        }
    }
    
    // QR 코드 생성
    LaunchedEffect(key1 = uiState.inviteLink?.inviteUrl, key2 = Unit) {
        if (uiState.inviteLink?.inviteUrl != null) {
            scope.launch {
                qrBitmap = QRCodeGenerator.generateQRCode(
                    text = uiState.inviteLink!!.inviteUrl,
                    width = 512,
                    height = 512
                )
            }
        }
    }
    
    LaunchedEffect(uiState.copySuccess) {
        if (uiState.copySuccess) {
            showCopyToast = true
            delay(2000)
            showCopyToast = false
            viewModel.resetCopySuccess()
        }
    }
    
    LaunchedEffect(uiState.joinSuccess) {
        if (uiState.joinSuccess) {
            onJoinSuccess()
            viewModel.resetJoinSuccess()
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
                .statusBarsPadding()
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
                // Back Button
                IconButton(
                    onClick = onBackPress
                ) {
                    Text(text = "←", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                
                // Title
                Text(
                    text = "그룹 초대",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
                
                // Share Button
                IconButton(onClick = {
                    val inviteUrl = uiState.inviteLink?.inviteUrl ?: return@IconButton
                    shareMore(context, inviteUrl)
                }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                }
            }
            
            HorizontalDivider(
                color = Color(0xFFE5E7EB),
                thickness = 1.dp
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Group Info Card
                GroupInfoCard(
                    group = group,
                    memberCount = memberCount
                )
                
                // QR Code Container
                QRCodeContainer(
                    qrCodeBitmap = qrBitmap,
                    inviteUrl = uiState.inviteLink?.inviteUrl ?: "",
                    expiryTime = uiState.expiryTime
                )
                
                // Share Options
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.838f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Copy Link Button
                    CopyLinkButton(
                        onClick = {
                            val inviteUrl = uiState.inviteLink?.inviteUrl
                            if (inviteUrl != null) {
                                copyToClipboard(context, inviteUrl)
                                viewModel.copyInviteLink()
                            }
                        }
                    )
                }
                
                // Loading Indicator
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Color(0xFF667EEA),
                        strokeWidth = 2.dp
                    )
                }
                
                // Error Message
                if (uiState.errorMessage.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.838f)
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
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        
        // Copy Toast
        if (showCopyToast) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "✓ 초대 링크 복사됨",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

// Helper Functions

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("invite_link", text)
    clipboard.setPrimaryClip(clip)
}

private fun shareToKakao(context: Context, groupName: String, inviteUrl: String) {
    // 카카오톡 공유 구현 (카카오 SDK 필요)
    try {
        val message = "「$groupName」 그룹에 초대합니다!\n\n$inviteUrl"
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, "카카오톡으로 공유"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun shareToInstagram(context: Context, inviteUrl: String) {
    // 인스타그램 공유 (Direct Message로)
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "그룹 초대 링크: $inviteUrl")
            setPackage("com.instagram.android")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // 기본 공유로 폴백
        shareMore(context, inviteUrl)
    }
}

private fun shareMore(context: Context, inviteUrl: String) {
    // Android 기본 공유 메뉴
    val message = "그룹에 초대합니다!\n\n$inviteUrl"
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(intent, "공유"))
}

// Surface Composable for simpler implementation
@Composable
fun Surface(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    shape: Shape = RoundedCornerShape(0.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(color = color, shape = shape)
    ) {
        content()
    }
}
