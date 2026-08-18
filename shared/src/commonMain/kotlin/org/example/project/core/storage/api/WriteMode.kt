/**
 * @File: WriteMode.kt
 * @Package: org.example.project.core.storage.api
 * @Description: 文件写入模式枚举定义
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage.api

/**
 * 文件写入模式枚举。
 */
enum class WriteMode {

    /**
     * 先写入临时文件，再原子替换目标文件 (默认推荐)。
     * 
     * 防止写入过程中进程崩溃导致文件损坏。
     */
    ATOMIC,

    /**
     * 直接覆盖写入目标文件。
     */
    OVERWRITE,

    /**
     * 追加写入目标文件末尾。
     */
    APPEND
}
