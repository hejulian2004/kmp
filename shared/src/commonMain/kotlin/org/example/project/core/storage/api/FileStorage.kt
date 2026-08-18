/**
 * @File: FileStorage.kt
 * @Package: org.example.project.core.storage.api
 * @Description: 应用统一文件存储基础契约接口
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage.api

/**
 * 应用统一文件存储接口。
 * 
 * 所有业务模块必须通过该接口访问应用私有文件系统，
 * 禁止直接访问平台真实文件路径与 FileSystem 实例。
 */
interface FileStorage {

    /**
     * 向指定存储区域与相对路径写入二进制字节数组。
     * 
     * @param area 目标逻辑存储区域 (PERSISTENT / CACHE / TEMPORARY)
     * @param path 逻辑相对路径 (如 "sdui/feedline/layout.json")
     * @param data 写入的二进制字节数据
     * @param mode 写入模式，默认为 WriteMode.ATOMIC (先写临时文件再原子替换)
     */
    suspend fun write(
        area: StorageArea,
        path: StoragePath,
        data: ByteArray,
        mode: WriteMode = WriteMode.ATOMIC
    )

    /**
     * 读取指定存储区域与相对路径的文件字节数组。
     * 
     * @param area 目标逻辑存储区域
     * @param path 逻辑相对路径
     * @return 文件完整字节数组
     * @throws StorageException 若文件不存在或读取失败抛出
     */
    suspend fun read(
        area: StorageArea,
        path: StoragePath
    ): ByteArray

    /**
     * 判断指定存储区域与相对路径的文件或目录是否存在。
     * 
     * @param area 目标逻辑存储区域
     * @param path 逻辑相对路径
     * @return 存在返回 true，否则返回 false
     */
    suspend fun exists(
        area: StorageArea,
        path: StoragePath
    ): Boolean

    /**
     * 删除指定存储区域与相对路径的文件或目录。
     * 
     * @param area 目标逻辑存储区域
     * @param path 逻辑相对路径
     * @return 删除成功返回 true，文件不存在或删除失败返回 false
     */
    suspend fun delete(
        area: StorageArea,
        path: StoragePath
    ): Boolean

    /**
     * 获取指定存储区域与相对路径的文件或目录元数据信息。
     * 
     * @param area 目标逻辑存储区域
     * @param path 逻辑相对路径
     * @return 元数据对象，若对象不存在返回 null
     */
    suspend fun metadata(
        area: StorageArea,
        path: StoragePath
    ): StorageMetadata?

    /**
     * 列出指定存储区域与相对目录下的直属文件与子目录列表。
     * 
     * @param area 目标逻辑存储区域
     * @param directory 逻辑相对目录路径 (若传空相对路径 StoragePath("") 则表示根目录)
     * @return 匹配的文件与目录列表
     */
    suspend fun list(
        area: StorageArea,
        directory: StoragePath
    ): List<StorageFile>

    /**
     * 清空指定逻辑存储区域下的所有文件与子目录。
     * 
     * @param area 目标逻辑存储区域
     */
    suspend fun clear(
        area: StorageArea
    )

    /**
     * 将外部物理文件以流式分块（Buffer）原子复制至指定存储区域与相对路径。
     * 
     * @param area 目标逻辑存储区域
     * @param path 目标逻辑相对路径
     * @param sourceAbsolutePath 来源物理文件的绝对路径
     * @param bufferSizeBytes 复制缓冲区大小，默认 64KB
     */
    suspend fun copyFile(
        area: StorageArea,
        path: StoragePath,
        sourceAbsolutePath: String,
        bufferSizeBytes: Int = 64 * 1024
    )
}
