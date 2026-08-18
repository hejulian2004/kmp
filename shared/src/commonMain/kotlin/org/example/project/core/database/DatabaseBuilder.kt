/**
 * @File: DatabaseBuilder.kt
 * @Package: org.example.project.core.database
 * @Description: Room数据库构建器跨平台与底层实例获得函数
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.database

import androidx.room.RoomDatabase

private val databaseInstance by lazy { createDefaultAppDatabase() }

/**
 * 跨平台统一获取与构建 AppDatabase 实例。
 */
fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return databaseInstance
}

/**
 * 根据平台 context 获取物理持久化数据库实例。
 */
expect fun getRoomDatabase(context: Any? = null): AppDatabase

/**
 * 获取跨平台测试临时数据库文件路径。
 */
expect fun getTestDatabasePath(dbName: String): String

/**
 * 获取跨平台测试 RoomDatabase.Builder 实例。
 */
expect fun getTestDatabaseBuilder(dbPath: String): RoomDatabase.Builder<AppDatabase>
