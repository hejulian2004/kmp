/**
 * @File: DatabaseBuilder.kt
 * @Package: org.example.project.core.database
 * @Description: Room/SQLite数据库构建器跨平台与底层物理文件实例获取函数
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.example.project.platform.currentTimeMillis

/**
 * 跨平台根据文件路径获取真实物理 SQLite AppDatabase 实例。
 */
fun getRoomDatabase(dbPath: String): AppDatabase {
    return RealSqliteAppDatabase(dbPath)
}

/**
 * 跨平台统一通过 RoomDatabase.Builder 获取 AppDatabase 实例。
 */
fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return RealSqliteAppDatabase(getTestDatabasePath("room_${currentTimeMillis()}"))
}

/**
 * 根据平台 context 获取默认物理持久化数据库实例。
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
