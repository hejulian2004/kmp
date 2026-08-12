/**
 * @File: StorageDirectories.kt
 * @Package: org.example.project.core.storage.platform
 * @Description: 跨平台基础存储物理目录映射接口与工厂函数声明
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage.platform

/**
 * 平台存储物理根目录映射契约。
 */
interface StorageDirectories {

    /** PERSISTENT 持久数据物理根目录绝对路径 */
    val persistent: String

    /** CACHE 缓存数据物理根目录绝对路径 */
    val cache: String

    /** TEMPORARY 临时文件物理根目录绝对路径 */
    val temporary: String
}

/**
 * 工厂函数：构建特定平台的 StorageDirectories 物理目录映射实例。
 * 
 * @param context 宿主平台上下文 (Android 必须传入 ApplicationContext，iOS 可传 null)
 */
expect fun createPlatformStorageDirectories(context: Any? = null): StorageDirectories
