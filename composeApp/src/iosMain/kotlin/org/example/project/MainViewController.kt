package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import org.example.project.core.init.AppInitParams
import org.example.project.core.init.AppInitializer

fun MainViewController() = ComposeUIViewController {
    AppInitializer.init(AppInitParams(platformName = "iOS"))
    App()
}
