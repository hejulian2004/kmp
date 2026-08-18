/**
 * @File: PlatformLock.ios.kt
 * @Package: org.example.project.core.concurrent
 * @Description: iOS平台互斥锁actual实现（基于NSLock）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.concurrent

import platform.Foundation.NSLock

internal actual class PlatformLock actual constructor() {
    private val lock = NSLock()

    actual fun <T> withLock(block: () -> T): T {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }
}
