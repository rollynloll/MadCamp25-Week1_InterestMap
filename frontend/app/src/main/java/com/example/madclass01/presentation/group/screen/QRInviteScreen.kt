package com.example.madclass01.presentation.group.screen

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.domain.model.Group
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
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

    var showCopyToast by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Deep Link URL 생성
    val deepLinkUrl = "https://madcamp.app/invite/${group.id}"

    LaunchedEffect(Unit) {
        viewModel.initializeWithGroup(group)
    }

    // QR 코드 생성 (항상 Deep Link URL 기반)
    LaunchedEffect(key1 = deepLinkUrl) {
         scope.launch {
            qrBitmap = QRCodeGenerator.generateQRCode(
                text = deepLinkUrl,
                width = 512,
                height = 512
            )
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
            .background(Color(0xFFF9F9F9))
            .statusBarsPadding()
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
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackPress,
                            modifier = Modifier.offset(x = (-12).dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Share Button in Header
                        IconButton(
                            onClick = {
                                shareMore(context, deepLinkUrl, group.name)
                            },
                             modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "그룹 초대하기",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "친구들과 함께 즐거운 시간을 보내세요! \uD83D\uDC8C",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // --- Main Content ---
            Column(
                 modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Group Info
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                     Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = group.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "현재 멤버 ${memberCount}명",
                            fontSize = 15.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // 2. QR Code
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        QRCodeContainer(
                            qrCodeBitmap = qrBitmap,
                            inviteUrl = deepLinkUrl,
                            expiryTime = uiState.expiryTime
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "QR코드를 스캔하여 입장하세요",
                            fontSize = 14.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }
                
                // 3. Link Action
                Button(
                    onClick = {
                        copyToClipboard(context, deepLinkUrl)
                        showCopyToast = true
                        scope.launch {
                             delay(2000)
                             showCopyToast = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9945)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "초대 링크 복사하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Copy Toast
        if (showCopyToast) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "링크가 클립보드에 복사되었습니다.",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
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

private fun shareMore(context: Context, inviteUrl: String, groupName: String) {
    val message = "「$groupName」 그룹에 초대합니다!\n링크를 클릭하여 바로 참여하세요 \uD83D\uDC47\n\n$inviteUrl"
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(intent, "그룹 초대하기"))
}
