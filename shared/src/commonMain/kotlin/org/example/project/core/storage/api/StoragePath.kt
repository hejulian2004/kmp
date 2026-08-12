/**
 * @File: StoragePath.kt
 * @Package: org.example.project.core.storage.api
 * @Description: 逻辑存储相对路径封装值类，禁止传递绝对路径
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage.api

import kotlin.jvm.JvmInline

/**
 * 逻辑存储相对路径值类。
 * 
 * @property value 内部相对路径字符串 (如 "feedline/images/avatar.jpg")
 */
@JvmInline
value class StoragePath(
    val value: String
) {
    override fun toString(): String = value
}
