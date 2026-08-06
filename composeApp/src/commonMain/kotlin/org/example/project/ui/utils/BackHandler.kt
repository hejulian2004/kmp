/**
 * @File: BackHandler.kt
 * @Package: org.example.project.ui.utils
 * @Description: 跨平台BackHandler接口定义
 * @Author: 何聚敛
 * @Date: 2026-07-23
 */
package org.example.project.ui.utils

import androidx.compose.runtime.Composable

/**
 * 跨平台返回键拦截器
 *
 * @param enabled是否开启拦截
 * @param onBack拦截后的返回事件回调
 */
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
