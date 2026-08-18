/**
 * @File: StorageMetadata.kt
 * @Package: org.example.project.core.storage.api
 * @Description: 文件与目录基础元信息数据模型
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage.api

/**
 * 存储对象元数据模型。
 * 
 * @property size 文件字节大小
 * @property lastModifiedAt 最后修改时间戳 (毫秒)，若不可获取则为 null
 * @property isDirectory 是否为目录
 */
data class StorageMetadata(
    val size: Long,
    val lastModifiedAt: Long?,
    val isDirectory: Boolean
)
