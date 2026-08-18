/**
 * @File: WeChatAdaptiveLayout.kt
 * @Package: org.example.project.ui.components.wechat
 * @Description: 微信公众号屏幕多状态自适应与相对布局工具
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.components.wechat

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 屏幕宽度自适应断点类型
 */
enum class WeChatWindowSizeClass {
    /** 竖屏窄屏手机 (<600dp) */
    Compact,

    /** 折叠屏/平板中屏 (600dp~840dp) */
    Medium,

    /** 平板横屏/桌面大屏 (>=840dp) */
    Expanded
}

/**
 * 获取当前窗口尺寸断点
 */
@Composable
fun rememberWeChatWindowSizeClass(): WeChatWindowSizeClass {
    val windowInfo = LocalWindowInfo.current
    val widthDp = with(LocalDensity.current) { windowInfo.containerSize.width.toDp() }
    return when {
        widthDp >= 840.dp -> WeChatWindowSizeClass.Expanded
        widthDp >= 600.dp -> WeChatWindowSizeClass.Medium
        else -> WeChatWindowSizeClass.Compact
    }
}

/**
 * 根据屏幕状态动态计算瀑布流列数
 */
fun getWaterfallColumnCount(sizeClass: WeChatWindowSizeClass): Int {
    return when (sizeClass) {
        WeChatWindowSizeClass.Compact -> 2
        WeChatWindowSizeClass.Medium -> 3
        WeChatWindowSizeClass.Expanded -> 4
    }
}

/**
 * 获取大屏状态下的主容器最大宽度
 */
fun getMaxAdaptiveContentWidth(sizeClass: WeChatWindowSizeClass): Dp {
    return when (sizeClass) {
        WeChatWindowSizeClass.Compact -> Dp.Unspecified
        WeChatWindowSizeClass.Medium -> 680.dp
        WeChatWindowSizeClass.Expanded -> 920.dp
    }
}
