/**
 * @File: AppVideoCacheManagerTest.kt
 * @Package: org.example.project.ui.core.video
 * @Description: AppVideoCacheManager 视频离线缓存、本地路径直通、磁盘缓存命中与清理单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.core.video

import kotlinx.coroutines.test.runTest
import org.example.project.core.storage.api.StorageArea
import org.example.project.core.storage.api.StoragePath
import org.example.project.core.storage.api.WriteMode
import org.example.project.core.storage.client.AppStorageInitializer
import org.example.project.core.storage.client.StorageContainer
import org.example.project.core.storage.platform.StorageDirectories
import org.example.project.core.storage.testing.FakeFileStorage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppVideoCacheManagerTest {

    private class MockTestStorageDirectories : StorageDirectories {
        override val persistent: String = "build/test_storage/persistent"
        override val cache: String = "build/test_storage/cache"
        override val temporary: String = "build/test_storage/temp"
    }

    private val testDirectories = MockTestStorageDirectories()
    private val testStorage = FakeFileStorage()

    @BeforeTest
    fun setUp() {
        AppStorageInitializer.resetForTesting()
        AppStorageInitializer.initForTesting(object : StorageContainer {
            override val fileStorage = testStorage
            override val directories = testDirectories
        })
    }

    @AfterTest
    fun tearDown() = runTest {
        AppVideoCacheManager.clearVideoCache()
        AppStorageInitializer.resetForTesting()
    }

    @Test
    fun testLocalVideoPathReturnsImmediatelyWithoutCacheLookup() = runTest {
        val localPath = "/data/user/0/org.example.project/files/feedline/media/test_video.mp4"
        val resolved = AppVideoCacheManager.getPlayableVideoUrl(localPath)
        assertEquals(localPath, resolved, "本地文件物理路径应直接直通返回")

        val isCached = AppVideoCacheManager.isVideoCached(localPath)
        assertTrue(isCached, "本地文件默认视作已缓存")
    }

    @Test
    fun testRemoteVideoUrlCacheMissReturnsOriginalUrl() = runTest {
        val remoteUrl = "https://example.com/videos/social_stream_001.mp4"
        val resolved = AppVideoCacheManager.getPlayableVideoUrl(remoteUrl)
        assertEquals(remoteUrl, resolved, "未缓存的网络视频应先返回原始网络流保障首播")
    }

    @Test
    fun testRemoteVideoUrlCacheHitReturnsLocalPath() = runTest {
        val remoteUrl = "https://example.com/videos/trending_reels_2026.mp4"
        val hash = remoteUrl.hashCode().toUInt().toString(16)
        val expectedRelativePath = StoragePath("video_cache/video_${hash}.mp4")

        // 模拟提前将视频流写入磁盘缓存
        val dummyVideoBytes = byteArrayOf(0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70) // mp4 ftyp header
        testStorage.write(StorageArea.CACHE, expectedRelativePath, dummyVideoBytes, WriteMode.ATOMIC)

        assertTrue(AppVideoCacheManager.isVideoCached(remoteUrl), "写入缓存后应判定为已缓存")

        val resolved = AppVideoCacheManager.getPlayableVideoUrl(remoteUrl)
        val expectedAbsolutePath = "${testDirectories.cache}/video_cache/video_${hash}.mp4"
        assertEquals(expectedAbsolutePath, resolved, "已缓存的网络视频应直接交付本地沙盒物理路径实现0ms离线秒开")
    }

    @Test
    fun testClearVideoCacheRemovesAllCachedFiles() = runTest {
        val remoteUrl1 = "https://example.com/video1.mp4"
        val remoteUrl2 = "https://example.com/video2.mp4"
        val hash1 = remoteUrl1.hashCode().toUInt().toString(16)
        val hash2 = remoteUrl2.hashCode().toUInt().toString(16)

        testStorage.write(StorageArea.CACHE, StoragePath("video_cache/video_${hash1}.mp4"), byteArrayOf(1, 2, 3), WriteMode.ATOMIC)
        testStorage.write(StorageArea.CACHE, StoragePath("video_cache/video_${hash2}.mp4"), byteArrayOf(4, 5, 6), WriteMode.ATOMIC)

        assertTrue(AppVideoCacheManager.isVideoCached(remoteUrl1))
        assertTrue(AppVideoCacheManager.isVideoCached(remoteUrl2))

        AppVideoCacheManager.clearVideoCache()

        assertFalse(AppVideoCacheManager.isVideoCached(remoteUrl1), "清理后视频1应不存在")
        assertFalse(AppVideoCacheManager.isVideoCached(remoteUrl2), "清理后视频2应不存在")
    }
}
