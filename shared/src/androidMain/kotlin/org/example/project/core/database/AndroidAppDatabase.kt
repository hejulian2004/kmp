/**
 * @File: AndroidAppDatabase.kt
 * @Package: org.example.project.core.database
 * @Description: Android 平台特定 Room Database 派生实现
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.core.database

actual fun createDefaultAppDatabase(): AppDatabase = AndroidAppDatabase()

open class AndroidAppDatabase : BaseDefaultAppDatabase() {
    override fun clearAllTables() {
    }
}
