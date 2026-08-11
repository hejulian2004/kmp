package org.example.project.platform

import android.os.Build

/**
 * @File: Platform.android.kt
 * @Description: Android平台特定实现
 * @Date: 2026-04-20
 */

actual fun getPlatformName(): String = "Android ${Build.VERSION.SDK_INT}"

//获取系统时间
actual fun currentTimeMillis(): Long {
    return System.currentTimeMillis()
}

actual fun readStorageFile(fileName: String): String? {
    return try {
        val dir = java.io.File(System.getProperty("java.io.tmpdir") ?: ".", "social_kmp_db")
        if (!dir.exists()) dir.mkdirs()
        val file = java.io.File(dir, fileName)
        if (file.exists()) file.readText() else null
    } catch (e: Exception) {
        null
    }
}

actual fun writeStorageFile(fileName: String, content: String) {
    try {
        val dir = java.io.File(System.getProperty("java.io.tmpdir") ?: ".", "social_kmp_db")
        if (!dir.exists()) dir.mkdirs()
        val file = java.io.File(dir, fileName)
        file.writeText(content)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
