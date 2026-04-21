package org.example.project.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.example.project.navigation.BottomNavGraph
import org.example.project.navigation.BottomBar
import org.example.project.navigation.Screen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            val screens = listOf(
                Screen.Home,
                Screen.Reels,
                Screen.Post,
                Screen.Search,
                Screen.Profile,
            )
            BottomBar(screens = screens, navController = navController)
        }
    ) { innerPadding->
        BottomNavGraph(
            navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
