/**
 * @File: AndroidVideoCacheTest.kt
 * @Package: org.example.project.ui.core.video
 * @Description: Android Media3视频缓存与本地数据源回归测试
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.core.video

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.content.pm.ProviderInfo
import android.content.res.AssetFileDescriptor
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DataSpec
import androidx.test.core.app.ApplicationProvider
import org.example.project.ui.components.feedline.videoUriForPlayback
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import java.io.ByteArrayOutputStream
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AndroidVideoCacheTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Before
    fun setUp() {
        AndroidVideoCache.clearCache(context)
    }

    @After
    fun tearDown() {
        AndroidVideoCache.clearCache(context)
    }

    @Test
    fun testNetworkVideoUsesMedia3SimpleCacheWith512MbLruQuota() {
        val cache = AndroidVideoCache.getCache(context)
        val factory = AndroidVideoCache.createCacheDataSourceFactory(context)
        val dataSource = factory.createDataSource()

        assertEquals(512L * 1024 * 1024, AndroidVideoCache.MAX_CACHE_SIZE_BYTES)
        assertSame(cache, factory.cache)
        assertSame(cache, dataSource.cache)

        val networkVideo = MediaItem.fromUri(Uri.parse("https://example.com/video.mp4"))
        assertEquals("https", networkVideo.localConfiguration?.uri?.scheme)
    }

    @Test
    fun testAndroidLocalAbsolutePathUsesMedia3FileDataSource() {
        val expected = byteArrayOf(0, 0, 0, 24, 0x66, 0x74, 0x79, 0x70)
        val localFile = File(context.cacheDir, "local_video.mp4").apply {
            writeBytes(expected)
        }

        val uri = videoUriForPlayback(localFile.absolutePath)
        assertEquals("file", uri.scheme)
        assertArrayEquals(expected, readThroughMedia3(uri))
    }

    @Test
    fun testContentUriUsesMedia3ContentDataSource() {
        val expected = byteArrayOf(1, 2, 3, 4, 5, 6)
        val contentFile = File(context.cacheDir, "content_video.mp4").apply {
            writeBytes(expected)
        }
        val authority = "org.example.project.testcontent.${System.nanoTime()}"
        val contentUri = Uri.parse("content://$authority/video.mp4")
        val provider = FileContentProvider(contentFile)
        provider.attachInfo(context, ProviderInfo().apply { this.authority = authority })
        ShadowContentResolver.registerProviderInternal(authority, provider)

        assertArrayEquals(expected, readThroughMedia3(videoUriForPlayback(contentUri.toString())))
    }

    private fun readThroughMedia3(uri: Uri): ByteArray {
        val dataSource = AndroidVideoCache.createCacheDataSourceFactory(context).createDataSource()
        val output = ByteArrayOutputStream()
        try {
            dataSource.open(DataSpec(uri))
            val buffer = ByteArray(1024)
            while (true) {
                val bytesRead = dataSource.read(buffer, 0, buffer.size)
                if (bytesRead == C.RESULT_END_OF_INPUT) break
                output.write(buffer, 0, bytesRead)
            }
        } finally {
            dataSource.close()
        }
        return output.toByteArray()
    }

    private class FileContentProvider(
        private val file: File
    ) : ContentProvider() {

        override fun onCreate(): Boolean = true

        override fun getType(uri: Uri): String = "video/mp4"

        override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?
        ): Cursor? = null

        override fun query(
            uri: Uri,
            projection: Array<out String>?,
            queryArgs: Bundle?,
            cancellationSignal: CancellationSignal?
        ): Cursor? = null

        override fun insert(uri: Uri, values: ContentValues?): Uri? = null

        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

        override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<out String>?
        ): Int = 0

        override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor {
            val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            return AssetFileDescriptor(descriptor, 0, file.length())
        }

        override fun openTypedAssetFile(
            uri: Uri,
            mimeTypeFilter: String,
            opts: Bundle?
        ): AssetFileDescriptor = openAssetFile(uri, "r")
    }
}
