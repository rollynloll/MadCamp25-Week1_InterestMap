package com.example.madclass01.presentation.login.screen

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.R
import com.example.madclass01.presentation.login.model.LoginSource
import com.example.madclass01.presentation.login.viewmodel.LoginViewModel
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: (userId: String, nickname: String, source: LoginSource, isProfileComplete: Boolean, age: Int?, gender: String?, region: String?, bio: String?, tags: List<String>, photoInterests: List<String>) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Gradient Configuration
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFF9945), // Original Orange
            Color(0xFFFFB775)  // Lighter Orange
        )
    )
    
    val kakaoYellow = Color(0xFFFEE500)

    // 로그인 성공 시 처리
    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess && uiState.userId != null) {
            onLoginSuccess(
                uiState.userId!!,
                uiState.nickname ?: "",
                uiState.loginSource,
                uiState.isProfileComplete,
                uiState.profileAge,
                uiState.profileGender,
                uiState.profileRegion,
                uiState.profileBio,
                uiState.profileTags,
                uiState.profilePhotoInterests
            )
            viewModel.resetLoginState()
        }
    }
    
    // 카카오 로그인 콜백
    val kakaoCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e("KakaoLogin", "로그인 실패", error)
            viewModel.setLoginError("카카오 로그인 실패: ${error.message}")
        } else if (token != null) {
            Log.d("KakaoLogin", "로그인 성공, 토큰: ${token.accessToken}")

            // 사용자 정보 요청 (카카오 userId/nickname 확보)
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e("KakaoLogin", "사용자 정보 요청 실패", error)
                    viewModel.setLoginError("사용자 정보 요청 실패: ${error.message}")
                } else if (user != null) {
                    Log.d("KakaoLogin", "사용자 정보: ${user.id}, ${user.kakaoAccount?.profile?.nickname}")
                    
                    // 백엔드에 사용자 등록
                    viewModel.handleKakaoLoginSuccess(
                        kakaoAccessToken = token.accessToken,
                        kakaoUserId = user.id.toString(),
                        nickname = user.kakaoAccount?.profile?.nickname,
                        profileImageUrl = user.kakaoAccount?.profile?.profileImageUrl
                    )
                } else {
                    viewModel.setLoginError("사용자 정보가 비어있습니다")
                }
            }
        }
    }
    
    // 카카오 로그인 실행 함수
    fun startKakaoLogin() {
        val talkAvailable = UserApiClient.instance.isKakaoTalkLoginAvailable(context)
        Log.d("KakaoLogin", "startKakaoLogin: isKakaoTalkLoginAvailable=$talkAvailable")

        if (talkAvailable) {
            val host = activity
            if (host == null) {
                UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoCallback)
                return
            }

            UserApiClient.instance.loginWithKakaoTalk(host) { token, error ->
                if (error != null) {
                    Log.e("KakaoLogin", "카카오톡 로그인 실패", error)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoCallback)
                } else if (token != null) {
                    kakaoCallback(token, null)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoCallback)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush) // Apply Gradient
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // --- Top Branding Section ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Logo Container (Idea)
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glow/shadow
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .shadow(
                                elevation = 24.dp,
                                shape = CircleShape,
                                spotColor = Color.Black.copy(alpha = 0.4f),
                                ambientColor = Color.Black.copy(alpha = 0.4f)
                            )
                    )

                    Image(
                        painter = painterResource(R.drawable.omo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            .background(Color.White) // Fallback background if image is transparent
                    )
                }
                
                Spacer(Modifier.height(32.dp))
                
                Text(
                    text = stringResource(R.string.login_title),
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(16.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.login_subtitle1),
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.login_subtitle2),
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = stringResource(R.string.login_caption),
                    color = Color(0xFFFFF3E0),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // --- Bottom Action Sheet ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Kakao Login Button
                    Button(
                        onClick = { startKakaoLogin() },
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = kakaoYellow,
                            disabledContainerColor = kakaoYellow.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            // Assuming kakao symbol is not available as vector, reusing app icon or text only
                            // For a real app, use the official Kakao symbol
                             Icon(
                                painter = painterResource(R.drawable.omo), // Placeholder or Kakao Icon
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.kakao_login_button),
                                color = Color(0xFF191919), // Kakao Black
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Test Login Button
                    OutlinedButton(
                        onClick = {
                            viewModel.loginOffline(
                                userId = "local_test_${System.currentTimeMillis()}",
                                nickname = "테스트유저"
                            )
                        },
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF666666)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "테스트 계정으로 시작하기",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    if (uiState.loginErrorMessage.isNotEmpty()) {
                        Text(
                            text = uiState.loginErrorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.login_terms),
                        color = Color(0xFF999999),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                         modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
