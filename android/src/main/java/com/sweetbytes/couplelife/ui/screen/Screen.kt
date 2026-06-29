package com.sweetbytes.couplelife.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "家", Icons.Outlined.Cottage)
    object Entry : Screen("entry", "記帳", Icons.Outlined.ReceiptLong)
    object Chart : Screen("chart", "統計", Icons.Outlined.PieChart)
    object Settings : Screen("settings", "設定", Icons.Outlined.Tune)
}