/**
 * @File: AirbnbColor.kt
 * @Package: org.example.project.ui.theme.airbnb
 * @Description: Airbnb 模块动态主题颜色定义（保留原 AppColors 逻辑与完整变体）
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.ui.theme.airbnb

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Reassignable colors — they switch between light/dark when setTheme() is called.
// All existing UI files reference these top-level vals and will recompose on change.

// Light defaults
private val LightPageBg = Color(0xFFF7F7F7)
private val LightTextPrimary = Color(0xFF222222)
private val LightTextSecondary = Color(0xFF6A6A6A)
private val LightAccent = Color(0xFFFF385C)
private val LightDividerColor = Color(0xFFE6E6E6)
private val LightGuideBg = Color(0xFFF4E9EE)
private val LightCardBg = Color.White
private val LightImagePlaceholder = Color(0xFFEDEDED)

// Dark values
private val DarkPageBg = Color(0xFF121212)
private val DarkTextPrimary = Color(0xFFE1E1E1)
private val DarkTextSecondary = Color(0xFF9E9E9E)
private val DarkAccent = Color(0xFFFF6B81)
private val DarkDividerColor = Color(0xFF3A3A3A)
private val DarkGuideBg = Color(0xFF2A1E23)
private val DarkCardBg = Color(0xFF1E1E1E)
private val DarkImagePlaceholder = Color(0xFF2C2C2C)

// Theme-aware top-level vals — existing imports continue to work
var PageBg by mutableStateOf(LightPageBg)
var TextPrimary by mutableStateOf(LightTextPrimary)
var TextSecondary by mutableStateOf(LightTextSecondary)
var Accent by mutableStateOf(LightAccent)
var DividerColor by mutableStateOf(LightDividerColor)
var GuideBg by mutableStateOf(LightGuideBg)
var CardBg by mutableStateOf(LightCardBg)
var ImagePlaceholder by mutableStateOf(LightImagePlaceholder)

fun applyTheme(isDark: Boolean) {
    PageBg = if (isDark) DarkPageBg else LightPageBg
    TextPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    TextSecondary = if (isDark) DarkTextSecondary else LightTextSecondary
    Accent = if (isDark) DarkAccent else LightAccent
    DividerColor = if (isDark) DarkDividerColor else LightDividerColor
    GuideBg = if (isDark) DarkGuideBg else LightGuideBg
    CardBg = if (isDark) DarkCardBg else LightCardBg
    ImagePlaceholder = if (isDark) DarkImagePlaceholder else LightImagePlaceholder
}
