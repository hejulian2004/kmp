/**
 * @File: StorageContainer.kt
 * @Package: org.example.project.core.storage.client
 * @Description: 存储依赖容器契约与默认实现
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.storage.client

import org.example.project.core.storage.api.FileStorage
import org.example.project.core.storage.platform.StorageDirectories

/**
 * 存储依赖容器接口。
 */
interface StorageContainer {

    /** 全局统一文件存储实例 */
    val fileStorage: FileStorage

    /** 平台物理根目录映射 */
    val directories: StorageDirectories
}

/**
 * 默认存储依赖容器实现类。
 */
class DefaultStorageContainer(
    override val fileStorage: FileStorage,
    override val directories: StorageDirectories
) : StorageContainer
