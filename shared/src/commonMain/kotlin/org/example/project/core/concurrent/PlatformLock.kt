/**
 * @File: PlatformLock.kt
 * @Package: org.example.project.core.concurrent
 * @Description: KMP跨平台互斥锁expect声明，提供多平台统一的同步块执行能力
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.concurrent

/**
 * 跨平台互斥锁expect类
 */
internal expect class PlatformLock() {
    fun <T> withLock(block: () -> T): T
}
