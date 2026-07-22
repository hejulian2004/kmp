package org.example.project.platform

import android.os.Build

/**
 * @File: Platform.android.kt
 * @Description: Android 平台特定实现
 * @Date: 2026-04-20
 */

actual fun getPlatformName(): String = "Android ${Build.VERSION.SDK_INT}"

//获取系统时间
actual fun currentTimeMillis(): Long {
    return System.currentTimeMillis()
}