/**
 * @File: StorageConstants.kt
 * @Package: org.example.project.core.storage.internal
 * @Description: 文件存储架构内部使用的常量集合
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage.internal

internal object StorageConstants {
    /** 临时写入文件后缀名 */
    const val TEMP_SUFFIX = ".tmp"

    /** 断点下载临时文件后缀名 */
    const val PART_SUFFIX = ".part"
}
