/**
 * @File: StorageArea.kt
 * @Package: org.example.project.core.storage.api
 * @Description: 应用逻辑存储区域枚举定义，隔离底层平台目录差异
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage.api

/**
 * 应用逻辑存储区域枚举。
 * 
 * 业务层不得直接访问平台真实文件路径，必须通过逻辑存储区域进行访问。
 */
enum class StorageArea {

    /**
     * 长期持久数据。
     * 
     * 存储应用的核心离线数据、草稿、附件等，不应该被系统自动清理。
     */
    PERSISTENT,

    /**
     * 可重新生成的数据。
     * 
     * 存储网络缓存、图片缓存等，系统允许在磁盘空间不足时清理。
     */
    CACHE,

    /**
     * 生命周期较短的临时文件。
     * 
     * 存储上传/下载中间文件、解压临时文件等。
     */
    TEMPORARY
}
