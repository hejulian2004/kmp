/**
 * @File: MediaPersisterAndroidTest.kt
 * @Package: org.example.project.ui.utils
 * @Description: Android PlatformFile视频流式持久化回归测试
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.test.runTest
import org.example.project.core.storage.client.AppStorageInitializer
import org.example.project.domain.model.feedline.FeedLineMedia
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MediaPersisterAndroidTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        AppStorageInitializer.resetForTesting()
    }

    @After
    fun tearDown() {
        AppStorageInitializer.resetForTesting()
    }

    @Test
    fun testVideoPersistenceUsesPlatformFileSource() = runTest {
        AppStorageInitializer.init(context)
        val expected = ByteArray(128 * 1024) { index -> (index % 251).toByte() }
        val sourceFile = File(context.cacheDir, "picked_video.mp4").apply {
            writeBytes(expected)
        }

        val result = persistPickedMedia(PlatformFile(sourceFile))
        assertTrue(result is FeedLineMedia.Video)
        val video = result as FeedLineMedia.Video

        assertTrue(video.videoUrl.startsWith(context.filesDir.absolutePath))
        assertArrayEquals(expected, File(video.videoUrl).readBytes())
    }
}
