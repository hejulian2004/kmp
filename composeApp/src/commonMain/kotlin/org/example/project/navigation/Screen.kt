package org.example.project.navigation

sealed class Screen(val route: String) {
    object Home: Screen(route = "home_screen")
    object Detail: Screen(route = "detail_screen")

    object Setting: Screen(route = "Setting_screen")

}