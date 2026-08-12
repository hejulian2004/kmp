/**
 * @File: FakeFileStorage.kt
 * @Package: org.example.project.core.storage.testing
 * @Description: 内存 Map 实现的 FakeFileStorage，供 Repository 与 Domain 单元测试消费
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage.testing

import org.example.project.core.storage.api.FileStorage
import org.example.project.core.storage.api.StorageArea
import org.example.project.core.storage.api.StorageError
import org.example.project.core.storage.api.StorageException
import org.example.project.core.storage.api.StorageFile
import org.example.project.core.storage.api.StorageMetadata
import org.example.project.core.storage.api.StoragePath
import org.example.project.core.storage.api.WriteMode
import org.example.project.core.storage.internal.StoragePathValidator

/**
 * 内存 Map Fake 存储实现类，专门用于单元测试。
 */
class FakeFileStorage : FileStorage {

    private data class Key(
        val area: StorageArea,
        val pathStr: String
    )

    private data class StoredItem(
        val data: ByteArray,
        val metadata: StorageMetadata
    )

    private val storageMap = mutableMapOf<Key, StoredItem>()

    override suspend fun write(
        area: StorageArea,
        path: StoragePath,
        data: ByteArray,
        mode: WriteMode
    ) {
        val pathStr = StoragePathValidator.validateAndNormalize(path)
        val key = Key(area, pathStr)

        val finalBytes = when (mode) {
            WriteMode.ATOMIC, WriteMode.OVERWRITE -> data
            WriteMode.APPEND -> {
                val existing = storageMap[key]?.data ?: byteArrayOf()
                existing + data
            }
        }

        val meta = StorageMetadata(
            size = finalBytes.size.toLong(),
            lastModifiedAt = 1000L,
            isDirectory = false
        )

        storageMap[key] = StoredItem(finalBytes, meta)
    }

    override suspend fun read(area: StorageArea, path: StoragePath): ByteArray {
        val pathStr = StoragePathValidator.validateAndNormalize(path)
        val key = Key(area, pathStr)
        val item = storageMap[key] ?: throw StorageException(StorageError.NotFound, "File not found: $pathStr")
        return item.data
    }

    override suspend fun exists(area: StorageArea, path: StoragePath): Boolean {
        val pathStr = StoragePathValidator.validateAndNormalize(path)
        val key = Key(area, pathStr)
        return storageMap.containsKey(key)
    }

    override suspend fun delete(area: StorageArea, path: StoragePath): Boolean {
        val pathStr = StoragePathValidator.validateAndNormalize(path)
        val key = Key(area, pathStr)
        val removed = storageMap.remove(key)
        return removed != null
    }

    override suspend fun metadata(area: StorageArea, path: StoragePath): StorageMetadata? {
        val pathStr = StoragePathValidator.validateAndNormalize(path)
        val key = Key(area, pathStr)
        return storageMap[key]?.metadata
    }

    override suspend fun list(area: StorageArea, directory: StoragePath): List<StorageFile> {
        val dirStr = StoragePathValidator.validateAndNormalize(directory)
        val prefix = if (dirStr.isEmpty()) "" else "$dirStr/"

        return storageMap.entries
            .filter { (key, _) -> key.area == area && key.pathStr.startsWith(prefix) }
            .map { (key, item) ->
                StorageFile(
                    path = StoragePath(key.pathStr),
                    metadata = item.metadata
                )
            }
    }

    override suspend fun clear(area: StorageArea) {
        storageMap.keys.removeAll { it.area == area }
    }
}
