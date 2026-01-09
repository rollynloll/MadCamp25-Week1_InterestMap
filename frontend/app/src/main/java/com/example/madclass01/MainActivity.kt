package com.example.madclass01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.madclass01.presentation.login.screen.LoginScreen
import com.example.madclass01.presentation.profile.screen.ProfileSetupScreen
import com.example.madclass01.presentation.profile.screen.LoadingScreen
import com.example.madclass01.presentation.profile.screen.TagSelectionScreen
import com.example.madclass01.presentation.test.ApiTestScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TasteMapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    // 🧪 테스트 모드: true로 설정하면 API 테스트 화면으로 시작
    val isTestMode = false  // 테스트 완료!

    var currentScreen by remember { mutableStateOf<AppScreen>(
        if (isTestMode) AppScreen.ApiTest else AppScreen.Login
    ) }
    var userId by remember { mutableStateOf<String?>(null) }
    var userNickname by remember { mutableStateOf<String?>(null) }
    var userAge by remember { mutableStateOf<Int?>(null) }
    var userRegion by remember { mutableStateOf<String?>(null) }
    var userImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var recommendedTags by remember { mutableStateOf<List<String>>(emptyList()) }

    when (currentScreen) {
        AppScreen.ApiTest -> {
            ApiTestScreen()
        }
        AppScreen.Login -> {
            LoginScreen(
                onLoginSuccess = { id, nickname ->
                    userId = id
                    userNickname = nickname
                    currentScreen = AppScreen.ProfileSetup
                }
            )
        }
        AppScreen.ProfileSetup -> {
            ProfileSetupScreen(
                userId = userId,  // userId 전달
                onProfileComplete = { nickname, age, region, images ->
                    userNickname = nickname
                    userAge = age
                    userRegion = region
                    userImages = images
                    println("Step 1 완료: $nickname, $age, $region, ${images.size}개 이미지")
                    currentScreen = AppScreen.Loading
                },
                onProceedToTagSelection = {
                    currentScreen = AppScreen.Loading
                }
            )
        }
        AppScreen.Loading -> {
            LoadingScreen(
                userId = userId,
                imageUrls = userImages,
                onLoadingComplete = { tags ->
                    recommendedTags = tags
                    currentScreen = AppScreen.TagSelection
                }
            )
        }
        AppScreen.TagSelection -> {
            TagSelectionScreen(
                userId = userId,
                nickname = userNickname ?: "",
                age = userAge,
                region = userRegion,
                recommendedTags = recommendedTags,
                onBack = {
                    currentScreen = AppScreen.ProfileSetup
                },
                onComplete = { count ->
                    println("Step 2 complete: $count tags selected")
                    currentScreen = AppScreen.Home
                }
            )
        }
        AppScreen.Home -> {
            com.example.madclass01.presentation.main.MainScreen(
                userId = userId,  // userId 전달
                onNavigateToGroupDetail = { groupId ->
                    println("그룹 상세 화면으로 이동: $groupId")
                },
                onNavigateToEditProfile = {
                    currentScreen = AppScreen.ProfileSetup
                }
            )
        }
    }
}

sealed class AppScreen {
    object ApiTest : AppScreen()  // 🧪 테스트 화면
    object Login : AppScreen()
    object ProfileSetup : AppScreen()
    object Loading : AppScreen()
    object TagSelection : AppScreen()
    object Home : AppScreen()
}

@Composable
fun TasteMapTheme(content: @Composable () -> Unit) = MaterialTheme(
    content = content
)
