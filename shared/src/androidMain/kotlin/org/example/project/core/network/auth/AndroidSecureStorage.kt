/**
 * @File: AndroidSecureStorage.kt
 * @Package: org.example.project.core.network.auth
 * @Description: Android平台原生EncryptedSharedPreferences加密存储实现
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [AndroidSecureStorage]
 * 基于AndroidX EncryptedSharedPreferences与KeyStore主密钥实现的安全Key-Value存储。
 * 
 * @param context Android应用上下文
 */
class AndroidSecureStorage(
    context: Context
) : SecureStorage {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_auth_tokens_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun getString(key: String): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(key, null)
    }

    override suspend fun putString(key: String, value: String): Unit = withContext(Dispatchers.IO) {
        sharedPreferences.edit { putString(key, value) }
    }

    override suspend fun remove(key: String): Unit = withContext(Dispatchers.IO) {
        sharedPreferences.edit { remove(key) }
    }

    override suspend fun clear(): Unit = withContext(Dispatchers.IO) {
        sharedPreferences.edit { clear() }
    }
}
