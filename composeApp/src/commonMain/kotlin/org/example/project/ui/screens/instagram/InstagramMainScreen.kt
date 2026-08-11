package org.example.project.ui.screens.instagram

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import org.example.project.navigation.instagram.InstagramBottomBar
import org.example.project.navigation.instagram.InstagramBottomNavGraph
import org.example.project.navigation.instagram.instagramBottomNavScreens
import org.example.project.presentation.viewmodel.instagram.InstagramMainViewModel

@Composable
@Preview
fun InstagramMainScreen() {
    val navController = rememberNavController()
    val viewModel: InstagramMainViewModel = viewModel { InstagramMainViewModel() }
    val showBottomBar by viewModel.showBottomBar
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                InstagramBottomBar(
                    screens = instagramBottomNavScreens,
                    navController = navController,
                    modifier = Modifier.padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
                )
            }
        }
    ) { _ ->
        InstagramBottomNavGraph(
            navController,
            modifier = Modifier.fillMaxSize(),
            viewModel
        )
    }
}