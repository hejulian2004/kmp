/**
 * @File: AppStorageInitializer.kt
 * @Package: org.example.project.core.storage.client
 * @Description: 应用文件存储统一单例初始化器
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.storage.client

import kotlin.concurrent.Volatile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.project.core.storage.internal.DefaultFileStorage
import org.example.project.core.storage.platform.createPlatformStorageDirectories

/**
 * 应用文件存储统一单例初始化器。
 */
object AppStorageInitializer {

    @Volatile
    private var _container: StorageContainer? = null
    private val initMutex = Mutex()

    /**
     * 获取存储架构依赖容器实例。
     * 
     * @throws IllegalStateException 若存储架构尚未初始化抛出强断言
     */
    val container: StorageContainer
        get() = checkNotNull(_container) {
            "Storage 尚未初始化！必须在应用冷启动阶段显式调用 AppStorageInitializer.init(context) 方可使用。"
        }

    /**
     * 判断存储架构是否已完成初始化。
     */
    val isInitialized: Boolean
        get() = _container != null

    /**
     * 线程与协程安全且 exactly-once 的显式初始化入口。
     * 
     * @param context 平台上下文 (Android 平台需传入 ApplicationContext)
     */
    suspend fun init(context: Any?) {
        if (_container != null) return
        initMutex.withLock {
            if (_container != null) return@withLock
            val directories = createPlatformStorageDirectories(context)
            val fileStorage = DefaultFileStorage(directories = directories)
            _container = DefaultStorageContainer(
                fileStorage = fileStorage,
                directories = directories
            )
        }
    }

    /**
     * 仅供单元测试重置初始化状态使用。
     */
    internal fun resetForTesting() {
        _container = null
    }
}
