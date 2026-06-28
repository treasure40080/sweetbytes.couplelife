package com.sweetbytes.couplelife

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("🏠 我們的家\n(這裡之後要放客廳插畫與伴侶狀態)", fontSize = 20.sp)
    }
}

@Composable
fun EntryScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("💰 雙人記帳流水\n(這裡之後要放記帳表單與列表)", fontSize = 20.sp)
    }
}

@Composable
fun ChartScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("📈 財務統計圖表\n(這裡之後要用 Canvas 畫圓餅圖)", fontSize = 20.sp)
    }
}

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("⚙️ 專屬設定頁\n(這裡之後要放伴侶連結邀請碼)", fontSize = 20.sp)
    }
}