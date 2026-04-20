package org.example.project.platform

import platform.UIKit.UIDevice

/**
 * @File: Platform.ios.kt
 * @Description: iOS 平台特定实现
 * @Date: 2026-04-20
 */

actual fun getPlatformName(): String {
    return UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}
