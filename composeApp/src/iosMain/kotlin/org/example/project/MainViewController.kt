package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import org.example.project.core.init.AppInitParams
import org.example.project.ui.components.StartupGate

fun MainViewController() = ComposeUIViewController {
    StartupGate(
        initParams = AppInitParams(platformName = "iOS")
    ) {
        App()
    }
}
