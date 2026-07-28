/**
 * @File: InstagramType.kt
 * @Package: org.example.project.ui.theme.instagram
 * @Description: 定义 Instagram 模块的排版系统与导航控件样式定义
 * @Date: 2026-04-20
 */
package org.example.project.ui.theme.instagram

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AppFontFamily {
    val Default = FontFamily.SansSerif
}

object AppFontWeight {
    val Normal = FontWeight.Normal
    val Medium = FontWeight.Medium
    val Bold   = FontWeight.Bold
}

object AppFontSize {
    val xs : TextUnit = 11.sp
    val sm : TextUnit = 13.sp
    val md : TextUnit = 16.sp
    val lg : TextUnit = 22.sp
    val xl : TextUnit = 28.sp
}

object AppLineHeight {
    val xs : TextUnit = 16.sp
    val sm : TextUnit = 20.sp
    val md : TextUnit = 24.sp
    val lg : TextUnit = 28.sp
}

object AppLetterSpacing {
    val none : TextUnit = 0.sp
    val sm   : TextUnit = 0.5.sp
    val md   : TextUnit = 1.sp
}

val InstagramTypography = Typography(
    titleLarge = TextStyle(
        fontFamily    = AppFontFamily.Default,
        fontWeight    = AppFontWeight.Bold,
        fontSize      = AppFontSize.lg,
        lineHeight    = AppLineHeight.lg,
        letterSpacing = AppLetterSpacing.none
    ),
    bodyLarge = TextStyle(
        fontFamily    = AppFontFamily.Default,
        fontWeight    = AppFontWeight.Normal,
        fontSize      = AppFontSize.md,
        lineHeight    = AppLineHeight.md,
        letterSpacing = AppLetterSpacing.sm
    ),
    bodyMedium = TextStyle(
        fontFamily    = AppFontFamily.Default,
        fontWeight    = AppFontWeight.Normal,
        fontSize      = AppFontSize.sm,
        lineHeight    = AppLineHeight.sm,
        letterSpacing = AppLetterSpacing.sm
    ),
    labelSmall = TextStyle(
        fontFamily    = AppFontFamily.Default,
        fontWeight    = AppFontWeight.Medium,
        fontSize      = AppFontSize.xs,
        lineHeight    = AppLineHeight.xs,
        letterSpacing = AppLetterSpacing.sm
    ),
    labelMedium = TextStyle(
        fontFamily    = AppFontFamily.Default,
        fontWeight    = AppFontWeight.Medium,
        fontSize      = AppFontSize.sm,
        lineHeight    = AppLineHeight.sm,
        letterSpacing = AppLetterSpacing.none
    ),
)

data class NavColors(
    val background     : Color,
    val selectedIcon   : Color,
    val unselectedIcon : Color,
    val divider        : Color,
)

@Composable
fun navColors(): NavColors {
    val scheme = MaterialTheme.colorScheme
    val isDark  = isSystemInDarkTheme()
    return NavColors(
        background     = scheme.surface,
        selectedIcon   = scheme.onSurface,
        unselectedIcon = if (isDark) scheme.secondary
        else scheme.onBackground.copy(alpha = 0.4f),
        divider        = scheme.secondary,
    )
}

object NavSize {
    val IconSize             = 24.dp
    val BigIconSize          = 30.dp
    val IconScaleSelected    = 1.1f
    val IconScaleUnselected  = 1.0f
    val BarPaddingHorizontal = 8.dp
    val BarPaddingVertical   = 12.dp
    val DividerThickness     = 0.5.dp
}

object NavAnim {
    const val DurationMs = 250
}
