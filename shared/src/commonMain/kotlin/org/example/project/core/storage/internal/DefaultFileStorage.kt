/**
 * @File: DefaultFileStorage.kt
 * @Package: org.example.project.core.storage.internal
 * @Description: 应用统一文件存储 Core 核心实现类
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
 * 应用统一文件存储核心实现类。
 * 
 * 职责：
 * 1. 隔离底层平台物理文件系统差异。
 * 2. 防范目录穿越与路径逃逸。
 * 3. 强制在 Dispatchers.IO 调度器下执行。
 * 4. 实现基于路径粒度的并发锁 (Path-based Mutex Lock)。
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

    /** 路径并发锁 Map */
    private val pathLocks = mutableMapOf<String, Mutex>()
    private val mapMutex = Mutex()

    override suspend fun write(
        area: StorageArea,
        path: StoragePath,
        data: ByteArray,
        mode: WriteMode
    ): Unit = withContext(Dispatchers.IO) {
        val targetPath = resolver.resolve(area, path)
        val canonicalPathKey = targetPath.toString()

        withPathLock(canonicalPathKey) {
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
        val targetPath = resolver.resolve(area, path)
        val canonicalPathKey = targetPath.toString()

        withPathLock(canonicalPathKey) {
            driver.read(targetPath)
        }
    }

    override suspend fun exists(
        area: StorageArea,
        path: StoragePath
    ): Boolean = withContext(Dispatchers.IO) {
        val targetPath = resolver.resolve(area, path)
        driver.exists(targetPath)
    }

    override suspend fun delete(
        area: StorageArea,
        path: StoragePath
    ): Boolean = withContext(Dispatchers.IO) {
        val targetPath = resolver.resolve(area, path)
        val canonicalPathKey = targetPath.toString()

        withPathLock(canonicalPathKey) {
            driver.delete(targetPath)
        }
    }

    override suspend fun metadata(
        area: StorageArea,
        path: StoragePath
    ): StorageMetadata? = withContext(Dispatchers.IO) {
        val targetPath = resolver.resolve(area, path)
        driver.metadata(targetPath)
    }

    override suspend fun list(
        area: StorageArea,
        directory: StoragePath
    ): List<StorageFile> = withContext(Dispatchers.IO) {
        val dirPath = resolver.resolve(area, directory)
        val areaRootPathStr = resolver.resolveAreaRoot(area).toString()
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

    override suspend fun clear(
        area: StorageArea
    ): Unit = withContext(Dispatchers.IO) {
        val rootPath = resolver.resolveAreaRoot(area)
        val childPaths = driver.list(rootPath)
        childPaths.forEach { child ->
            val canonicalKey = child.toString()
            withPathLock(canonicalKey) {
                driver.delete(child)
            }
        }
    }

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
            // 清理写一半残留的临时文件
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

    private suspend fun <T> withPathLock(key: String, action: suspend () -> T): T {
        val mutex = mapMutex.withLock {
            pathLocks.getOrPut(key) { Mutex() }
        }
        return mutex.withLock {
            action()
        }
    }
}
