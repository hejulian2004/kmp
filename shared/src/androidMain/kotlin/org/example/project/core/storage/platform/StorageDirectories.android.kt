/**
 * @File: StorageDirectories.android.kt
 * @Package: org.example.project.core.storage.platform
 * @Description: Android 平台存储物理目录映射特定实现
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage.platform

import android.content.Context
import java.io.File

/**
 * Android 平台存储目录映射实现类。
 */
class AndroidStorageDirectories(
    appContext: Context
) : StorageDirectories {

    private val filesDirFile: File = appContext.filesDir
    private val cacheDirFile: File = appContext.cacheDir
    private val tempDirFile: File = File(appContext.cacheDir, "temp").apply {
        if (!exists()) {
            mkdirs()
        }
    }

    override val persistent: String = filesDirFile.absolutePath
    override val cache: String = cacheDirFile.absolutePath
    override val temporary: String = tempDirFile.absolutePath
}

/**
 * Android 平台 fallback 目录映射 (无 Context 测试环境)。
 */
private class FallbackAndroidStorageDirectories : StorageDirectories {
    private val baseDir = File(System.getProperty("java.io.tmpdir") ?: ".", "social_kmp_android_storage")
    
    override val persistent: String = File(baseDir, "files").apply { if (!exists()) mkdirs() }.absolutePath
    override val cache: String = File(baseDir, "cache").apply { if (!exists()) mkdirs() }.absolutePath
    override val temporary: String = File(baseDir, "temp").apply { if (!exists()) mkdirs() }.absolutePath
}

actual fun createPlatformStorageDirectories(context: Any?): StorageDirectories {
    if (context is StorageDirectories) return context
    val androidContext = context as? Context
    val appContext = androidContext?.applicationContext ?: (context as? Context)
    return if (appContext != null) {
        AndroidStorageDirectories(appContext)
    } else {
        throw IllegalStateException("Android Context 必须非空才能初始化 Storage！在测试环境中请使用 TestStorageDirectories 或 FakeFileStorage。")
    }
}
