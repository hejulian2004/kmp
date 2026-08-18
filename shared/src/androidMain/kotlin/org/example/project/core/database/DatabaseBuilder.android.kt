/**
 * @File: DatabaseBuilder.android.kt
 * @Package: org.example.project.core.database
 * @Description: Android平台Room KMP/SQLite数据库构建实现
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.database

import android.content.Context
import android.content.ContextWrapper
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

private class TestContext : ContextWrapper(null) {
    override fun getApplicationContext(): Context = this
    override fun getDatabasePath(name: String): File {
        val file = File(name)
        file.parentFile?.mkdirs()
        return file
    }
    override fun getSystemService(name: String): Any? = null
    override fun getSystemServiceName(serviceClass: Class<*>): String? = null
}

fun getAndroidDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("app_database.db")
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

actual fun getRoomDatabase(context: Any?): AppDatabase {
    val androidContext = context as? Context
        ?: try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val method = activityThreadClass.getMethod("currentApplication")
            method.invoke(null) as? Context
        } catch (_: Throwable) {
            null
        }
        ?: TestContext()

    val dbFile = androidContext.getDatabasePath("app_database.db")
    dbFile.parentFile?.mkdirs()
    return RealSqliteAppDatabase(dbFile.absolutePath)
}

actual fun getTestDatabasePath(dbName: String): String {
    val tempDir = System.getProperty("java.io.tmpdir") ?: "."
    return "$tempDir/$dbName.db"
}

actual fun getTestDatabaseBuilder(dbPath: String): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder<AppDatabase>(
        context = TestContext(),
        name = dbPath
    )
}
