package org.example.project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.Navigator

private val DarkColorScheme = darkColorScheme(
    primary      = InstagramBlue,
    onPrimary    = Color.White,
    secondary    = InstagramDarkGray,
    onSecondary  = Color.White,
    tertiary     = InstagramStoryPink,
    background   = Color.Black,
    surface      = InstagramBlack,
    onBackground = Color.White,
    onSurface    = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary      = InstagramBlue,
    onPrimary    = Color.White,
    secondary    = InstagramLightGray,
    onSecondary  = InstagramBlack,
    tertiary     = InstagramStoryPink,
    background   = Color.White,
    surface      = Color.White,
    onBackground = InstagramBlack,
    onSurface    = InstagramBlack,
)


/**
 * 间距系统：统一管理组件内外边距
 *
 *  xxs  2dp  —— 最小间距，用于紧凑型元素内部
 *  xs   4dp  —— 图标与文字间距、角标偏移
 *  sm   8dp  —— 列表项内部元素间距
 *  md  16dp  —— 标准内边距（卡片/页面水平 padding）
 *  lg  24dp  —— 区块间距
 *  xl  32dp  —— 页面顶部安全间距
 *  xxl 48dp  —— 大区块分隔
 */
data class AppSpacing(
    val xxs : Dp = 2.dp,
    val xs  : Dp = 4.dp,
    val sm  : Dp = 8.dp,
    val md  : Dp = 16.dp,
    val lg  : Dp = 24.dp,
    val xl  : Dp = 32.dp,
    val xxl : Dp = 48.dp,
)


/**
 * 尺寸系统：统一管理组件固定尺寸
 *
 *  Avatar  —— 头像三档
 *  Icon    —— 图标三档
 *  Card    —— 卡片宽度与圆角
 *  Border  —— 通用描边粗细
 *  Button  —— 按钮圆角
 */
data class AppSize(
    val avatarSm     : Dp = 32.dp,
    val avatarMd     : Dp = 80.dp,
    val avatarLg     : Dp = 120.dp,
    val iconSm       : Dp = 16.dp,
    val iconMd       : Dp = 24.dp,
    val iconLg       : Dp = 30.dp,
    val cardWidth    : Dp = 200.dp,
    val cardRadius   : Dp = 12.dp,
    val borderWidth  : Dp = 1.dp,
    val dividerWidth : Dp = 0.5.dp,
    val buttonRadius : Dp = 8.dp,
    val topBarHeight : Dp = 50.dp,
    val navigationBarHeight:Dp = 90.dp
)


val LocalSpacing = staticCompositionLocalOf { AppSpacing() }
val LocalSize    = staticCompositionLocalOf { AppSize() }

val MaterialTheme.spacing: AppSpacing
    @Composable @ReadOnlyComposable get() = LocalSpacing.current

val MaterialTheme.size: AppSize
    @Composable @ReadOnlyComposable get() = LocalSize.current


/**
 * [InstagramTheme]
 *    InstagramTheme { MainScreen() }
 */
@Composable
fun InstagramTheme(
    darkTheme : Boolean    = isSystemInDarkTheme(),
    spacing   : AppSpacing = AppSpacing(),
    size      : AppSize    = AppSize(),
    content   : @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalSpacing provides spacing,
        LocalSize    provides size,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = InstagramTypography,
            content     = content
        )
    }
}