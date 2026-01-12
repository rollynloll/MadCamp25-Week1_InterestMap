package com.example.madclass01.presentation.group.screen

import android.Manifest
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.madclass01.presentation.group.viewmodel.QRInviteViewModel
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.google.zxing.BarcodeFormat

@Composable
fun QRScannerScreen(
    userId: String,
    onBackPress: () -> Unit = {},
    onScanSuccess: (groupId: String) -> Unit = {},
    viewModel: QRInviteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    var hasCameraPermission by remember { mutableStateOf(false) }
    var scannedUrl by remember { mutableStateOf<String?>(null) }
    
    // 카메라 권한 요청
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            onBackPress()
        }
    }
    
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    // 스캔 성공 시 그룹 참여
    LaunchedEffect(scannedUrl) {
        if (scannedUrl != null) {
            viewModel.joinGroupByLink(scannedUrl!!, userId)
        }
    }
    
    // 참여 성공 시 화면 이동
    LaunchedEffect(uiState.joinSuccess) {
        if (uiState.joinSuccess) {
            Toast.makeText(context, "그룹에 참여했습니다!", Toast.LENGTH_SHORT).show()
            viewModel.resetJoinSuccess()
            onScanSuccess(uiState.joinGroupId ?: "")
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // QR 스캐너 카메라 뷰
            val barcodeView = remember { mutableStateOf<DecoratedBarcodeView?>(null) }
            val lifecycleOwner = LocalLifecycleOwner.current
            
            // 생명주기 관찰 및 카메라 관리
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            barcodeView.value?.resume()
                        }
                        Lifecycle.Event.ON_PAUSE -> {
                            barcodeView.value?.pause()
                        }
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    // 카메라 완전히 해제
                    barcodeView.value?.pause()
                }
            }
            
            AndroidView(
                factory = { context ->
                    DecoratedBarcodeView(context).apply {
                        val formats = listOf(BarcodeFormat.QR_CODE)
                        getBarcodeView().decoderFactory = DefaultDecoderFactory(formats)
                        
                        // 빈 Intent로 기본 설정 초기화
                        initializeFromIntent(Intent())
                        
                        decodeContinuous(object : BarcodeCallback {
                            override fun barcodeResult(result: BarcodeResult?) {
                                result?.text?.let { url ->
                                    if (scannedUrl == null) {
                                        scannedUrl = url
                                        pause()
                                    }
                                }
                            }
                        })
                        
                        resume()
                    }.also { barcodeView.value = it }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // 상단 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPress
                ) {
                    Text(
                        text = "←",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Text(
                    text = "QR 코드 스캔",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.size(48.dp))
            }
            
            // 스캔 가이드
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
            ) {
                Text(
                    text = "QR 코드를 사각형 안에\n맞춰주세요",
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
            
            // 로딩/에러 표시
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Text(
                            text = "그룹 참여 중...",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            if (uiState.errorMessage.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp)
                ) {
                    Surface(
                        color = Color(0xFFEF4444),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
