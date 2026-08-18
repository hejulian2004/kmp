/**
 * @File: FileSystemDriver.kt
 * @Package: org.example.project.core.storage.internal
 * @Description: 底层文件系统驱动接口与 kotlinx-io 驱动实现
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.storage.internal

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.example.project.core.storage.api.StorageError
import org.example.project.core.storage.api.StorageException
import org.example.project.core.storage.api.StorageMetadata
import org.example.project.core.storage.api.WriteMode

/**
 * 内部底层文件系统抽象驱动契约。
 */
internal interface FileSystemDriver {

    suspend fun write(
        path: Path,
        data: ByteArray,
        mode: WriteMode
    )

    suspend fun read(
        path: Path
    ): ByteArray

    suspend fun exists(
        path: Path
    ): Boolean

    suspend fun delete(
        path: Path
    ): Boolean

    suspend fun metadata(
        path: Path
    ): StorageMetadata?

    suspend fun list(
        path: Path
    ): List<Path>

    suspend fun createDirectories(
        path: Path
    )

    suspend fun atomicMove(
        source: Path,
        destination: Path
    )

    suspend fun copyStream(
        source: Path,
        destination: Path,
        bufferSize: Int = 64 * 1024
    )

    suspend fun copyStream(
        source: Source,
        destination: Path,
        bufferSize: Int = 64 * 1024
    )
}

/**
 * 基于 kotlinx-io (SystemFileSystem) 实现的物理文件驱动。
 */
internal class KotlinxIoFileSystemDriver : FileSystemDriver {

    override suspend fun copyStream(
        source: Path,
        destination: Path,
        bufferSize: Int
    ) {
        try {
            if (!SystemFileSystem.exists(source)) {
                throw StorageException(StorageError.NotFound, "Source file not found: $source")
            }
            val src = SystemFileSystem.source(source).buffered()
            src.use { input ->
                copyStream(input, destination, bufferSize)
            }
        } catch (e: StorageException) {
            throw e
        } catch (e: Exception) {
            throw mapIoException("copyStream", "$source -> $destination", e)
        }
    }

    override suspend fun copyStream(
        source: Source,
        destination: Path,
        bufferSize: Int
    ) {
        try {
            ensureParentDirectoriesExist(destination)
            val dst = SystemFileSystem.sink(destination, append = false).buffered()
            dst.use { output ->
                val buffer = ByteArray(bufferSize)
                while (true) {
                    val bytesRead = source.readAtMostTo(buffer)
                    if (bytesRead <= 0) break
                    output.write(buffer, 0, bytesRead)
                }
                output.flush()
            }
        } catch (e: StorageException) {
            throw e
        } catch (e: Exception) {
            throw mapIoException("copyStream", "stream -> $destination", e)
        }
    }

    override suspend fun write(
        path: Path,
        data: ByteArray,
        mode: WriteMode
    ) {
        try {
            ensureParentDirectoriesExist(path)
            val append = mode == WriteMode.APPEND
            val sink = SystemFileSystem.sink(path, append = append).buffered()
            sink.use {
                it.write(data)
                it.flush()
            }
        } catch (e: Exception) {
            throw mapIoException("write", path.toString(), e)
        }
    }

    override suspend fun read(path: Path): ByteArray {
        if (!SystemFileSystem.exists(path)) {
            throw StorageException(StorageError.NotFound, "File not found: $path")
        }
        return try {
            val source = SystemFileSystem.source(path).buffered()
            source.use {
                it.readByteArray()
            }
        } catch (e: StorageException) {
            throw e
        } catch (e: Exception) {
            throw mapIoException("read", path.toString(), e)
        }
    }

    override suspend fun exists(path: Path): Boolean {
        return try {
            SystemFileSystem.exists(path)
        } catch (e: Exception) {
            throw mapIoException("exists", path.toString(), e)
        }
    }

    override suspend fun delete(path: Path): Boolean {
        return try {
            if (!SystemFileSystem.exists(path)) {
                return false
            }
            deleteRecursively(path)
            true
        } catch (e: Exception) {
            throw mapIoException("delete", path.toString(), e)
        }
    }

    private fun deleteRecursively(path: Path) {
        val rawMeta = SystemFileSystem.metadataOrNull(path) ?: return
        if (rawMeta.isDirectory) {
            val children = SystemFileSystem.list(path)
            for (child in children) {
                deleteRecursively(child)
            }
        }
        SystemFileSystem.delete(path, mustExist = false)
    }

    override suspend fun metadata(path: Path): StorageMetadata? {
        return try {
            if (!SystemFileSystem.exists(path)) {
                return null
            }
            val rawMeta = SystemFileSystem.metadataOrNull(path) ?: return null
            StorageMetadata(
                size = rawMeta.size,
                lastModifiedAt = null,
                isDirectory = rawMeta.isDirectory
            )
        } catch (e: Exception) {
            throw mapIoException("metadata", path.toString(), e)
        }
    }

    override suspend fun list(path: Path): List<Path> {
        return try {
            if (!SystemFileSystem.exists(path)) {
                return emptyList()
            }
            SystemFileSystem.list(path).toList()
        } catch (e: Exception) {
            throw mapIoException("list", path.toString(), e)
        }
    }

    override suspend fun createDirectories(path: Path) {
        try {
            if (!SystemFileSystem.exists(path)) {
                SystemFileSystem.createDirectories(path, mustCreate = false)
            }
        } catch (e: Exception) {
            throw mapIoException("createDirectories", path.toString(), e)
        }
    }

    override suspend fun atomicMove(source: Path, destination: Path) {
        try {
            ensureParentDirectoriesExist(destination)
            SystemFileSystem.atomicMove(source, destination)
        } catch (e: Exception) {
            throw mapIoException("atomicMove", "$source -> $destination", e)
        }
    }

    private fun ensureParentDirectoriesExist(path: Path) {
        val parent = path.parent
        if (parent != null && !SystemFileSystem.exists(parent)) {
            SystemFileSystem.createDirectories(parent, mustCreate = false)
        }
    }

    private fun mapIoException(op: String, pathStr: String, cause: Exception): StorageException {
        val message = cause.message ?: ""
        val error = when {
            message.contains("Permission denied", ignoreCase = true) -> StorageError.PermissionDenied
            message.contains("No space left", ignoreCase = true) -> StorageError.NoSpaceLeft
            message.contains("File not found", ignoreCase = true) || message.contains("NoSuchFile", ignoreCase = true) -> StorageError.NotFound
            else -> StorageError.IoError("Operation [$op] failed on path [$pathStr]: ${cause.message}")
        }
        return StorageException(error, cause = cause)
    }
}
