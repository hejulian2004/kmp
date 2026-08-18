/**
 * @File: DatabaseBuilder.ios.kt
 * @Package: org.example.project.core.database
 * @Description: iOS平台Room KMP数据库构建实现
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUserDomainMask

open class IosAppDatabase : BaseDefaultAppDatabase()

actual fun createDefaultAppDatabase(): AppDatabase = IosAppDatabase()

@OptIn(ExperimentalForeignApi::class)
fun getIosDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    val dbFilePath = requireNotNull(documentDirectory?.path) + "/app_database.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
        factory = { IosAppDatabase() }
    )
}

actual fun getRoomDatabase(context: Any?): AppDatabase {
    return getRoomDatabase(getIosDatabaseBuilder())
}

actual fun getTestDatabasePath(dbName: String): String {
    val tempDir = NSTemporaryDirectory()
    return "$tempDir/$dbName.db"
}

actual fun getTestDatabaseBuilder(dbPath: String): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder<AppDatabase>(
        name = dbPath,
        factory = { IosAppDatabase() }
    )
}
