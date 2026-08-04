/**
 * @File: AppLocale.kt
 * @Package: org.example.project.utils.airbnb
 * @Description: Airbnb 模块语言选择与底层切换工具入口（包含 expect 定义）
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.utils.airbnb

/** 设置应用语言，触发界面重建使新语言生效。 */
expect fun applyAppLanguage(languageTag: String)

/** 读取当前应用语言标签（如 "zh"、"en"），用于初始化 ViewModel 选中状态。 */
expect fun getInitialLanguageTag(): String
