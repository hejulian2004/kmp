package org.example.project.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel { MainViewModel() }
    val showBottomBar by viewModel.showBottomBar
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    screens = bottomNavScreens,
                    navController = navController
                )
            }
        }
    ) { innerPadding ->
        BottomNavGraph(
            navController,
            modifier = Modifier.padding(innerPadding),
            viewModel
        )
    }
}
