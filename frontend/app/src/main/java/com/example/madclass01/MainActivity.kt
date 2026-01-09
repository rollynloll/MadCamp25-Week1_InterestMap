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
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Login) }
    var userToken by remember { mutableStateOf<String?>(null) }
    
    when (currentScreen) {
        AppScreen.Login -> {
            LoginScreen(
                onLoginSuccess = { token ->
                    userToken = token
                    currentScreen = AppScreen.ProfileSetup
                }
            )
        }
        AppScreen.ProfileSetup -> {
            ProfileSetupScreen(
                onProfileComplete = { nickname, age, region, images ->
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
                onLoadingComplete = {
                    currentScreen = AppScreen.TagSelection
                }
            )
        }
        AppScreen.TagSelection -> {
            TagSelectionScreen(
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
