package com.example.madclass01.presentation.group.screen

import android.Manifest
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    
    // Handle system back press
    androidx.activity.compose.BackHandler(onBack = onBackPress)
    
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
                    barcodeView.value?.pause()
                }
            }
            
            AndroidView(
                factory = { context ->
                    DecoratedBarcodeView(context).apply {
                        val formats = listOf(BarcodeFormat.QR_CODE)
                        getBarcodeView().decoderFactory = DefaultDecoderFactory(formats)
                        initializeFromIntent(Intent())
                        setStatusText("") // 기본 텍스트 제거
                        
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
            
            // 어두운 오버레이 및 스캔 가이드
            QRScannerOverlay()

            // 상단 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPress,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.3f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로 가기"
                    )
                }
            }
            
            // 하단 텍스트 가이드
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "QR 코드를 사각형 안에 맞춰주세요",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }
            
            // 로딩 표시
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
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "그룹 정보를 불러오는 중...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // 에러 메시지
            if (uiState.errorMessage.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚠️", // 간단한 아이콘
                                fontSize = 16.sp
                            )
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QRScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_line")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_line_animation"
    )

    val density = LocalDensity.current
    val scanSize = 260.dp
    val scanSizePx = with(density) { scanSize.toPx() }
    val cornerRadius = with(density) { 20.dp.toPx() }
    val strokeWidth = with(density) { 4.dp.toPx() }
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val left = (width - scanSizePx) / 2
        val top = (height - scanSizePx) / 2
        val right = left + scanSizePx
        val bottom = top + scanSizePx

        // 1. 전체 어두운 배경 (구멍 뚫기)
        // Layer를 사용하여 BlendMode.Clear를 적용
        with(drawContext.canvas.nativeCanvas) {
            val checkPoint = saveLayer(null, null)
            
            // 전체 화면 반투명 검정
            drawRect(
                color = Color.Black.copy(alpha = 0.6f),
                size = size
            )

            // 중앙 구멍 (Clear 모드)
            drawRoundRect(
                topLeft = Offset(left, top),
                size = Size(scanSizePx, scanSizePx),
                cornerRadius = CornerRadius(cornerRadius),
                color = Color.Transparent,
                blendMode = BlendMode.Clear
            )
            
            restoreToCount(checkPoint)
        }

        // 2. 모서리 가이드 (흰색 테두리)
        val lineLength = 40.dp.toPx()
        val capColor = Color.White
        
        // Top Left
        drawLine(capColor, Offset(left, top), Offset(left + lineLength, top), strokeWidth)
        drawLine(capColor, Offset(left, top), Offset(left, top + lineLength), strokeWidth)
        
        // Top Right
        drawLine(capColor, Offset(right, top), Offset(right - lineLength, top), strokeWidth)
        drawLine(capColor, Offset(right, top), Offset(right, top + lineLength), strokeWidth)
        
        // Bottom Left
        drawLine(capColor, Offset(left, bottom), Offset(left + lineLength, bottom), strokeWidth)
        drawLine(capColor, Offset(left, bottom), Offset(left, bottom - lineLength), strokeWidth)
        
        // Bottom Right
        drawLine(capColor, Offset(right, bottom), Offset(right - lineLength, bottom), strokeWidth)
        drawLine(capColor, Offset(right, bottom), Offset(right, bottom - lineLength), strokeWidth)

        // 3. 스캔 라인 애니메이션
        val lineY = top + (scanSizePx * scanLineY)
        drawLine(
            color = primaryColor.copy(alpha = 0.8f),
            start = Offset(left + 20f, lineY),
            end = Offset(right - 20f, lineY),
            strokeWidth = 2.dp.toPx()
        )
        
        // 스캔 라인 그라데이션 효과 (선 아래로 살짝 퍼지는 빛)
        drawRect(
            color = primaryColor.copy(alpha = 0.2f),
            topLeft = Offset(left + 20f, lineY),
            size = Size(scanSizePx - 40f, 40f) // 아래로 40px 정도 빛 번짐
        )
    }
}
