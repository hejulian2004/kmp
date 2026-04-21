/**
 * @File: Type.kt
 * @Description: 定义应用程序的排版系统，包含字体家族、字号及行高
 * @Date: 2026-04-20
 */

package org.example.project.ui.theme

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

// 字体家族
object AppFontFamily {
    val Default = FontFamily.SansSerif
}
// 字重
object AppFontWeight {
    val Normal  = FontWeight.Normal
    val Medium  = FontWeight.Medium
    val Bold    = FontWeight.Bold
}
// 字号
object AppFontSize {
    val xs    :  TextUnit = 11.sp   // labelSmall
    val sm    :  TextUnit = 13.sp
    val md    :  TextUnit = 16.sp   // bodyLarge
    val lg    :  TextUnit = 22.sp   // titleLarge
    val xl    :  TextUnit = 28.sp
}
// 行高
object AppLineHeight {
    val xs    : TextUnit = 16.sp
    val sm    : TextUnit = 20.sp
    val md    : TextUnit = 24.sp
    val lg    : TextUnit = 28.sp
}
// 字间距
object AppLetterSpacing {
    val none : TextUnit = 0.sp
    val sm : TextUnit = 0.5.sp
    val md : TextUnit = 1.sp
}


/**
 * 项目标准排版配置
 * 包含：
 * - titleLarge: 用于页面标题及品牌 Logo
 * - bodyLarge: 用于主要的列表项及正文
 * - labelSmall: 用于次要的辅助信息
 */
val InstagramTypography = Typography(

    // 用于页面标题及品牌 Logo
    titleLarge = TextStyle(
        fontFamily    = AppFontFamily.Default,
        fontWeight    = AppFontWeight.Bold,
        fontSize      = AppFontSize.lg,
        lineHeight    = AppLineHeight.lg,
        letterSpacing = AppLetterSpacing.none
    ),

    // 用于主要的列表项及正文
    bodyLarge = TextStyle(
        fontFamily    = AppFontFamily.Default,
        fontWeight    = AppFontWeight.Normal,
        fontSize      = AppFontSize.md,
        lineHeight    = AppLineHeight.md,
        letterSpacing = AppLetterSpacing.sm
    ),

    // 用于次要的辅助信息
    labelSmall = TextStyle(
        fontFamily    = AppFontFamily.Default,
        fontWeight    = AppFontWeight.Medium,
        fontSize      = AppFontSize.xs,
        lineHeight    = AppLineHeight.xs,
        letterSpacing = AppLetterSpacing.sm
    )
)


data class NavColors(
    val background : Color,
    val selectedIcon : Color,
    val unselectedIcon: Color,
    val divider : Color,
)

@Composable
fun navColors(): NavColors {
    val scheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    return NavColors(
        background     = scheme.surface,
        unselectedIcon = if (isDark) scheme.secondary else scheme.onBackground.copy(alpha = 0.4f),
        selectedIcon   = scheme.onSurface,
        divider        = scheme.secondary,
    )
}


object NavSize {
    val IconSize = 24.dp
    val BigIconSize = 30.dp
    val IconScaleSelected  = 1.1f
    val IconScaleUnselected = 1.0f
    val BarPaddingHorizontal = 8.dp
    val BarPaddingVertical = 12.dp
    val DividerThickness = 0.5.dp
}

object NavAnim {
    const val DurationMs = 250
}