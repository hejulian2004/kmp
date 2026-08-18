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
    private val filesDirectory = File(System.getProperty("java.io.tmpdir"), "test_files").apply { mkdirs() }
    private val cacheDirectory = File(System.getProperty("java.io.tmpdir"), "test_cache").apply { mkdirs() }

    override fun getApplicationContext(): Context = this
    override fun getPackageName(): String = "org.example.project.shared.test"
    override fun getDatabasePath(name: String): File {
        val file = if (name.startsWith("/") || (name.length > 2 && name[1] == ':')) File(name) else File(filesDirectory, name)
        file.parentFile?.mkdirs()
        return file
    }
    override fun getFilesDir(): File = filesDirectory
    override fun getCacheDir(): File = cacheDirectory
    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: android.database.sqlite.SQLiteDatabase.CursorFactory?
    ): android.database.sqlite.SQLiteDatabase {
        val dbFile = getDatabasePath(name)
        return android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, factory)
    }
    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: android.database.sqlite.SQLiteDatabase.CursorFactory?,
        errorHandler: android.database.DatabaseErrorHandler?
    ): android.database.sqlite.SQLiteDatabase {
        val dbFile = getDatabasePath(name)
        return android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile.path, factory, errorHandler)
    }
    override fun getSystemService(name: String): Any? = null
    override fun getSystemServiceName(serviceClass: Class<*>): String? = null
}

private fun resolveContext(provided: Any? = null): Context {
    if (provided is Context) return provided
    return try {
        val appProviderClass = Class.forName("androidx.test.core.app.ApplicationProvider")
        val getAppMethod = appProviderClass.getMethod("getApplicationContext")
        (getAppMethod.invoke(null) as? Context) ?: TestContext()
    } catch (_: Throwable) {
        try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val method = activityThreadClass.getMethod("currentApplication")
            (method.invoke(null) as? Context) ?: TestContext()
        } catch (_: Throwable) {
            TestContext()
        }
    }
}

fun getAndroidDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext ?: context
    val dbFile = appContext.getDatabasePath("app_database.db")
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

actual fun getRoomDatabase(context: Any?): AppDatabase {
    val androidContext = resolveContext(context)
    return getRoomDatabase(getAndroidDatabaseBuilder(androidContext))
}

actual fun getTestDatabasePath(dbName: String): String {
    val tempDir = System.getProperty("java.io.tmpdir") ?: "."
    return "$tempDir/$dbName.db"
}

actual fun getTestDatabaseBuilder(dbPath: String): RoomDatabase.Builder<AppDatabase> {
    val ctx = resolveContext()
    return Room.databaseBuilder<AppDatabase>(
        context = ctx,
        name = dbPath
    )
}
