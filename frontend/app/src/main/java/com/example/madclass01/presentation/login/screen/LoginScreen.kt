package com.example.madclass01.presentation.login.screen

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

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: (token: String) -> Unit = {}
) {
    val kakaoYellow = Color(0xFFFEE500)
    val omoOrange = Color(0xFFFF8A3D)

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                        // 임시 동작: 카카오 로그인 없이 바로 프로필 생성 흐름으로 이동
                        onLoginSuccess("dummy-token")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = kakaoYellow),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
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
