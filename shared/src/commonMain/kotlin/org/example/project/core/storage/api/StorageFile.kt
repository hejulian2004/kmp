/**
 * @File: StorageFile.kt
 * @Package: org.example.project.core.storage.api
 * @Description: 逻辑存储文件信息抽象数据模型
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage.api

/**
 * 逻辑存储文件与目录模型。
 * 
 * @property path 逻辑相对路径
 * @property metadata 对应的元数据信息
 */
data class StorageFile(
    val path: StoragePath,
    val metadata: StorageMetadata
)
