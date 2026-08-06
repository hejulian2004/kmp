/**
 * @File: AirbnbTheme.kt
 * @Package: org.example.project.ui.theme.airbnb
 * @Description: Airbnb模块Theme主题封装与深色模式适配
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.ui.theme.airbnb

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.example.project.presentation.state.airbnb.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = Accent,
    surface = CardBg,
    background = PageBg,
    onPrimary = Color.White,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF6B81),
    surface = Color(0xFF1E1E1E),
    background = Color(0xFF121212),
    onPrimary = Color.White,
    onSurface = Color(0xFFE1E1E1),
    onSurfaceVariant = Color(0xFF9E9E9E),
    outline = Color(0xFF3A3A3A),
)

@Composable
private fun resolveIsDarkTheme(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
}

@Composable
fun AirbnbTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = resolveIsDarkTheme(themeMode)
    applyTheme(dark)

    MaterialTheme(
        colorScheme = if (dark) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
