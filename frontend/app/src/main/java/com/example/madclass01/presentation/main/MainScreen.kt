package com.example.madclass01.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.madclass01.presentation.group.GroupListScreen
import com.example.madclass01.presentation.profile.ProfileScreen
import com.example.madclass01.presentation.search.SearchScreen

sealed class MainTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Groups : MainTab(
        route = "groups",
        title = "그룹",
        selectedIcon = Icons.Filled.Group,
        unselectedIcon = Icons.Outlined.Group
    )
    
    object Search : MainTab(
        route = "search",
        title = "찾기",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    )
    
    object Profile : MainTab(
        route = "profile",
        title = "프로필",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}

@Composable
fun MainScreen(
    userId: String? = null,  // userId 추가
    startTabRoute: String = MainTab.Groups.route,
    profileNickname: String? = null,
    profileAge: Int? = null,
    profileRegion: String? = null,
    profileBio: String? = null,
    profileImages: List<String> = emptyList(),
    profileTags: List<String> = emptyList(),
    onNavigateToGroupDetail: (String) -> Unit = {},
    onNavigateToCreateGroup: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {}
) {
    val navController = rememberNavController()
    val tabs = listOf(MainTab.Groups, MainTab.Search, MainTab.Profile)
    
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFFFF9945)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                tabs.forEach { tab ->
                    val isSelected = currentRoute == tab.route
                    
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFF9945),
                            selectedTextColor = Color(0xFFFF9945),
                            unselectedIconColor = Color(0xFF999999),
                            unselectedTextColor = Color(0xFF999999),
                            indicatorColor = Color(0xFFFFF3E0)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startTabRoute,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(MainTab.Groups.route) {
                GroupListScreen(
                    userId = userId,  // userId 전달
                    onGroupClick = onNavigateToGroupDetail,
                    onCreateGroupClick = onNavigateToCreateGroup
                )
            }
            
            composable(MainTab.Search.route) {
                SearchScreen(
                    onGroupClick = onNavigateToGroupDetail
                )
            }
            
            composable(MainTab.Profile.route) {
                ProfileScreen(
                    userId = userId,  // userId 전달
                    nickname = profileNickname,
                    age = profileAge,
                    region = profileRegion,
                    bio = profileBio,
                    images = profileImages,
                    tags = profileTags,
                    onEditClick = onNavigateToEditProfile
                )
            }
        }
    }
}
