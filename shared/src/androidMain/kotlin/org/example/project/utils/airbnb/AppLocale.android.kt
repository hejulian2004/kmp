/**
 * @File: AppLocale.android.kt
 * @Package: org.example.project.utils.airbnb
 * @Description: Android平台AppLocale actual实现
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.utils.airbnb

actual fun applyAppLanguage(languageTag: String) {
    // Android端语言切换逻辑（可以通过AppCompatDelegate设置）
}

actual fun getInitialLanguageTag(): String {
    return "zh"
}
