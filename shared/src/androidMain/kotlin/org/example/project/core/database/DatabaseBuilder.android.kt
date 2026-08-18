/**
 * @File: DatabaseBuilder.android.kt
 * @Package: org.example.project.core.database
 * @Description: Android平台Room KMP数据库构建实现
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
}

fun getAndroidDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("app_database.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
        factory = { AndroidAppDatabase() }
    )
}

actual fun getRoomDatabase(context: Any?): AppDatabase {
    val androidContext = context as? Context
    return if (androidContext != null) {
        getRoomDatabase(getAndroidDatabaseBuilder(androidContext))
    } else {
        AndroidAppDatabase()
    }
}

actual fun getTestDatabasePath(dbName: String): String {
    val tempDir = System.getProperty("java.io.tmpdir") ?: "."
    return "$tempDir/$dbName.db"
}

actual fun getTestDatabaseBuilder(dbPath: String): RoomDatabase.Builder<AppDatabase> {
    val context = try {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val method = activityThreadClass.getMethod("currentApplication")
        (method.invoke(null) as? Context)
    } catch (_: Throwable) {
        null
    } ?: TestContext()

    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbPath,
        factory = { AndroidAppDatabase() }
    )
}
