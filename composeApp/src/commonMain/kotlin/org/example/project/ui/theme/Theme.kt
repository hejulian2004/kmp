/**
 * @File: Theme.kt
 * @Description: 定义应用程序的颜色方案及主题配置，支持深浅色模式切换
 * @Date: 2026-04-20
 */

package org.example.project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** 深色模式颜色方案 */
private val DarkColorScheme = darkColorScheme(
    primary = InstagramBlue,
    onPrimary = Color.White,
    secondary = InstagramDarkGray,
    onSecondary = Color.White,
    tertiary = InstagramStoryPink,
    background = Color.Black,
    surface = InstagramBlack,
    onBackground = Color.White,
    onSurface = Color.White,
)

/** 浅色模式颜色方案 */
private val LightColorScheme = lightColorScheme(
    primary = InstagramBlue,
    onPrimary = Color.White,
    secondary = InstagramLightGray,
    onSecondary = InstagramBlack,
    tertiary = InstagramStoryPink,
    background = Color.White,
    surface = Color.White,
    onBackground = InstagramBlack,
    onSurface = InstagramBlack,
)

/**
 * [InstagramTheme]
 * 应用程序全局主题配置，统一应用 Material3 规范、自定义颜色及排版。
 * 
 * @param darkTheme 是否启用深色模式，默认跟随系统设置
 * @param content 需要应用此主题的可组合项
 */
@Composable
fun InstagramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = InstagramTypography,
        content = content
    )
}
