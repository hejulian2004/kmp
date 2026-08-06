/**
 * @File: SettingsUiState.kt
 * @Package: org.example.project.presentation.state.airbnb
 * @Description: Airbnb设置页面MVI状态与配置枚举
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.presentation.state.airbnb

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class FontSizeLevel { SMALL, NORMAL, LARGE, EXTRA_LARGE }

data class NotificationSettings(
    val pushEnabled: Boolean = true,
    val bookingEnabled: Boolean = true,
    val messageEnabled: Boolean = true,
    val promotionEnabled: Boolean = false,
)

data class SettingsUiState(
    val careModeEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedLanguage: String = "中文（简体）",
    val selectedRegion: String = "中国",
    val fontSizeLevel: FontSizeLevel = FontSizeLevel.NORMAL,
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val cacheCleared: Boolean = false,
)
