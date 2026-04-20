package org.example.project.navigation




sealed class BottomBarScreen(
    val route: String,
    val title:String,
) {
    object Home: Screen(
        route = "home_screen",
    )
    object Detail: Screen(
        route = "detail_screen"
    )
    object Setting: Screen(
        route = "Setting_screen"
    )
}