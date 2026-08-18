/**
 * @File: FileStorageCacheTest.kt
 * @Package: org.example.project.core.storage
 * @Description: FileStorage 缓存区 (StorageArea.CACHE) 读写、多级目录隔离、元数据与清理单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.storage

import kotlinx.coroutines.test.runTest
import org.example.project.core.storage.api.StorageArea
import org.example.project.core.storage.api.StoragePath
import org.example.project.core.storage.api.WriteMode
import org.example.project.core.storage.internal.DefaultFileStorage
import org.example.project.core.storage.testing.TestStorageDirectories
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FileStorageCacheTest {

    private val directories = TestStorageDirectories()
    private val storage = DefaultFileStorage(directories)

    @Test
    fun testCacheAreaReadWriteAndIsolation() = runTest {
        val imageCachePath = StoragePath("image_cache/avatar_001.jpg")
        val videoCachePath = StoragePath("video_cache/video_001.mp4")
        val dummyImageData = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        val dummyVideoData = byteArrayOf(0x00, 0x00, 0x00, 0x18)

        storage.write(StorageArea.CACHE, imageCachePath, dummyImageData, WriteMode.ATOMIC)
        storage.write(StorageArea.CACHE, videoCachePath, dummyVideoData, WriteMode.ATOMIC)

        assertTrue(storage.exists(StorageArea.CACHE, imageCachePath), "缓存区图片文件应存在")
        assertTrue(storage.exists(StorageArea.CACHE, videoCachePath), "缓存区视频文件应存在")

        // 验证持久区与临时区无法越界读取缓存区文件（区域隔离）
        assertFalse(storage.exists(StorageArea.PERSISTENT, imageCachePath), "持久化区不应存在缓存区文件")
        assertFalse(storage.exists(StorageArea.TEMPORARY, imageCachePath), "临时区不应存在缓存区文件")

        val readImage = storage.read(StorageArea.CACHE, imageCachePath)
        assertEquals(dummyImageData.size, readImage.size)

        val meta = storage.metadata(StorageArea.CACHE, imageCachePath)
        assertNotNull(meta)
        assertEquals(dummyImageData.size.toLong(), meta.size)

        // 清理
        storage.delete(StorageArea.CACHE, imageCachePath)
        storage.delete(StorageArea.CACHE, videoCachePath)
        assertFalse(storage.exists(StorageArea.CACHE, imageCachePath))
        assertFalse(storage.exists(StorageArea.CACHE, videoCachePath))
    }

    @Test
    fun testCacheListAndBatchDelete() = runTest {
        val path1 = StoragePath("feed_cache/feed_page_1.json")
        val path2 = StoragePath("feed_cache/feed_page_2.json")

        storage.write(StorageArea.CACHE, path1, "{\"page\":1}".encodeToByteArray(), WriteMode.ATOMIC)
        storage.write(StorageArea.CACHE, path2, "{\"page\":2}".encodeToByteArray(), WriteMode.ATOMIC)

        val cachedFiles = storage.list(StorageArea.CACHE, StoragePath("feed_cache"))
        assertTrue(cachedFiles.size >= 2, "列出 feed_cache 目录下的文件数量应 >= 2")

        storage.delete(StorageArea.CACHE, path1)
        storage.delete(StorageArea.CACHE, path2)
    }
}
