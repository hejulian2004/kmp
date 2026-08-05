/**
 * @File: SettingsIntent.kt
 * @Package: org.example.project.presentation.intent.airbnb
 * @Description: Airbnb 设置页面 MVI 意图集合
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.presentation.intent.airbnb

import org.example.project.presentation.state.airbnb.FontSizeLevel
import org.example.project.presentation.state.airbnb.ThemeMode

sealed interface SettingsIntent {
    data object ToggleCareMode : SettingsIntent
    data class SetThemeMode(val mode: ThemeMode) : SettingsIntent
    data class SetLanguage(val displayName: String, val languageTag: String) : SettingsIntent
    data class SetRegion(val region: String) : SettingsIntent
    data class SetFontSize(val level: FontSizeLevel) : SettingsIntent
    data object TogglePushNotification : SettingsIntent
    data object ToggleBookingNotification : SettingsIntent
    data object ToggleMessageNotification : SettingsIntent
    data object TogglePromotionNotification : SettingsIntent
    data object ClearCache : SettingsIntent
}
