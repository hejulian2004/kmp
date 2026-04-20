package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.navigation.AppNavigation
import org.example.project.ui.theme.InstagramTheme

@Composable
@Preview
fun App() {
    InstagramTheme {
        AppNavigation()
    }
}