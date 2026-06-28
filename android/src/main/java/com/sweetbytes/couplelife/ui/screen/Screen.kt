package com.sweetbytes.couplelife.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "我們的家", Icons.Default.Home)
    object Entry : Screen("entry", "記帳流水", Icons.Default.Email)
    object Chart : Screen("chart", "統計圖表", Icons.Default.MoreVert)
    object Settings : Screen("settings", "專屬設定", Icons.Default.Settings)
}