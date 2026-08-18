/**
 * @File: TestStorageDirectories.kt
 * @Package: org.example.project.core.storage.testing
 * @Description: 单元测试专用的 StorageDirectories 实现
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.core.storage.testing

import org.example.project.core.storage.platform.StorageDirectories

/**
 * 单元测试物理目录映射实现类。
 */
class TestStorageDirectories(
    basePath: String = "build/test_storage_${System.currentTimeMillis()}"
) : StorageDirectories {
    override val persistent: String = "$basePath/files"
    override val cache: String = "$basePath/cache"
    override val temporary: String = "$basePath/temp"
}
