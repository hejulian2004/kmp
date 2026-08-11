package org.example.project.platform

/**
 * @File: Platform.kt
 * @Description: 跨平台接口声明，用于获取当前运行平台的名称
 * @Date: 2026-04-20
 */

expect fun getPlatformName(): String

//获取系统时间
expect fun currentTimeMillis(): Long

/**
 * 读取本地磁盘持久化数据
 */
expect fun readStorageFile(fileName: String): String?

/**
 * 写入本地磁盘持久化数据
 */
expect fun writeStorageFile(fileName: String, content: String)
