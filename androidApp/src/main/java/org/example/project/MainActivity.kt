/**
 * @File: MainActivity.kt
 * @Package: org.example.project
 * @Description: Android 应用主 Activity 入口（配置高刷新率 120Hz 申请与降级策略）
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project

import android.os.Build
import android.os.Bundle
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import io.github.vinceglb.filekit.dialogs.init
import org.example.project.core.network.client.AppNetworkInitializer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestHighRefreshRate()
        FileKit.init(this)
        AppNetworkInitializer.init(applicationContext)
        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components {
                    add(KtorNetworkFetcherFactory())
                    addPlatformFileSupport()
                }
                .crossfade(true)
                .build()
        }
        setContent {
            App()
        }
    }

    /**
     * 申请 120Hz 高刷新率模式；如屏幕不支持高刷则优雅降级使用系统默认刷新率
     */
    private fun requestHighRefreshRate() {
        runCatching {
            val supportedModes: Array<Display.Mode>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display?.supportedModes
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay?.supportedModes
            } else null

            if (!supportedModes.isNullOrEmpty()) {
                val highRefreshRateMode = supportedModes
                    .filter { it.refreshRate >= 90f }
                    .maxByOrNull { it.refreshRate }

                if (highRefreshRateMode != null) {
                    window.attributes = window.attributes.apply {
                        preferredDisplayModeId = highRefreshRateMode.modeId
                    }
                }
            }
        }
    }
}
