package org.example.project.platform

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import org.example.project.core.storage.api.StorageArea
import org.example.project.core.storage.api.StoragePath
import org.example.project.core.storage.api.WriteMode
import org.example.project.core.storage.client.AppStorageInitializer

/**
 * @File: Platform.android.kt
 * @Description: Android平台特定实现
 * @Date: 2026-08-18
 */

actual fun getPlatformName(): String = "Android ${Build.VERSION.SDK_INT}"

//获取系统时间
actual fun currentTimeMillis(): Long {
    return System.currentTimeMillis()
}

actual fun readStorageFile(fileName: String): String? {
    return try {
        val storage = AppStorageInitializer.container.fileStorage
        val path = StoragePath(fileName)
        runBlocking(Dispatchers.IO) {
            if (storage.exists(StorageArea.PERSISTENT, path)) {
                storage.read(StorageArea.PERSISTENT, path).decodeToString()
            } else null
        }
    } catch (_: Exception) {
        null
    }
}

actual fun writeStorageFile(fileName: String, content: String) {
    try {
        val storage = AppStorageInitializer.container.fileStorage
        val path = StoragePath(fileName)
        runBlocking(Dispatchers.IO) {
            if (content.isEmpty()) {
                storage.delete(StorageArea.PERSISTENT, path)
            } else {
                storage.write(StorageArea.PERSISTENT, path, content.encodeToByteArray(), WriteMode.ATOMIC)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
