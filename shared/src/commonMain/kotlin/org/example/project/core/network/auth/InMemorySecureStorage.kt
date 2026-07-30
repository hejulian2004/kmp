/**
 * @File: InMemorySecureStorage.kt
 * @Package: org.example.project.core.network.auth
 * @Description: 内存版SecureStorage实现，适用于单元测试与无原生加密环境时的备用实现
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.auth

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * [InMemorySecureStorage]
 * 基于内存Map的安全存储实现，使用Mutex保证协程安全。
 */
class InMemorySecureStorage : SecureStorage {
    private val storage = mutableMapOf<String, String>()
    private val mutex = Mutex()

    override suspend fun getString(key: String): String? {
        return mutex.withLock {
            storage[key]
        }
    }

    override suspend fun putString(key: String, value: String) {
        mutex.withLock {
            storage[key] = value
        }
    }

    override suspend fun remove(key: String) {
        mutex.withLock {
            storage.remove(key)
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            storage.clear()
        }
    }
}
