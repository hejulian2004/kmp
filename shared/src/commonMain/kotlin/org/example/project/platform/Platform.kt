package org.example.project.platform

/**
 * @File: Platform.kt
 * @Description: 跨平台接口声明，用于获取当前运行平台的名称
 * @Date: 2026-04-20
 */

expect fun getPlatformName(): String
