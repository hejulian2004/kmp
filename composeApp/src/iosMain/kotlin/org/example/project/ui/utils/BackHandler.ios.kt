/**
 * @File: BackHandler.ios.kt
 * @Package: org.example.project.ui.utils
 * @Description: iOS平台BackHandler实际实现
 * @Author: 何聚敛
 * @Date: 2026-07-23
 */
package org.example.project.ui.utils

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS无物理返回键，如有右滑手势拦截逻辑可在此扩充
}
