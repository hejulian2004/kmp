/**
 * @File: InMemorySecureStorageTest.kt
 * @Package: org.example.project.core.network.auth
 * @Description: InMemorySecureStorage 内存安全存储读写删与清空单元测试套件
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.core.network.auth

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InMemorySecureStorageTest {

    @Test
    fun testPutGetRemoveAndClear() = runTest {
        val storage = InMemorySecureStorage()

        // 验证空存储获取为 null
        assertNull(storage.getString("auth_token"))

        // 验证写入与获取
        storage.putString("auth_token", "secret_token_123")
        assertEquals("secret_token_123", storage.getString("auth_token"))

        // 验证删除 key
        storage.remove("auth_token")
        assertNull(storage.getString("auth_token"))

        // 验证多 key 清空
        storage.putString("key1", "val1")
        storage.putString("key2", "val2")
        storage.clear()
        assertNull(storage.getString("key1"))
        assertNull(storage.getString("key2"))
    }
}
