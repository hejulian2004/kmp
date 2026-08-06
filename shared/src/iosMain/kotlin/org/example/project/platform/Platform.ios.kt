package org.example.project.platform

import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIDevice

/**
 * @File: Platform.ios.kt
 * @Description: iOS平台特定实现
 * @Date: 2026-04-20
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
        val paths = platform.Foundation.NSSearchPathForDirectoriesInDomains(
            platform.Foundation.NSDocumentDirectory,
            platform.Foundation.NSUserDomainMask,
            true
        )
        val documentsDirectory = paths.first() as String
        val filePath = "$documentsDirectory/$fileName"
        val fileManager = platform.Foundation.NSFileManager.defaultManager
        if (fileManager.fileExistsAtPath(filePath)) {
            platform.Foundation.NSString.stringWithContentsOfFile(
                filePath,
                platform.Foundation.NSUTF8StringEncoding,
                null
            ) as String?
        } else null
    } catch (e: Exception) {
        null
    }
}

actual fun writeStorageFile(fileName: String, content: String) {
    try {
        val paths = platform.Foundation.NSSearchPathForDirectoriesInDomains(
            platform.Foundation.NSDocumentDirectory,
            platform.Foundation.NSUserDomainMask,
            true
        )
        val documentsDirectory = paths.first() as String
        val filePath = "$documentsDirectory/$fileName"
        (content as platform.Foundation.NSString).writeToFile(
            filePath,
            true,
            platform.Foundation.NSUTF8StringEncoding,
            null
        )
    } catch (e: Exception) {
        // ignore
    }
}
