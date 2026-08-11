/**
 * @File: DatabaseBuilder.kt
 * @Package: org.example.project.core.database
 * @Description: Room数据库构建器跨平台与底层实例获得函数
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

fun getRoomDatabase(context: Any? = null): AppDatabase {
    return AppDatabase()
}
