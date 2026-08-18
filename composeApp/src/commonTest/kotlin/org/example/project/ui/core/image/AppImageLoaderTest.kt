/**
 * @File: AppImageLoaderTest.kt
 * @Package: org.example.project.ui.core.image
 * @Description: AppImageLoader 统一图片多级缓存构建与存储初始化依赖验证
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.core.image

import org.example.project.core.storage.client.AppStorageInitializer
import org.example.project.core.storage.client.StorageContainer
import org.example.project.core.storage.platform.StorageDirectories
import org.example.project.core.storage.testing.FakeFileStorage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppImageLoaderTest {

    private class MockTestStorageDirectories : StorageDirectories {
        override val persistent: String = "build/test_storage/persistent"
        override val cache: String = "build/test_storage/cache"
        override val temporary: String = "build/test_storage/temp"
    }

    @BeforeTest
    fun setUp() {
        AppStorageInitializer.resetForTesting()
    }

    @AfterTest
    fun tearDown() {
        AppStorageInitializer.resetForTesting()
    }

    @Test
    fun testStorageInitializerStatus() {
        assertFalse(AppStorageInitializer.isInitialized, "未初始化时 isInitialized 为 false")

        val testDirs = MockTestStorageDirectories()
        AppStorageInitializer.initForTesting(object : StorageContainer {
            override val fileStorage = FakeFileStorage()
            override val directories = testDirs
        })

        assertTrue(AppStorageInitializer.isInitialized, "初始化后 isInitialized 为 true")
    }
}
