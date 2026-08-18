/**
 * @File: AppDatabaseInitializer.kt
 * @Package: org.example.project.core.database
 * @Description: 应用Room数据库统一生命周期初始化器
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.database

import kotlin.concurrent.Volatile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 应用生命周期内唯一的[AppDatabase]初始化入口。
 */
object AppDatabaseInitializer {
    @Volatile
    private var _database: AppDatabase? = null
    private val initMutex = Mutex()

    /**
     * 获取已初始化的数据库实例。
     *
     * @throws IllegalStateException数据库尚未初始化时抛出异常。
     */
    val database: AppDatabase
        get() = checkNotNull(_database) {
            "Database尚未初始化！必须在应用冷启动阶段显式调用AppDatabaseInitializer.init(context)方可使用。"
        }

    /**
     * [database]的容器别名，便于与其他基础设施初始化器保持一致的访问形式。
     */
    val container: AppDatabase
        get() = database

    /**
     * 判断数据库是否已经完成初始化。
     */
    val isInitialized: Boolean
        get() = _database != null

    /**
     * 显式初始化全局数据库实例。
     *
     * 重复调用会复用第一次成功创建的实例；并发调用由互斥锁串行化，保证只创建一个实例。
     * 初始化失败时不写入实例，后续调用可以重试。
     */
    suspend fun init(context: Any? = null) {
        if (_database != null) return

        initMutex.withLock {
            if (_database != null) return@withLock
            _database = getRoomDatabase(context)
        }
    }

    /**
     * 仅供单元测试重置初始化状态使用。
     */
    internal fun resetForTesting() {
        _database = null
    }
}
