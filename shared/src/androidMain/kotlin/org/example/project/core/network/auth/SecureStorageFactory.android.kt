/**
 * @File: SecureStorageFactory.android.kt
 * @Package: org.example.project.core.network.auth
 * @Description: Android平台SecureStorage工厂函数actual实现
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.core.network.auth

import android.content.Context

/**
 * Android平台创建SecureStorage的actual实现
 * 
 * @param context 平台 Context (若无 Context 环境则降级为 InMemorySecureStorage)
 */
actual fun createPlatformSecureStorage(context: Any?): SecureStorage {
    val androidContext = context as? Context
    return if (androidContext != null) {
        AndroidSecureStorage(androidContext)
    } else {
        InMemorySecureStorage()
    }
}
