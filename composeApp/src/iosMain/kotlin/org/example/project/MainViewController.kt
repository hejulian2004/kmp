package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import org.example.project.core.network.client.AppNetworkInitializer

fun MainViewController() = ComposeUIViewController {
    AppNetworkInitializer.init()
    App()
}
