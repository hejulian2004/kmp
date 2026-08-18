/**
 * @File: PlatformLock.android.kt
 * @Package: org.example.project.core.concurrent
 * @Description: Android/JVM平台互斥锁actual实现（基于ReentrantLock）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.concurrent

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock as jvmWithLock

internal actual class PlatformLock actual constructor() {
    private val lock = ReentrantLock()

    actual fun <T> withLock(block: () -> T): T {
        return lock.jvmWithLock(block)
    }
}
