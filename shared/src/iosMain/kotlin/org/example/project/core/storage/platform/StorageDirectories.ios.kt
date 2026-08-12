/**
 * @File: StorageDirectories.ios.kt
 * @Package: org.example.project.core.storage.platform
 * @Description: iOS 平台存储物理目录映射特定实现
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUserDomainMask

/**
 * iOS 平台存储目录映射实现类。
 */
@OptIn(ExperimentalForeignApi::class)
class IosStorageDirectories : StorageDirectories {

    override val persistent: String by lazy {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSApplicationSupportDirectory,
            NSUserDomainMask,
            true
        )
        val dir = paths.first() as String
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(dir)) {
            fileManager.createDirectoryAtPath(dir, withIntermediateDirectories = true, attributes = null, error = null)
        }
        dir
    }

    override val cache: String by lazy {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSCachesDirectory,
            NSUserDomainMask,
            true
        )
        val dir = paths.first() as String
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(dir)) {
            fileManager.createDirectoryAtPath(dir, withIntermediateDirectories = true, attributes = null, error = null)
        }
        dir
    }

    override val temporary: String by lazy {
        val dir = NSTemporaryDirectory()
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(dir)) {
            fileManager.createDirectoryAtPath(dir, withIntermediateDirectories = true, attributes = null, error = null)
        }
        dir
    }
}

actual fun createPlatformStorageDirectories(context: Any?): StorageDirectories {
    return IosStorageDirectories()
}
