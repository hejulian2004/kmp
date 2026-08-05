/**
 * @File: SettingsViewModel.kt
 * @Package: org.example.project.presentation.viewmodel.airbnb
 * @Description: Airbnb 设置页面 MVI ViewModel（保留原有多语言/语言映射、主题配置等全部逻辑）
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.presentation.viewmodel.airbnb

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.example.project.presentation.intent.airbnb.SettingsIntent
import org.example.project.presentation.state.airbnb.FontSizeLevel
import org.example.project.presentation.state.airbnb.SettingsUiState
import org.example.project.presentation.state.airbnb.ThemeMode
import org.example.project.utils.airbnb.applyAppLanguage
import org.example.project.utils.airbnb.getInitialLanguageTag

// 语言标签（小写）→ 界面显示名称
private val LANGUAGE_TAG_TO_DISPLAY = mapOf(
    "zh" to "中文（简体）",
    "zh-cn" to "中文（简体）",
    "zh-hans" to "中文（简体）",
    "zh-tw" to "中文（繁體）",
    "zh-hk" to "中文（繁體）",
    "zh-hant" to "中文（繁體）",
    "en" to "English",
    "ja" to "日本語",
    "ko" to "한국어",
    "fr" to "Français",
    "es" to "Español",
    "de" to "Deutsch",
    "pt" to "Português",
    "ru" to "Русский",
    "ar" to "العربية",
    "hi" to "हिन्दी",
)

// 界面显示名称 → 语言标签（用于点击时传给 applyAppLanguage）
internal val DISPLAY_TO_LANGUAGE_TAG = mapOf(
    "中文（简体）" to "zh",
    "中文（繁體）" to "zh-TW",
    "English" to "en",
    "日本語" to "ja",
    "한국어" to "ko",
    "Français" to "fr",
    "Español" to "es",
    "Deutsch" to "de",
    "Português" to "pt",
    "Русский" to "ru",
    "العربية" to "ar",
    "हिन्दी" to "hi",
)

private fun tagToDisplayName(tag: String): String {
    val lower = tag.lowercase()
    return LANGUAGE_TAG_TO_DISPLAY[lower]
        ?: LANGUAGE_TAG_TO_DISPLAY[lower.substringBefore("-")]
        ?: "中文（简体）"
}

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            selectedLanguage = tagToDisplayName(getInitialLanguageTag())
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ToggleCareMode -> toggleCareMode()
            is SettingsIntent.SetThemeMode -> setThemeMode(intent.mode)
            is SettingsIntent.SetLanguage -> setLanguage(intent.displayName, intent.languageTag)
            is SettingsIntent.SetRegion -> setRegion(intent.region)
            is SettingsIntent.SetFontSize -> setFontSize(intent.level)
            is SettingsIntent.TogglePushNotification -> togglePushNotification()
            is SettingsIntent.ToggleBookingNotification -> toggleBookingNotification()
            is SettingsIntent.ToggleMessageNotification -> toggleMessageNotification()
            is SettingsIntent.TogglePromotionNotification -> togglePromotionNotification()
            is SettingsIntent.ClearCache -> clearCache()
        }
    }

    private fun toggleCareMode() {
        _uiState.update { it.copy(careModeEnabled = !it.careModeEnabled) }
    }

    private fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    /** displayName: 界面显示名称，languageTag: BCP-47 语言标签（如 "en"、"zh"）*/
    private fun setLanguage(displayName: String, languageTag: String) {
        applyAppLanguage(languageTag)
        _uiState.update { it.copy(selectedLanguage = displayName) }
    }

    private fun setRegion(region: String) {
        _uiState.update { it.copy(selectedRegion = region) }
    }

    private fun setFontSize(level: FontSizeLevel) {
        _uiState.update { it.copy(fontSizeLevel = level) }
    }

    private fun togglePushNotification() {
        _uiState.update { state ->
            val ns = state.notificationSettings
            state.copy(notificationSettings = ns.copy(pushEnabled = !ns.pushEnabled))
        }
    }

    private fun toggleBookingNotification() {
        _uiState.update { state ->
            val ns = state.notificationSettings
            state.copy(notificationSettings = ns.copy(bookingEnabled = !ns.bookingEnabled))
        }
    }

    private fun toggleMessageNotification() {
        _uiState.update { state ->
            val ns = state.notificationSettings
            state.copy(notificationSettings = ns.copy(messageEnabled = !ns.messageEnabled))
        }
    }

    private fun togglePromotionNotification() {
        _uiState.update { state ->
            val ns = state.notificationSettings
            state.copy(notificationSettings = ns.copy(promotionEnabled = !ns.promotionEnabled))
        }
    }

    private fun clearCache() {
        _uiState.update { it.copy(cacheCleared = true) }
    }
}
