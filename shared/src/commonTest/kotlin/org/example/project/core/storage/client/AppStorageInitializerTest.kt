/**
 * @File: AppStorageInitializerTest.kt
 * @Package: org.example.project.core.storage.client
 * @Description: AppStorageInitializer 单例初始化与容器获取单元测试套件
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.storage.client

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AppStorageInitializerTest {

    @Test
    fun testStorageInitializerInitAndContainer() {
        AppStorageInitializer.init(null)

        assertTrue(AppStorageInitializer.isInitialized, "存储架构应处于已初始化状态")
        val container = AppStorageInitializer.container
        assertNotNull(container, "StorageContainer 实例不应为空")
        assertNotNull(container.fileStorage, "fileStorage 实例不应为空")
    }
}
