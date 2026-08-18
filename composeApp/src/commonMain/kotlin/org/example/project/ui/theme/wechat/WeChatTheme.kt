/**
 * @File: WeChatTheme.kt
 * @Package: org.example.project.ui.theme.wechat
 * @Description: 微信公众号模块主题配置与Material3色彩映射
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.theme.wechat

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WeChatLightColorScheme = lightColorScheme(
    primary = WeChatBrandGreen,
    onPrimary = WeChatSurfaceWhite,
    background = WeChatBackgroundGray,
    onBackground = WeChatTextPrimary,
    surface = WeChatSurfaceWhite,
    onSurface = WeChatTextPrimary,
    outline = WeChatDividerGray
)

@Composable
fun WeChatTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WeChatLightColorScheme,
        content = content
    )
}
