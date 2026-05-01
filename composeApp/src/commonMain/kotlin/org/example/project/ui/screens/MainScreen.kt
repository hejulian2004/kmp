package org.example.project.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.example.project.navigation.BottomNavGraph
import org.example.project.navigation.BottomBar
import org.example.project.navigation.Screen
import org.example.project.navigation.bottomNavScreens
import org.example.project.navigation.hideBottomBarRoutes
import org.example.project.presentation.viewmodel.MainViewModel
import org.example.project.presentation.viewmodel.PostEditViewModel
import org.example.project.ui.theme.size

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel { MainViewModel() }
    val showBottomBar by viewModel.showBottomBar
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    screens = bottomNavScreens,
                    navController = navController,
                    modifier = Modifier.padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
                )
            }
        }
    ) { _ ->
        BottomNavGraph(
            navController,
            modifier = Modifier.fillMaxSize(),
            viewModel
        )
    }
}
