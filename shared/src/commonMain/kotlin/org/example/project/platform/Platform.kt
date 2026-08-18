/**
 * @File: Platform.kt
 * @Package: org.example.project.platform
 * @Description: 跨平台基础物理环境与时间接口声明
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.platform

/**
 * 获取当前运行平台的名称。
 */
expect fun getPlatformName(): String

/**
 * 获取当前平台毫秒级系统时间戳。
 */
expect fun currentTimeMillis(): Long
