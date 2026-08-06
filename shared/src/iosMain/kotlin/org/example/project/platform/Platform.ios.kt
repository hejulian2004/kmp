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
