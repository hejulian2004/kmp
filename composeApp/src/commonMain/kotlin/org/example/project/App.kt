package org.example.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import org.example.project.core.analytics.AnalyticsEvents
import org.example.project.core.analytics.AnalyticsModules
import org.example.project.core.analytics.AnalyticsParams
import org.example.project.core.analytics.AppAnalyticsManager
import org.example.project.core.database.AppDatabaseInitializer
import org.example.project.core.network.client.AppNetworkInitializer
import org.example.project.core.storage.client.AppStorageInitializer
import org.example.project.data.repository.feedline.FeedRepositoryImpl
import org.example.project.data.repository.feedline.generateUUID
import org.example.project.data.repository.wechat.WeChatMpRepositoryImpl
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.presentation.viewmodel.feedline.FeedLineViewModel
import org.example.project.presentation.viewmodel.wechat.WeChatMpViewModel
import org.example.project.ui.screens.airbnb.AirbnbMainScreen
import org.example.project.ui.screens.feedline.FeedScreen
import org.example.project.ui.screens.instagram.InstagramMainScreen
import org.example.project.ui.screens.wechat.WeChatMpScreen
import org.example.project.ui.theme.instagram.InstagramTheme
import org.example.project.ui.theme.wechat.WeChatTheme

import org.example.project.ui.core.image.createAppImageLoader

@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory { context ->
        createAppImageLoader(context)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        val rootNavController = rememberNavController()
        NavHost(
            navController = rootNavController,
            startDestination = "launch"
        ) {
            composable("launch") {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                AppAnalyticsManager.trackEvent(
                                    AnalyticsEvents.OPEN_FEED,
                                    mapOf(
                                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE
                                    )
                                )
                                rootNavController.navigate("feedline")
                            }
                        ) {
                            Text("FeedLine")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { rootNavController.navigate("instagram") }) {
                            Text("Instagram")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { rootNavController.navigate("airbnb") }) {
                            Text("Airbnb")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                AppAnalyticsManager.trackEvent(
                                    AnalyticsEvents.WECHAT_MP_OPEN,
                                    mapOf(
                                        AnalyticsParams.MODULE_NAME to AnalyticsModules.WECHAT_MP
                                    )
                                )
                                rootNavController.navigate("wechat_mp")
                            }
                        ) {
                            Text("微信公众号 (WeChat)")
                        }
                    }
                }
            }
            composable("feedline") {
                val viewModel = remember {
                    val currentUser = FeedLineUser(
                        id = generateUUID(),
                        name = "何聚敛",
                        avatarUrl = "https://i.pravatar.cc/300"
                    )
                    val feedRepository = FeedRepositoryImpl(
                        networkContainer = AppNetworkInitializer.container,
                        feedLineDao = AppDatabaseInitializer.database.feedLineDao()
                    )
                    FeedLineViewModel(
                        feedRepository = feedRepository,
                        currentUser = currentUser
                    )
                }
                FeedScreen(viewModel = viewModel)
            }
            composable("instagram") {
                InstagramTheme {
                    InstagramMainScreen()
                }
            }
            composable("airbnb") {
                AirbnbMainScreen()
            }
            composable("wechat_mp") {
                val viewModel = remember {
                    val repo = WeChatMpRepositoryImpl(
                        weChatMpDao = AppDatabaseInitializer.database.weChatMpDao(),
                        networkContainer = AppNetworkInitializer.container,
                        fileStorage = AppStorageInitializer.container.fileStorage
                    )
                    WeChatMpViewModel(repository = repo)
                }
                WeChatTheme {
                    WeChatMpScreen(
                        viewModel = viewModel,
                        onBackClick = { rootNavController.popBackStack() }
                    )
                }
            }
        }
    }
}
