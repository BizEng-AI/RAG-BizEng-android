package com.example.myapplication.uiPack.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.uiPack.chat.ChatScreen
import com.example.myapplication.uiPack.chat.ChatVm
import com.example.myapplication.uiPack.roleplay.RoleplayScreen
import com.example.myapplication.uiPack.roleplay.RoleplayVm
import com.example.myapplication.uiPack.pronunciation.PronunciationScreen
import com.example.myapplication.uiPack.pronunciation.PronunciationVm

sealed class NavDestination(val route: String, val title: String, val icon: ImageVector) {
    object Chat : NavDestination("chat", "Chat", Icons.Filled.Chat)
    object Roleplay : NavDestination("roleplay", "Roleplay", Icons.Filled.School)
    object Pronunciation : NavDestination("pronunciation", "Pronunciation", Icons.Filled.RecordVoiceOver)
}

@Composable
fun MainNavigation() {
    var selectedTab by remember { mutableStateOf<NavDestination>(NavDestination.Chat) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == NavDestination.Chat,
                    onClick = { selectedTab = NavDestination.Chat },
                    icon = { Icon(NavDestination.Chat.icon, contentDescription = NavDestination.Chat.title) },
                    label = { Text(NavDestination.Chat.title) }
                )
                NavigationBarItem(
                    selected = selectedTab == NavDestination.Roleplay,
                    onClick = { selectedTab = NavDestination.Roleplay },
                    icon = { Icon(NavDestination.Roleplay.icon, contentDescription = NavDestination.Roleplay.title) },
                    label = { Text(NavDestination.Roleplay.title) }
                )
                NavigationBarItem(
                    selected = selectedTab == NavDestination.Pronunciation,
                    onClick = { selectedTab = NavDestination.Pronunciation },
                    icon = { Icon(NavDestination.Pronunciation.icon, contentDescription = NavDestination.Pronunciation.title) },
                    label = { Text(NavDestination.Pronunciation.title) }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedTab) {
                NavDestination.Chat -> {
                    val vm: ChatVm = hiltViewModel()
                    ChatScreen(vm = vm)
                }
                NavDestination.Roleplay -> {
                    val vm: RoleplayVm = hiltViewModel()
                    RoleplayScreen(vm = vm)
                }
                NavDestination.Pronunciation -> {
                    val vm: PronunciationVm = hiltViewModel()
                    PronunciationScreen(vm = vm)
                }
            }
        }
    }
}

// Placeholder icon if School doesn't exist
private val Icons.Filled.School get() = Icons.Filled.AccountCircle

