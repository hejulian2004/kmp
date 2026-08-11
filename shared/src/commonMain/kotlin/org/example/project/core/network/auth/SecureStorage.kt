/**
 * @File: SecureStorage.kt
 * @Package: org.example.project.core.network.auth
 * @Description: 跨平台安全存储统一接口定义（Key-Value加密存储）
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.auth

/**
 * 跨平台安全存储契约
 * 
 * 在Android平台将绑定Keystore/EncryptedSharedPreferences，
 * 在iOS平台将绑定Keychain Services。
 */
interface SecureStorage {
    /**
     * 读取指定键的安全数据
     * 
     * @param key存储键
     * @return返回对应字符串，不存在时返回null
     */
    suspend fun getString(key: String): String?

    /**
     * 写入键值对至安全存储
     * 
     * @param key存储键
     * @param value字符串值
     */
    suspend fun putString(key: String, value: String)

    /**
     * 移除指定键的安全数据
     * 
     * @param key存储键
     */
    suspend fun remove(key: String)

    /**
     * 清空所有安全存储数据
     */
    suspend fun clear()
}
