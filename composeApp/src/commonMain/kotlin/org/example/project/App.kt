package org.example.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import org.example.project.ui.screens.MainScreen
import org.example.project.ui.theme.InstagramTheme

@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
                addPlatformFileSupport()
            }
            .crossfade(true)
            .build()
    }
    InstagramTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            MainScreen()
        }
    }
}