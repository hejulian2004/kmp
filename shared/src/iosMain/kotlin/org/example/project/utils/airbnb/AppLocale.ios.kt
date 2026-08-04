/**
 * @File: AppLocale.ios.kt
 * @Package: org.example.project.utils.airbnb
 * @Description: iOS 平台 AppLocale actual 实现
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.utils.airbnb

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual fun applyAppLanguage(languageTag: String) {
    // iOS 不支持运行时直接切换 App 语言，留在系统设置处理
}

actual fun getInitialLanguageTag(): String {
    return NSLocale.currentLocale.languageCode
}
