/**
 * @File: FakeFileStorageTest.kt
 * @Package: org.example.project.core.storage
 * @Description: FakeFileStorage 内存 Map 存储实现类的单元测试套件
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.core.storage

import kotlinx.coroutines.test.runTest
import org.example.project.core.storage.api.StorageArea
import org.example.project.core.storage.api.StorageError
import org.example.project.core.storage.api.StorageException
import org.example.project.core.storage.api.StoragePath
import org.example.project.core.storage.api.WriteMode
import org.example.project.core.storage.testing.FakeFileStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FakeFileStorageTest {

    private val storage = FakeFileStorage()

    @Test
    fun testWriteReadDeleteInCacheArea() = runTest {
        val path = StoragePath("cache/test_file.txt")
        val content = "Fake storage text content"

        storage.write(StorageArea.CACHE, path, content.encodeToByteArray(), WriteMode.OVERWRITE)
        assertTrue(storage.exists(StorageArea.CACHE, path))

        val readData = storage.read(StorageArea.CACHE, path)
        assertEquals(content, readData.decodeToString())

        val meta = storage.metadata(StorageArea.CACHE, path)
        assertNotNull(meta)
        assertEquals(content.encodeToByteArray().size.toLong(), meta.size)

        val deleted = storage.delete(StorageArea.CACHE, path)
        assertTrue(deleted)
        assertFalse(storage.exists(StorageArea.CACHE, path))
    }

    @Test
    fun testAppendModeInFakeStorage() = runTest {
        val path = StoragePath("logs/app.log")
        storage.write(StorageArea.PERSISTENT, path, "Log Line 1\n".encodeToByteArray(), WriteMode.OVERWRITE)
        storage.write(StorageArea.PERSISTENT, path, "Log Line 2\n".encodeToByteArray(), WriteMode.APPEND)

        val text = storage.read(StorageArea.PERSISTENT, path).decodeToString()
        assertEquals("Log Line 1\nLog Line 2\n", text)
    }

    @Test
    fun testListAndClearInFakeStorage() = runTest {
        val path1 = StoragePath("tmp/f1.txt")
        val path2 = StoragePath("tmp/f2.txt")

        storage.write(StorageArea.TEMPORARY, path1, "1".encodeToByteArray(), WriteMode.OVERWRITE)
        storage.write(StorageArea.TEMPORARY, path2, "2".encodeToByteArray(), WriteMode.OVERWRITE)

        val files = storage.list(StorageArea.TEMPORARY, StoragePath("tmp"))
        assertEquals(2, files.size)

        storage.clear(StorageArea.TEMPORARY)
        assertFalse(storage.exists(StorageArea.TEMPORARY, path1))
        assertFalse(storage.exists(StorageArea.TEMPORARY, path2))
    }

    @Test
    fun testReadNonExistentThrowsStorageException() = runTest {
        val path = StoragePath("missing.bin")
        val ex = assertFailsWith<StorageException> {
            storage.read(StorageArea.PERSISTENT, path)
        }
        assertEquals(StorageError.NotFound, ex.error)
    }
}
