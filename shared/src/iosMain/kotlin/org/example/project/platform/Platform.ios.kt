package org.example.project.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import org.example.project.core.storage.api.StorageArea
import org.example.project.core.storage.api.StoragePath
import org.example.project.core.storage.api.WriteMode
import org.example.project.core.storage.client.AppStorageInitializer
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIDevice

/**
 * @File: Platform.ios.kt
 * @Description: iOS平台特定实现
 * @Date: 2026-08-12
 */

actual fun getPlatformName(): String {
    return UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

//获取系统时间
actual fun currentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual fun readStorageFile(fileName: String): String? {
    return try {
        if (!AppStorageInitializer.isInitialized) {
            AppStorageInitializer.init()
        }
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
        if (!AppStorageInitializer.isInitialized) {
            AppStorageInitializer.init()
        }
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
