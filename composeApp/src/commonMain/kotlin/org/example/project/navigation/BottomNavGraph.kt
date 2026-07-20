package org.example.project.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.example.project.presentation.viewmodel.MainViewModel
import org.example.project.ui.screens.HomeScreen
import org.example.project.ui.screens.ImagePickerScreen
import org.example.project.ui.screens.profilescreen.ProfileScreen
import org.example.project.ui.screens.SettingScreen


@Composable
fun BottomNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedViewModel: MainViewModel? = null
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Profile.route,
        modifier = modifier
    ){
        composable(
            route = Screen.Home.route
        ) {
            HomeScreen()
        }
        composable(
            route = Screen.Profile.route
        ) {
            ProfileScreen()
        }
        composable(
            route = Screen.Setting.route
        ) {
            SettingScreen(navController)
        }
        composable(
            route = Screen.Reels.route
        ) {
            SettingScreen(navController)
        }
        composable(
            route = Screen.Detail.route
        ) {
            SettingScreen(navController)
        }
        composable(
            route = Screen.Post.route
        ) {
            SettingScreen(navController)
        }
        composable(
            route = Screen.Search.route
        ) {
            SettingScreen(navController)
        }
        composable(
            route = Screen.ImagePickerScreen.route
        ) {
            DisposableEffect(Unit) {
                sharedViewModel?.setBottomBarVisible(false)
                onDispose {
                    sharedViewModel?.setBottomBarVisible(true)
                }
            }
            ImagePickerScreen(navController)
        }
    }
}
