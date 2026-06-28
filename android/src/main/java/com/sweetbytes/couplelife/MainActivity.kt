package com.sweetbytes.couplelife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sweetbytes.couplelife.ui.screen.ChartScreen
import com.sweetbytes.couplelife.ui.screen.EntryScreen
import com.sweetbytes.couplelife.ui.screen.HomeScreen
import com.sweetbytes.couplelife.ui.screen.Screen
import com.sweetbytes.couplelife.ui.screen.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainNavigationContainer()
            }
        }
    }
}

@Composable
fun MainNavigationContainer() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Entry, Screen.Chart, Screen.Settings)
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedScreen == screen,
                        onClick = {
                            selectedScreen = screen
                            navController.navigate(screen.route) {
                                // 避免重複建立相同的 Destination
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Entry.route) { EntryScreen() }
            composable(Screen.Chart.route) { ChartScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}