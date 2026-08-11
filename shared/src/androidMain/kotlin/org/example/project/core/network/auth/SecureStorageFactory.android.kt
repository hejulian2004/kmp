/**
 * @File: SecureStorageFactory.android.kt
 * @Package: org.example.project.core.network.auth
 * @Description: Android平台SecureStorage工厂函数actual实现
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.auth

import android.content.Context

/**
 * Android平台创建SecureStorage的actual实现
 * 
 * @param context必须传入Android Context
 */
actual fun createPlatformSecureStorage(context: Any?): SecureStorage {
    require(context is Context) {
        "Android平台的createPlatformSecureStorage必须传入非空的android.content.Context对象"
    }
    return AndroidSecureStorage(context)
}
