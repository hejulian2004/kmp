/**
 * @File: Platform.ios.kt
 * @Package: org.example.project.platform
 * @Description: iOS 平台特定基础设施实现
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.platform

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIDevice

actual fun getPlatformName(): String {
    return UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun currentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}
