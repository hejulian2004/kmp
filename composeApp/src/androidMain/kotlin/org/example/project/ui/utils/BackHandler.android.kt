/**
 * @File: BackHandler.android.kt
 * @Package: org.example.project.ui.utils
 * @Description: Android平台BackHandler实际实现
 * @Author: 何聚敛
 * @Date: 2026-07-23
 */
package org.example.project.ui.utils

import androidx.activity.compose.BackHandler as AndroidXBackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    AndroidXBackHandler(enabled = enabled, onBack = onBack)
}
