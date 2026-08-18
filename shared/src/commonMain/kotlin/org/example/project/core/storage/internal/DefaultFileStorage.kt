/**
 * @File: DefaultFileStorage.kt
 * @Package: org.example.project.core.storage.internal
 * @Description: 应用统一文件存储 Core 核心实现类 (Storage V1 Area-Level 并发模型)
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage.internal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import org.example.project.core.storage.api.FileStorage
import org.example.project.core.storage.api.StorageArea
import org.example.project.core.storage.api.StorageError
import org.example.project.core.storage.api.StorageException
import org.example.project.core.storage.api.StorageFile
import org.example.project.core.storage.api.StorageMetadata
import org.example.project.core.storage.api.StoragePath
import org.example.project.core.storage.api.WriteMode
import org.example.project.core.storage.platform.StorageDirectories

/**
 * 应用统一文件存储核心实现类 (Storage V1 区域级互斥并发模型)。
 * 
 * 职责：
 * 1. 隔离底层平台物理文件系统差异。
 * 2. 防范目录穿越与路径逃逸。
 * 3. 强制在 Dispatchers.IO 调度器下执行。
 * 4. 统一在 StorageArea 区域 Mutex 互斥下控制并发操作。
 * 5. 实现基于临时写与重命名的原子写 (Atomic Write)。
 * 
 * @param directories 平台存储物理根目录映射
 * @param driver 底层物理文件驱动 (默认使用 kotlinx-io)
 */
class DefaultFileStorage internal constructor(
    private val directories: StorageDirectories,
    private val driver: FileSystemDriver = KotlinxIoFileSystemDriver()
) : FileStorage {

    private val resolver = StoragePathResolver(directories)
    private val areaMutexes = StorageArea.entries.associateWith { Mutex() }

    override suspend fun write(
        area: StorageArea,
        path: StoragePath,
        data: ByteArray,
        mode: WriteMode
    ): Unit = withContext(Dispatchers.IO) {
        val targetPath = resolver.resolve(area, path, allowEmpty = false)
        areaMutexes.getValue(area).withLock {
            when (mode) {
                WriteMode.ATOMIC -> executeAtomicWrite(targetPath, data)
                WriteMode.OVERWRITE, WriteMode.APPEND -> driver.write(targetPath, data, mode)
            }
        }
    }

    override suspend fun read(
        area: StorageArea,
        path: StoragePath
    ): ByteArray = withContext(Dispatchers.IO) {
        val targetPath = resolver.resolve(area, path, allowEmpty = false)
        areaMutexes.getValue(area).withLock {
            driver.read(targetPath)
        }
    }

    override suspend fun exists(
        area: StorageArea,
        path: StoragePath
    ): Boolean = withContext(Dispatchers.IO) {
        val targetPath = resolver.resolve(area, path, allowEmpty = false)
        areaMutexes.getValue(area).withLock {
            driver.exists(targetPath)
        }
    }

    override suspend fun delete(
        area: StorageArea,
        path: StoragePath
    ): Boolean = withContext(Dispatchers.IO) {
        val targetPath = resolver.resolve(area, path, allowEmpty = false)
        areaMutexes.getValue(area).withLock {
            driver.delete(targetPath)
        }
    }

    override suspend fun metadata(
        area: StorageArea,
        path: StoragePath
    ): StorageMetadata? = withContext(Dispatchers.IO) {
        val targetPath = resolver.resolve(area, path, allowEmpty = false)
        areaMutexes.getValue(area).withLock {
            driver.metadata(targetPath)
        }
    }

    override suspend fun list(
        area: StorageArea,
        directory: StoragePath
    ): List<StorageFile> = withContext(Dispatchers.IO) {
        val dirPath = resolver.resolve(area, directory, allowEmpty = true)
        val areaRootPathStr = resolver.resolveAreaRoot(area).toString()
        areaMutexes.getValue(area).withLock {
            val childPaths = driver.list(dirPath)
            childPaths.mapNotNull { childPath ->
                val childPathStr = childPath.toString()
                val relativeStr = if (childPathStr.startsWith(areaRootPathStr)) {
                    childPathStr.removePrefix(areaRootPathStr).trimStart('/', '\\')
                } else {
                    childPath.name
                }
                val meta = driver.metadata(childPath) ?: return@mapNotNull null
                StorageFile(
                    path = StoragePath(relativeStr),
                    metadata = meta
                )
            }
        }
    }

    override suspend fun clear(
        area: StorageArea
    ): Unit = withContext(Dispatchers.IO) {
        areaMutexes.getValue(area).withLock {
            val rootPath = resolver.resolveAreaRoot(area)
            val childPaths = driver.list(rootPath)
            childPaths.forEach { child ->
                driver.delete(child)
            }
        }
    }

    override suspend fun copyFile(
        area: StorageArea,
        path: StoragePath,
        sourceAbsolutePath: String,
        bufferSizeBytes: Int
    ): Unit = withContext(Dispatchers.IO) {
        val targetPath = resolver.resolve(area, path, allowEmpty = false)
        val tempPath = Path("${targetPath}${StorageConstants.TEMP_SUFFIX}")
        val sourcePath = Path(sourceAbsolutePath)

        areaMutexes.getValue(area).withLock {
            try {
                driver.copyStream(sourcePath, tempPath, bufferSizeBytes)
                driver.atomicMove(tempPath, targetPath)
            } catch (e: Exception) {
                if (driver.exists(tempPath)) {
                    driver.delete(tempPath)
                }
                if (e is StorageException) {
                    throw e
                } else {
                    throw StorageException(StorageError.IoError("Atomic stream copy failed: ${e.message}"), cause = e)
                }
            }
        }
    }

    internal suspend fun activeLockCount(): Int = 0

    /**
     * 执行原子写逻辑：
     * 1. 构建临时文件路径 targetPath.tmp
     * 2. 完整写入数据至临时文件
     * 3. 原子重命名/移动至目标物理路径
     * 4. 若过程发生异常，自动清理残留临时文件并抛出异常
     */
    private suspend fun executeAtomicWrite(targetPath: Path, data: ByteArray) {
        val tempPath = Path("${targetPath}${StorageConstants.TEMP_SUFFIX}")
        try {
            driver.write(tempPath, data, WriteMode.OVERWRITE)
            driver.atomicMove(tempPath, targetPath)
        } catch (e: Exception) {
            if (driver.exists(tempPath)) {
                driver.delete(tempPath)
            }
            if (e is StorageException) {
                throw e
            } else {
                throw StorageException(StorageError.IoError("Atomic write failed: ${e.message}"), cause = e)
            }
        }
    }
}
