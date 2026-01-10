package com.example.madclass01.presentation.login.screen

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madclass01.R
import com.example.madclass01.presentation.login.viewmodel.LoginViewModel
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: (userId: String, nickname: String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val kakaoYellow = Color(0xFFFEE500)
    val omoOrange = Color(0xFFFF8A3D)
    
    // 로그인 성공 시 처리
    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess && uiState.userId != null) {
            onLoginSuccess(uiState.userId!!, uiState.nickname ?: "")
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
        // 카카오톡 설치/로그인 가능 여부 확인
        val talkAvailable = UserApiClient.instance.isKakaoTalkLoginAvailable(context)
        Log.d(
            "KakaoLogin",
            "startKakaoLogin: isKakaoTalkLoginAvailable=$talkAvailable, activity=${activity != null}"
        )

        if (talkAvailable) {
            // 카카오톡으로 로그인 (Activity context 필요)
            val host = activity
            if (host == null) {
                Log.w("KakaoLogin", "카카오톡 로그인을 위한 Activity 컨텍스트를 가져올 수 없어 웹 로그인으로 대체합니다")
                UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoCallback)
                return
            }

            UserApiClient.instance.loginWithKakaoTalk(host) { token, error ->
                if (error != null) {
                    Log.e("KakaoLogin", "카카오톡 로그인 실패", error)
                    
                    // 사용자가 카카오톡 로그인을 취소한 경우
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    
                    // 카카오톡 로그인 실패 시 카카오 계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoCallback)
                } else if (token != null) {
                    kakaoCallback(token, null)
                }
            }
        } else {
            // 카카오톡 미설치: 카카오 계정으로 로그인
            UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoCallback)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(omoOrange)
    ) {
        // 상단 로고/타이틀
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(170.dp),
                shape = CircleShape,
                color = Color(0xFFFFB777)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(R.drawable.omo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp)
                            .background(Color.Transparent, CircleShape)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = LocalContext.current.getString(R.string.login_title),
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = LocalContext.current.getString(R.string.login_subtitle1),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = LocalContext.current.getString(R.string.login_subtitle2),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = LocalContext.current.getString(R.string.login_caption),
                color = Color(0xFFFEF6E8),
                fontSize = 16.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )
        }

        // 하단 컨테이너 + 카카오 버튼
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(220.dp),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        // 실제 카카오톡 로그인 실행
                        startKakaoLogin()
                    },
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = kakaoYellow),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black
                        )
                    } else {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.omo),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = LocalContext.current.getString(R.string.kakao_login_button),
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 테스트 로그인 버튼 (임시)
                OutlinedButton(
                    onClick = {
                        // 임시: 서버 연동 없이 프론트에서 바로 로그인
                        viewModel.loginOffline(
                            userId = "local_test_${System.currentTimeMillis()}",
                            nickname = "테스트유저"
                        )
                    },
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "테스트로 로그인",
                        color = Color(0xFF111827),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // 에러 메시지 표시
                if (uiState.loginErrorMessage.isNotEmpty()) {
                    Text(
                        text = uiState.loginErrorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = LocalContext.current.getString(R.string.login_terms),
                    color = Color(0xFFB0B0B0),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
