/**
 * @File: StoragePathResolver.kt
 * @Package: org.example.project.core.storage.internal
 * @Description: 逻辑存储区域与相对路径至物理绝对路径解析器
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.storage.internal

import kotlinx.io.files.Path
import org.example.project.core.storage.api.StorageArea
import org.example.project.core.storage.api.StoragePath
import org.example.project.core.storage.platform.StorageDirectories

/**
 * 逻辑路径解析器。
 * 
 * 将 (StorageArea, StoragePath) 组合映射为平台物理绝对路径 Path。
 */
class StoragePathResolver(
    private val directories: StorageDirectories
) {

    /**
     * 获取指定逻辑存储区域的物理根目录路径。
     */
    fun resolveAreaRoot(area: StorageArea): Path {
        val rootPathString = when (area) {
            StorageArea.PERSISTENT -> directories.persistent
            StorageArea.CACHE -> directories.cache
            StorageArea.TEMPORARY -> directories.temporary
        }
        return Path(rootPathString)
    }

    /**
     * 解析逻辑相对路径至物理绝对路径。
     * 
     * @param area 逻辑存储区域
     * @param path 逻辑相对路径
     * @param allowEmpty 是否允许空路径
     * @return 对应的 kotlinx.io.files.Path 绝对路径
     */
    fun resolve(area: StorageArea, path: StoragePath, allowEmpty: Boolean = false): Path {
        val normalizedRelativePath = StoragePathValidator.validateAndNormalize(path, allowEmpty = allowEmpty)
        val areaRoot = resolveAreaRoot(area)
        return if (normalizedRelativePath.isEmpty()) {
            areaRoot
        } else {
            Path(areaRoot, normalizedRelativePath)
        }
    }
}
