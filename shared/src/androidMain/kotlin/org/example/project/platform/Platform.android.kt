/**
 * @File: Platform.android.kt
 * @Package: org.example.project.platform
 * @Description: Android 平台特定基础设施实现
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.platform

import android.os.Build

actual fun getPlatformName(): String = "Android ${Build.VERSION.SDK_INT}"

actual fun currentTimeMillis(): Long {
    return System.currentTimeMillis()
}
