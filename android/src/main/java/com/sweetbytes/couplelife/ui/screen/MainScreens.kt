package com.sweetbytes.couplelife.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

