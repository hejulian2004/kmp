package org.example.project.navigation.instagram

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import org.example.project.ui.components.feedline.PostPickerContent
import org.jetbrains.compose.resources.StringResource
import kotlinproject.composeapp.generated.resources.*

sealed class InstagramScreen(
    val route: String,
    val icon : ImageVector,
    val title: StringResource? = null,
    val action: NavAction = NavAction.Navigate(route)
) {
    object Home : InstagramScreen(
        route = "home_screen",
        icon = Icons.Outlined.Home,
        title = Res.string.nav_home,
    )
    object Reels : InstagramScreen(
        route = "reels_screen",
        icon = Icons.Outlined.PlayCircle
    )
    object Search : InstagramScreen(
        route = "search_screen",
        icon = Icons.Outlined.Search,
        title = Res.string.nav_search
    )
    object Post : InstagramScreen(
        route = "post_screen",
        icon = Icons.Outlined.NearMe,
        title = Res.string.nav_post,
        action = NavAction.Popup.BottomSheet(
            content = { onDismiss, navController ->
                PostPickerContent(
                    onDismiss = onDismiss,
                    navController = navController
                )
            }
        )
    )
    object Profile : InstagramScreen(
        route = "Profile_screen",
        icon = Icons.Filled.AccountCircle,
        title = Res.string.nav_profile
    )
    object Setting : InstagramScreen(
        route = "setting_screen",
        icon = Icons.Outlined.Settings,
        title = Res.string.nav_setting
    )

    object Detail : InstagramScreen(
        route = "detail_screen",
        icon = Icons.Filled.AccountCircle
    )

    object ImagePickerScreen : InstagramScreen(
        route = "image_picked_screen",
        icon = Icons.Filled.Image
    )
}

typealias Screen = InstagramScreen

sealed class NavAction {
    data class Navigate(val route: String) : NavAction()

    sealed class Popup : NavAction() {
        data class BottomSheet(
            val content: @Composable (
                onDismiss: () -> Unit,
                navController: NavHostController
            ) -> Unit
        ) : Popup()
        data class TopSheet(
            val content: @Composable (onDismiss: () -> Unit) -> Unit
        ) : Popup()
        data class Dialog(
            val content: @Composable (onDismiss: () -> Unit) -> Unit
        ) : Popup()
    }
}

val instagramBottomNavScreens = listOf(
    InstagramScreen.Home,
    InstagramScreen.Search,
    InstagramScreen.Post,
    InstagramScreen.Profile,
    InstagramScreen.Setting,
)

val bottomNavScreens = instagramBottomNavScreens

val hideBottomBarRoutes = listOf(
    InstagramScreen.Post.route,
)
