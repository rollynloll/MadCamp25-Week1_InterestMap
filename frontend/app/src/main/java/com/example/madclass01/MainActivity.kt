package com.example.madclass01

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.madclass01.presentation.login.screen.LoginScreen
import com.example.madclass01.presentation.login.model.LoginSource
import com.example.madclass01.presentation.profile.screen.ProfileSetupScreen
import com.example.madclass01.presentation.profile.screen.LoadingScreen
import com.example.madclass01.presentation.profile.screen.ProfileEditScreen
import com.example.madclass01.presentation.profile.screen.TagSelectionScreen
import com.example.madclass01.presentation.profile.ProfileScreen
import com.example.madclass01.presentation.test.ApiTestScreen
import com.example.madclass01.presentation.chat.ChatScreen
import com.example.madclass01.presentation.group.screen.CreateGroupScreen
import com.example.madclass01.presentation.group.screen.GroupDetailScreen
import com.example.madclass01.presentation.group.screen.QRInviteScreen
import com.example.madclass01.presentation.group.screen.QRScannerScreen
import com.example.madclass01.core.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var tokenManager: TokenManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Deep Link 처리
        val deepLinkData = handleDeepLink(intent)
        
        setContent {
            TasteMapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        initialDeepLink = deepLinkData,
                        tokenManager = tokenManager
                    )
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        // 앱이 이미 실행 중일 때 Deep Link 처리
        handleDeepLink(intent)?.let { _ ->
            Toast.makeText(this, "그룹 초대 링크를 처리합니다...", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun handleDeepLink(intent: Intent?): DeepLinkData? {
        val data: Uri? = intent?.data
        if (data != null) {
            // madcamp://invite/{groupId} 또는 https://madcamp.app/invite/{groupId}
            if ((data.scheme == "madcamp" || data.scheme == "https") && 
                (data.host == "invite" || data.path?.startsWith("/invite") == true)) {
                
                val groupId = data.lastPathSegment ?: data.getQueryParameter("groupId")
                val inviteCode = data.getQueryParameter("code")
                
                if (groupId != null || inviteCode != null) {
                    return DeepLinkData(
                        type = DeepLinkType.GROUP_INVITE,
                        groupId = groupId,
                        inviteCode = inviteCode
                    )
                }
            }
        }
        return null
    }
}

data class DeepLinkData(
    val type: DeepLinkType,
    val groupId: String? = null,
    val inviteCode: String? = null
)

enum class DeepLinkType {
    GROUP_INVITE
}

enum class ProfileFlowEntry {
    Login,
    Edit
}

@Composable
fun AppNavigation(
    initialDeepLink: DeepLinkData? = null,
    tokenManager: TokenManager
) {
    // 🧪 테스트 모드: true로 설정하면 API 테스트 화면으로 시작
    val isTestMode = false  // 테스트 완료!
    val context = LocalContext.current

    var currentScreen by remember { mutableStateOf<AppScreen>(
        if (isTestMode) AppScreen.ApiTest else AppScreen.Login
    ) }
    var userId by remember { mutableStateOf<String?>(null) }
    var userNickname by remember { mutableStateOf<String?>(null) }
    var userAge by remember { mutableStateOf<Int?>(null) }
    var userGender by remember { mutableStateOf<String?>(null) }
    var profileRefreshTrigger by remember { mutableStateOf(0) }  // 프로필 새로고침 트리거  // 백엔드에서 받아옴
    var userRegion by remember { mutableStateOf<String?>(null) }
    var userBio by remember { mutableStateOf<String>("") }
    var userImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var recommendedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var userTags by remember { mutableStateOf<List<String>>(emptyList()) }  // 사용자가 선택한 관심사
    var userPhotoInterests by remember { mutableStateOf<List<String>>(emptyList()) }  // 사진에서 추출한 관심사
    var profileFlowEntry by remember { mutableStateOf(ProfileFlowEntry.Login) }
    var homeStartTabRoute by remember { mutableStateOf("groups") }
    
    // Deep Link 처리
    LaunchedEffect(initialDeepLink) {
        initialDeepLink?.let { deepLink ->
            when (deepLink.type) {
                DeepLinkType.GROUP_INVITE -> {
                    if (userId != null) {
                        // 로그인된 상태면 바로 그룹 상세로 이동
                        deepLink.groupId?.let { groupId ->
                            currentScreen = AppScreen.GroupDetail(groupId)
                            Toast.makeText(context, "그룹으로 이동합니다...", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 로그인 안 된 상태면 로그인 후 처리하도록 대기
                        Toast.makeText(context, "먼저 로그인해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    when (currentScreen) {
        AppScreen.ApiTest -> {
            ApiTestScreen()
        }
        AppScreen.Login -> {
            LoginScreen(
                onLoginSuccess = { id, nickname, source, isProfileComplete, age, gender, region, bio, tags, photoInterests ->
                    println("MainActivity: Login Success - Tags: $tags, PhotoInterests: $photoInterests")
                    userId = id
                    tokenManager.saveUserId(id) // TokenManager에 userId 저장
                    userNickname = nickname
                    if (source == LoginSource.Test) {
                        // 프로필 목업 값
                        userAge = 20
                        userGender = "male"
                        userRegion = "서울"
                        userBio = ""
                        userImages = emptyList()
                        recommendedTags = emptyList()
                        userTags = emptyList()
                        userPhotoInterests = emptyList()
                        homeStartTabRoute = "groups"
                        currentScreen = AppScreen.Home
                    } else if (isProfileComplete) {
                        // 이미 프로필이 등록된 유저는 스킵
                        userAge = age
                        userGender = gender
                        userRegion = region
                        userBio = bio ?: ""
                        userTags = tags
                        userPhotoInterests = photoInterests
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
                onProfileComplete = { nickname, age, region, bio, images, tags, interests ->
                    userNickname = nickname
                    userAge = age
                    userRegion = region
                    userBio = bio
                    userImages = images
                    recommendedTags = tags
                    userTags = interests // Step 1에서 선택한 태그를 userTags에 저장
                    println("Step 1 완료: $nickname, $age, $region, ${images.size}개 이미지")
                    println("Step 1 관심사: $interests")
                    currentScreen = AppScreen.TagSelection
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
                bio = userBio,
                recommendedTags = recommendedTags,
                initialCustomTags = userTags,
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
                userId = userId ?: "",
                initialProfileImage = userImages.firstOrNull(),
                initialNickname = userNickname ?: "",
                initialAge = userAge,
                initialRegion = userRegion,
                initialBio = userBio,
                initialImages = userImages,
                initialTags = userTags,
                initialPhotoInterests = userPhotoInterests,
                onBack = {
                    homeStartTabRoute = "profile"
                    currentScreen = AppScreen.Home
                },
                onSave = { profileImage, nickname, age, region, bio, images, tags ->
                    // API 업데이트가 성공했으므로, 로컬 상태도 업데이트
                    userNickname = nickname
                    userAge = age
                    userRegion = region
                    userBio = bio
                    userImages = if (profileImage != null) {
                        listOf(profileImage) + images.filter { it != profileImage }
                    } else {
                        images
                    }
                    userTags = tags
                    // Profile 탭으로 이동하고 새로고침 트리거 증가
                    profileRefreshTrigger++  // 이 값이 변경되면 ProfileScreen이 다시 마운트되어 최신 데이터 로드
                    homeStartTabRoute = "profile"
                    currentScreen = AppScreen.Home
                }
            )
        }
        is AppScreen.UserProfile -> {
            val userProfile = currentScreen as AppScreen.UserProfile
            ProfileScreen(
                userId = userProfile.userId,
                onBack = {
                    if (userProfile.fromGroupId != null) {
                        currentScreen = AppScreen.GroupDetail(userProfile.fromGroupId)
                    } else {
                        currentScreen = AppScreen.Home
                    }
                },
                onEditClick = null
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
                },
                onQRCodeClick = { group ->
                    currentScreen = AppScreen.QRInvite(group.id, group.name, group.memberCount)
                },
                onProfileClick = { targetUserId ->
                    currentScreen = AppScreen.UserProfile(targetUserId, fromGroupId = groupDetail.groupId)
                },
                onChatRoomCreated = { chatRoomId, groupName, memberCount ->
                    currentScreen = AppScreen.Chat(chatRoomId, groupName, memberCount)
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
        is AppScreen.Chat -> {
            val chat = currentScreen as AppScreen.Chat
            ChatScreen(
                chatRoomId = chat.chatRoomId,
                chatRoomName = chat.chatRoomName,
                memberCount = chat.memberCount,
                userId = userId ?: "",
                onBackPress = {
                    currentScreen = AppScreen.Home
                }
            )
        }
        AppScreen.QRScanner -> {
            QRScannerScreen(
                userId = userId ?: "",
                onBackPress = {
                    currentScreen = AppScreen.Home
                },
                onScanSuccess = { groupId ->
                    currentScreen = AppScreen.GroupDetail(groupId)
                }
            )
        }
        is AppScreen.QRInvite -> {
            val qrInvite = currentScreen as AppScreen.QRInvite
            // GroupDetailScreen에서 전달받은 그룹 정보로 객체 생성
            val group = com.example.madclass01.domain.model.Group(
                id = qrInvite.groupId,
                name = qrInvite.groupName,
                description = ""
            )
            QRInviteScreen(
                group = group,
                memberCount = qrInvite.memberCount,
                userId = userId ?: "mock_user",
                onBackPress = {
                    currentScreen = AppScreen.GroupDetail(qrInvite.groupId)
                }
            )
        }
        AppScreen.Home -> {
            com.example.madclass01.presentation.main.MainScreen(
                userId = userId,  // userId 전달
                startTabRoute = homeStartTabRoute,
                profileNickname = userNickname,
                profileAge = userAge,
                profileGender = userGender,
                profileRegion = userRegion,
                profileBio = userBio,
                profileImages = userImages,
                profileTags = userTags,
                profileRefreshTrigger = profileRefreshTrigger,  // 새로고침 트리거 전달
                onNavigateToGroupDetail = { groupId ->
                    currentScreen = AppScreen.GroupDetail(groupId)
                },
                onNavigateToCreateGroup = {
                    currentScreen = AppScreen.CreateGroup
                },
                onNavigateToEditProfile = {
                    homeStartTabRoute = "profile"
                    currentScreen = AppScreen.ProfileEdit
                },
                onNavigateToQRScanner = {
                    currentScreen = AppScreen.QRScanner
                },
                onProfileLoaded = { nickname, age, gender, region, bio, images, interests, photoInterests ->
                    userNickname = nickname
                    userAge = age
                    userGender = gender
                    userRegion = region
                    userBio = bio ?: ""
                    userImages = images
                    userTags = interests
                    userPhotoInterests = photoInterests
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
    data class UserProfile(val userId: String, val fromGroupId: String? = null) : AppScreen()
    object CreateGroup : AppScreen()
    data class Chat(val chatRoomId: String, val chatRoomName: String = "채팅", val memberCount: Int = 0) : AppScreen()
    object QRScanner : AppScreen()
    data class QRInvite(val groupId: String, val groupName: String, val memberCount: Int = 0) : AppScreen()
    object Home : AppScreen()
}

@Composable
fun TasteMapTheme(content: @Composable () -> Unit) = MaterialTheme(
    content = content
)
