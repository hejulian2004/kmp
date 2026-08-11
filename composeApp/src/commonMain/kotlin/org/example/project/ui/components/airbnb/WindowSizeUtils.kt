/**
 * @File: WindowSizeUtils.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb模块窗口尺寸自适应类工具
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.ui.components.airbnb

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

enum class WindowWidthClass { Compact, Medium, Expanded }

@Composable
fun currentWindowWidthClass(): WindowWidthClass {
    val windowInfo = LocalWindowInfo.current
    val widthDp = with(LocalDensity.current) { windowInfo.containerSize.width.toDp() }
    return when {
        widthDp >= 840.dp -> WindowWidthClass.Expanded
        widthDp >= 600.dp -> WindowWidthClass.Medium
        else -> WindowWidthClass.Compact
    }
}
