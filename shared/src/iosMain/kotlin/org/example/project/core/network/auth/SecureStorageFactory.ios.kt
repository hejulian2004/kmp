/**
 * @File: SecureStorageFactory.ios.kt
 * @Package: org.example.project.core.network.auth
 * @Description: iOS平台SecureStorage工厂函数actual实现
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.auth

/**
 * iOS平台创建SecureStorage的actual实现
 * 
 * @param context iOS平台无需Context，传null即可
 */
actual fun createPlatformSecureStorage(context: Any?): SecureStorage {
    return IosSecureStorage()
}
