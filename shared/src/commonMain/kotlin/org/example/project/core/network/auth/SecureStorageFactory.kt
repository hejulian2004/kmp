/**
 * @File: SecureStorageFactory.kt
 * @Package: org.example.project.core.network.auth
 * @Description: 跨平台SecureStorage工厂函数声明
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.auth

/**
 * 创建对应平台的原生加密SecureStorage实例
 * 
 * @param context Android平台传入Context(android.content.Context)，iOS平台可留空
 * @return 返回对应平台原生的SecureStorage实现
 */
expect fun createPlatformSecureStorage(context: Any? = null): SecureStorage
