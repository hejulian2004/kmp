package org.example.project.navigation.instagram

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.example.project.presentation.viewmodel.instagram.InstagramMainViewModel
import org.example.project.ui.screens.instagram.InstagramHomeScreen
import org.example.project.ui.screens.instagram.InstagramImagePickerScreen
import org.example.project.ui.screens.instagram.InstagramProfileScreen
import org.example.project.ui.screens.instagram.InstagramSettingScreen

@Composable
fun InstagramBottomNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedViewModel: InstagramMainViewModel? = null
) {
    NavHost(
        navController = navController,
        startDestination = InstagramScreen.Profile.route,
        modifier = modifier
    ) {
        composable(route = InstagramScreen.Home.route) {
            InstagramHomeScreen()
        }
        composable(route = InstagramScreen.Profile.route) {
            InstagramProfileScreen()
        }
        composable(route = InstagramScreen.Setting.route) {
            InstagramSettingScreen(navController)
        }
        composable(route = InstagramScreen.Reels.route) {
            InstagramSettingScreen(navController)
        }
        composable(route = InstagramScreen.Detail.route) {
            InstagramSettingScreen(navController)
        }
        composable(route = InstagramScreen.Post.route) {
            InstagramSettingScreen(navController)
        }
        composable(route = InstagramScreen.Search.route) {
            InstagramSettingScreen(navController)
        }
        composable(route = InstagramScreen.ImagePickerScreen.route) {
            DisposableEffect(Unit) {
                sharedViewModel?.setBottomBarVisible(false)
                onDispose {
                    sharedViewModel?.setBottomBarVisible(true)
                }
            }
            InstagramImagePickerScreen(navController)
        }
    }
}

@Composable
fun BottomNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedViewModel: InstagramMainViewModel? = null
) = InstagramBottomNavGraph(navController, modifier, sharedViewModel)
