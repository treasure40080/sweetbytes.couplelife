package com.sweetbytes.couplelife.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.sweetbytes.couplelife.R

@Composable
fun HomeScreen() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.home_animation)
    )
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.fillMaxSize()
    )
}


@Composable
fun ChartScreen() = com.sweetbytes.couplelife.ui.screen.chart.ChartScreen()

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("⚙️ 專屬設定頁\n(這裡之後要放伴侶連結邀請碼)", fontSize = 20.sp)
    }
}