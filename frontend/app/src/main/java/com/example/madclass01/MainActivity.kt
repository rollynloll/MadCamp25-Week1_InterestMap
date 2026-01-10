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
import com.example.madclass01.presentation.profile.screen.ProfileEditScreen
import com.example.madclass01.presentation.profile.screen.TagSelectionScreen
import com.example.madclass01.presentation.test.ApiTestScreen
import com.example.madclass01.presentation.group.screen.CreateGroupScreen
import com.example.madclass01.presentation.group.screen.GroupDetailScreen
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

enum class ProfileFlowEntry {
    Login,
    Edit
}

@Composable
fun AppNavigation() {
    // 🧪 테스트 모드: true로 설정하면 API 테스트 화면으로 시작
    val isTestMode = false  // 테스트 완료!

    // 임시: 프로필 입력/업로드 단계 스킵
    val skipProfileSetupForNow = true


    var currentScreen by remember { mutableStateOf<AppScreen>(
        if (isTestMode) AppScreen.ApiTest else AppScreen.Login
    ) }
    var userId by remember { mutableStateOf<String?>(null) }
    var userNickname by remember { mutableStateOf<String?>(null) }
    var userAge by remember { mutableStateOf<Int?>(null) }
    var userRegion by remember { mutableStateOf<String?>(null) }
    var userBio by remember { mutableStateOf<String>("") }
    var userImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var recommendedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var userTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var profileFlowEntry by remember { mutableStateOf(ProfileFlowEntry.Login) }
    var homeStartTabRoute by remember { mutableStateOf("groups") }

    when (currentScreen) {
        AppScreen.ApiTest -> {
            ApiTestScreen()
        }
        AppScreen.Login -> {
            LoginScreen(
                onLoginSuccess = { id, nickname ->
                    userId = id
                    userNickname = nickname
                    if (skipProfileSetupForNow) {
                        // 프로필 목업 값
                        userAge = 20
                        userRegion = "서울"
                        userBio = ""
                        userImages = emptyList()
                        recommendedTags = emptyList()
                        userTags = emptyList()
                        homeStartTabRoute = "groups"
                        currentScreen = AppScreen.Home
                    } else {
                        profileFlowEntry = ProfileFlowEntry.Login
                        currentScreen = AppScreen.ProfileSetup
                    }
                }
            )
        }
        AppScreen.ProfileSetup -> {
            ProfileSetupScreen(
                userId = userId,  // userId 전달
                isEditMode = profileFlowEntry == ProfileFlowEntry.Edit,
                onBack = {
                    if (profileFlowEntry == ProfileFlowEntry.Edit) {
                        homeStartTabRoute = "profile"
                        currentScreen = AppScreen.Home
                    } else {
                        currentScreen = AppScreen.Login
                    }
                },
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
                onBack = {
                    currentScreen = AppScreen.ProfileSetup
                },
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
                onComplete = { tags ->
                    userTags = tags
                    homeStartTabRoute = if (profileFlowEntry == ProfileFlowEntry.Edit) "profile" else "groups"
                    profileFlowEntry = ProfileFlowEntry.Login
                    currentScreen = AppScreen.Home
                }
            )
        }
        AppScreen.ProfileEdit -> {
            ProfileEditScreen(
                initialNickname = userNickname ?: "",
                initialAge = userAge,
                initialRegion = userRegion,
                initialBio = userBio,
                initialImages = userImages,
                onBack = {
                    homeStartTabRoute = "profile"
                    currentScreen = AppScreen.Home
                },
                onSave = { nickname, age, region, bio, images ->
                    userNickname = nickname
                    userAge = age
                    userRegion = region
                    userBio = bio
                    userImages = images
                    homeStartTabRoute = "profile"
                    currentScreen = AppScreen.Home
                }
            )
        }
        is AppScreen.GroupDetail -> {
            val groupDetail = currentScreen as AppScreen.GroupDetail
            GroupDetailScreen(
                groupId = groupDetail.groupId,
                currentUserId = userId ?: "mock_user",
                onBackPress = {
                    homeStartTabRoute = "groups"
                    currentScreen = AppScreen.Home
                }
            )
        }
        AppScreen.CreateGroup -> {
            CreateGroupScreen(
                userId = userId ?: "mock_user",
                onBackPress = {
                    homeStartTabRoute = "groups"
                    currentScreen = AppScreen.Home
                },
                onCreateSuccess = {
                    homeStartTabRoute = "groups"
                    currentScreen = AppScreen.Home
                }
            )
        }
        AppScreen.Home -> {
            com.example.madclass01.presentation.main.MainScreen(
                userId = userId,  // userId 전달
                startTabRoute = homeStartTabRoute,
                profileNickname = userNickname,
                profileAge = userAge,
                profileRegion = userRegion,
                profileBio = userBio,
                profileImages = userImages,
                profileTags = userTags,
                onNavigateToGroupDetail = { groupId ->
                    currentScreen = AppScreen.GroupDetail(groupId)
                },
                onNavigateToCreateGroup = {
                    currentScreen = AppScreen.CreateGroup
                },
                onNavigateToEditProfile = {
                    homeStartTabRoute = "profile"
                    currentScreen = AppScreen.ProfileEdit
                }
            )
        }
    }
}

sealed class AppScreen {
    object ApiTest : AppScreen()  // 🧪 테스트 화면
    object Login : AppScreen()
    object ProfileSetup : AppScreen()
    object ProfileEdit : AppScreen()
    object Loading : AppScreen()
    object TagSelection : AppScreen()
    data class GroupDetail(val groupId: String) : AppScreen()
    object CreateGroup : AppScreen()
    object Home : AppScreen()
}

@Composable
fun TasteMapTheme(content: @Composable () -> Unit) = MaterialTheme(
    content = content
)
