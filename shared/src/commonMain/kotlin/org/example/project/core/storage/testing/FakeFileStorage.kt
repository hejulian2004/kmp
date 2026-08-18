/**
 * @File: FakeFileStorage.kt
 * @Package: org.example.project.core.storage.testing
 * @Description: 内存 Map 实现的 FakeFileStorage，供 Repository 与 Domain 单元测试消费
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.storage.testing

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val mapMutex = Mutex()

    override suspend fun write(
        area: StorageArea,
        path: StoragePath,
        data: ByteArray,
        mode: WriteMode
    ) {
        mapMutex.withLock {
            val pathStr = StoragePathValidator.validateAndNormalize(path, allowEmpty = false)
            val key = Key(area, pathStr)

            val inputBytes = data.copyOf()
            val finalBytes = when (mode) {
                WriteMode.ATOMIC, WriteMode.OVERWRITE -> inputBytes
                WriteMode.APPEND -> {
                    val existing = storageMap[key]?.data ?: byteArrayOf()
                    existing + inputBytes
                }
            }

            val meta = StorageMetadata(
                size = finalBytes.size.toLong(),
                lastModifiedAt = 1000L,
                isDirectory = false
            )

            storageMap[key] = StoredItem(finalBytes, meta)
        }
    }

    override suspend fun read(area: StorageArea, path: StoragePath): ByteArray = mapMutex.withLock {
        val pathStr = StoragePathValidator.validateAndNormalize(path, allowEmpty = false)
        val key = Key(area, pathStr)
        val item = storageMap[key] ?: throw StorageException(StorageError.NotFound, "File not found: $pathStr")
        item.data.copyOf()
    }

    override suspend fun exists(area: StorageArea, path: StoragePath): Boolean = mapMutex.withLock {
        val pathStr = StoragePathValidator.validateAndNormalize(path, allowEmpty = false)
        val key = Key(area, pathStr)
        storageMap.containsKey(key)
    }

    override suspend fun delete(area: StorageArea, path: StoragePath): Boolean = mapMutex.withLock {
        val pathStr = StoragePathValidator.validateAndNormalize(path, allowEmpty = false)
        val key = Key(area, pathStr)
        val removed = storageMap.remove(key)
        removed != null
    }

    override suspend fun metadata(area: StorageArea, path: StoragePath): StorageMetadata? = mapMutex.withLock {
        val pathStr = StoragePathValidator.validateAndNormalize(path, allowEmpty = false)
        val key = Key(area, pathStr)
        storageMap[key]?.metadata
    }

    override suspend fun list(area: StorageArea, directory: StoragePath): List<StorageFile> = mapMutex.withLock {
        val dirStr = StoragePathValidator.validateAndNormalize(directory, allowEmpty = true)
        val prefix = if (dirStr.isEmpty()) "" else "$dirStr/"

        val matchingEntries = storageMap.entries.filter { (key, _) ->
            key.area == area && (prefix.isEmpty() || key.pathStr.startsWith(prefix))
        }

        val directChildren = mutableMapOf<String, StorageFile>()

        for ((key, item) in matchingEntries) {
            val relativePathStr = key.pathStr.removePrefix(prefix)
            if (relativePathStr.isEmpty()) continue

            val firstSlashIndex = relativePathStr.indexOf('/')
            if (firstSlashIndex >= 0) {
                // 属于子目录项
                val dirName = relativePathStr.substring(0, firstSlashIndex)
                val fullDirRelativePath = if (prefix.isEmpty()) dirName else "$dirStr/$dirName"
                if (!directChildren.containsKey(fullDirRelativePath)) {
                    directChildren[fullDirRelativePath] = StorageFile(
                        path = StoragePath(fullDirRelativePath),
                        metadata = StorageMetadata(size = 0L, lastModifiedAt = 1000L, isDirectory = true)
                    )
                }
            } else {
                // 属于直属文件项
                val fullFileRelativePath = if (prefix.isEmpty()) relativePathStr else "$dirStr/$relativePathStr"
                directChildren[fullFileRelativePath] = StorageFile(
                    path = StoragePath(fullFileRelativePath),
                    metadata = item.metadata
                )
            }
        }

        directChildren.values.toList()
    }

    override suspend fun clear(area: StorageArea) {
        mapMutex.withLock {
            storageMap.keys.removeAll { it.area == area }
        }
    }

    override suspend fun copyFile(
        area: StorageArea,
        path: StoragePath,
        sourceAbsolutePath: String,
        bufferSizeBytes: Int
    ) {
        write(area, path, "fake_stream_copied_data".encodeToByteArray())
    }
}
