/**
 * @File: DefaultFileStorageTest.kt
 * @Package: org.example.project.core.storage
 * @Description: DefaultFileStorage 读写、覆盖、追加、原子写与并发锁单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.storage

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.example.project.core.storage.api.StorageArea
import org.example.project.core.storage.api.StorageError
import org.example.project.core.storage.api.StorageException
import org.example.project.core.storage.api.StoragePath
import org.example.project.core.storage.api.WriteMode
import org.example.project.core.storage.internal.DefaultFileStorage
import org.example.project.core.storage.testing.TestStorageDirectories
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DefaultFileStorageTest {

    private val storage = DefaultFileStorage(directories = TestStorageDirectories())

    @Test
    fun testWriteReadAndExistsInPersistentArea() = runTest {
        val path = StoragePath("test/persistent_data.txt")
        val content = "Hello KMP Storage Infrastructure!"

        storage.write(StorageArea.PERSISTENT, path, content.encodeToByteArray(), WriteMode.ATOMIC)
        assertTrue(storage.exists(StorageArea.PERSISTENT, path))

        val readBytes = storage.read(StorageArea.PERSISTENT, path)
        assertEquals(content, readBytes.decodeToString())

        val meta = storage.metadata(StorageArea.PERSISTENT, path)
        assertNotNull(meta)
        assertEquals(content.encodeToByteArray().size.toLong(), meta.size)

        val deleted = storage.delete(StorageArea.PERSISTENT, path)
        assertTrue(deleted)
        assertFalse(storage.exists(StorageArea.PERSISTENT, path))
    }

    @Test
    fun testAppendMode() = runTest {
        val path = StoragePath("test/append_log.txt")
        storage.write(StorageArea.CACHE, path, "Line 1\n".encodeToByteArray(), WriteMode.OVERWRITE)
        storage.write(StorageArea.CACHE, path, "Line 2\n".encodeToByteArray(), WriteMode.APPEND)

        val readText = storage.read(StorageArea.CACHE, path).decodeToString()
        assertEquals("Line 1\nLine 2\n", readText)

        storage.delete(StorageArea.CACHE, path)
    }

    @Test
    fun testListAndClearInTemporaryArea() = runTest {
        val path1 = StoragePath("temp/file1.bin")
        val path2 = StoragePath("temp/file2.bin")

        storage.write(StorageArea.TEMPORARY, path1, byteArrayOf(1, 2, 3), WriteMode.ATOMIC)
        storage.write(StorageArea.TEMPORARY, path2, byteArrayOf(4, 5, 6), WriteMode.ATOMIC)

        val list = storage.list(StorageArea.TEMPORARY, StoragePath("temp"))
        assertTrue(list.size >= 2)

        storage.clear(StorageArea.TEMPORARY)
        assertFalse(storage.exists(StorageArea.TEMPORARY, path1))
        assertFalse(storage.exists(StorageArea.TEMPORARY, path2))
    }

    @Test
    fun testConcurrentWrites() = runTest {
        val path = StoragePath("test/concurrent.txt")
        val deferreds = (1..10).map { index ->
            async {
                storage.write(StorageArea.CACHE, path, "Value $index\n".encodeToByteArray(), WriteMode.OVERWRITE)
            }
        }
        deferreds.awaitAll()
        assertTrue(storage.exists(StorageArea.CACHE, path))
        storage.delete(StorageArea.CACHE, path)
    }

    @Test
    fun testReadNonExistentFileThrowsNotFound() = runTest {
        val path = StoragePath("non_existent_file.json")
        val exception = assertFailsWith<StorageException> {
            storage.read(StorageArea.PERSISTENT, path)
        }
        assertEquals(StorageError.NotFound, exception.error)
    }

    @Test
    fun testRootPathForbiddenForWriteReadDelete() = runTest {
        val emptyPath = StoragePath("")
        assertFailsWith<StorageException> {
            storage.write(StorageArea.PERSISTENT, emptyPath, "test".encodeToByteArray(), WriteMode.OVERWRITE)
        }
        assertFailsWith<StorageException> {
            storage.read(StorageArea.PERSISTENT, emptyPath)
        }
        assertFailsWith<StorageException> {
            storage.delete(StorageArea.PERSISTENT, emptyPath)
        }
    }

    @Test
    fun testPathLockMemoryReleased() = runTest {
        val path1 = StoragePath("test/lock1.txt")
        val path2 = StoragePath("test/lock2.txt")
        storage.write(StorageArea.CACHE, path1, "data1".encodeToByteArray(), WriteMode.OVERWRITE)
        storage.write(StorageArea.CACHE, path2, "data2".encodeToByteArray(), WriteMode.OVERWRITE)

        assertEquals(0, storage.activeLockCount(), "操作完成后 Path Lock 锁注册表引用计数归零，内存映射被释放")
    }

    @Test
    fun testZeroByteAndLargeFile() = runTest {
        val zeroPath = StoragePath("test/zero_byte.bin")
        storage.write(StorageArea.PERSISTENT, zeroPath, byteArrayOf(), WriteMode.OVERWRITE)
        assertTrue(storage.exists(StorageArea.PERSISTENT, zeroPath))
        val readZero = storage.read(StorageArea.PERSISTENT, zeroPath)
        assertEquals(0, readZero.size)

        val largePath = StoragePath("test/large_file.bin")
        val largeData = ByteArray(1024 * 1024) { (it % 256).toByte() }
        storage.write(StorageArea.PERSISTENT, largePath, largeData, WriteMode.ATOMIC)
        val readLarge = storage.read(StorageArea.PERSISTENT, largePath)
        assertEquals(largeData.size, readLarge.size)
        assertEquals(largeData[100], readLarge[100])
        storage.delete(StorageArea.PERSISTENT, largePath)
        storage.delete(StorageArea.PERSISTENT, zeroPath)
    }
}
