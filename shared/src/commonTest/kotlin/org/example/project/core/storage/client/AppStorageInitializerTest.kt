/**
 * @File: AppStorageInitializerTest.kt
 * @Package: org.example.project.core.storage.client
 * @Description: AppStorageInitializer 单例初始化与容器获取单元测试套件
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.storage.client

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.example.project.core.storage.testing.TestStorageDirectories
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AppStorageInitializerTest {

    @BeforeTest
    fun setup() {
        AppStorageInitializer.resetForTesting()
    }

    @Test
    fun testAccessContainerUninitializedThrowsException() {
        assertFailsWith<IllegalStateException> {
            AppStorageInitializer.container
        }
    }

    @Test
    fun testStorageInitializerInitAndContainer() = runTest {
        val testDirs = TestStorageDirectories()
        AppStorageInitializer.init(testDirs)

        assertTrue(AppStorageInitializer.isInitialized, "存储架构应处于已初始化状态")
        val container = AppStorageInitializer.container
        assertNotNull(container, "StorageContainer 实例不应为空")
        assertNotNull(container.fileStorage, "fileStorage 实例不应为空")
    }

    @Test
    fun testReInitReturnsSameContainer() = runTest {
        val testDirs = TestStorageDirectories()
        AppStorageInitializer.init(testDirs)
        val container1 = AppStorageInitializer.container

        AppStorageInitializer.init(testDirs)
        val container2 = AppStorageInitializer.container

        assertSame(container1, container2, "重复 init 应保留同一 Container 单例")
    }

    @Test
    fun testConcurrentInit() = runTest {
        val testDirs = TestStorageDirectories()
        val deferreds = (1..10).map {
            async {
                AppStorageInitializer.init(testDirs)
            }
        }
        deferreds.awaitAll()

        assertTrue(AppStorageInitializer.isInitialized)
        assertNotNull(AppStorageInitializer.container)
    }
}
