package org.example.project.navigation

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

sealed class Screen(
    val route: String,
    val icon : ImageVector,
    val title: StringResource? = null,
    val action: NavAction = NavAction.Navigate(route)
) {
    object Home : Screen(
        route = "home_screen",
        icon = Icons.Outlined.Home,
        title = Res.string.nav_home,
    )
    object Reels : Screen(
        route = "reels_screen",
        icon = Icons.Outlined.PlayCircle
    )
    object Search : Screen(
        route = "search_screen",
        icon = Icons.Outlined.Search,
        title = Res.string.nav_search
    )
    object Post : Screen(
        route = "post_screen",
        icon = Icons.Outlined.NearMe,
        title = Res.string.nav_post,
        action = NavAction.Popup.BottomSheet(
            content = {onDismiss, navController ->
                PostPickerContent(
                    onDismiss = onDismiss,
                    navController = navController
                )
            }
        )
    )
    object Profile : Screen(
        route = "Profile_screen",
        icon = Icons.Filled.AccountCircle,
        title = Res.string.nav_profile
    )
    object Setting : Screen(
        route = "setting_screen",
        icon = Icons.Outlined.Settings,
        title = Res.string.nav_setting
    )

    object Detail : Screen(
        route = "detail_screen",
        icon = Icons.Filled.AccountCircle
    )

    object ImagePickerScreen: Screen(
        route = "image_picked_screen",
        icon = Icons.Filled.Image
    )
}

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

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Post,
    Screen.Profile,
    Screen.Setting,
)
val hideBottomBarRoutes = listOf(
    Screen.Post.route,
)
