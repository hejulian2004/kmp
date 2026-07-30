/**
 * @File: IosSecureStorage.kt
 * @Package: org.example.project.core.network.auth
 * @Description: iOS平台原生Keychain Services加密存储实现
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.auth

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSDictionary
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * [IosSecureStorage]
 * 基于iOS Security框架Keychain Services实现的安全Key-Value存储。
 * 
 * @param serviceName Keychain服务名称标识
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosSecureStorage(
    private val serviceName: String = "org.example.project.auth"
) : SecureStorage {

    override suspend fun getString(key: String): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )
        val queryRef = CFBridgingRetain(query as NSDictionary) as CFDictionaryRef

        return try {
            memScoped {
                val resultPtr = alloc<platform.CoreFoundation.CFTypeRefVar>()
                val status = SecItemCopyMatching(queryRef, resultPtr.ptr)
                if (status == errSecSuccess) {
                    val data = CFBridgingRelease(resultPtr.value) as? NSData
                    if (data != null) {
                        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
                    } else null
                } else null
            }
        } finally {
            CFBridgingRelease(queryRef)
        }
    }

    override suspend fun putString(key: String, value: String) {
        val nsValue = NSString.create(string = value)
        val data = nsValue.dataUsingEncoding(NSUTF8StringEncoding) ?: return

        val searchQuery = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key
        )
        val searchQueryRef = CFBridgingRetain(searchQuery as NSDictionary) as CFDictionaryRef

        try {
            val status = SecItemCopyMatching(searchQueryRef, null)
            if (status == errSecSuccess) {
                // 已存在，执行更新
                val updateFields = mapOf(kSecValueData to data)
                val updateFieldsRef = CFBridgingRetain(updateFields as NSDictionary) as CFDictionaryRef
                try {
                    SecItemUpdate(searchQueryRef, updateFieldsRef)
                } finally {
                    CFBridgingRelease(updateFieldsRef)
                }
            } else {
                // 不存在，执行新增
                val addQuery = mapOf(
                    kSecClass to kSecClassGenericPassword,
                    kSecAttrService to serviceName,
                    kSecAttrAccount to key,
                    kSecValueData to data
                )
                val addQueryRef = CFBridgingRetain(addQuery as NSDictionary) as CFDictionaryRef
                try {
                    SecItemAdd(addQueryRef, null)
                } finally {
                    CFBridgingRelease(addQueryRef)
                }
            }
        } finally {
            CFBridgingRelease(searchQueryRef)
        }
    }

    override suspend fun remove(key: String) {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key
        )
        val queryRef = CFBridgingRetain(query as NSDictionary) as CFDictionaryRef
        try {
            SecItemDelete(queryRef)
        } finally {
            CFBridgingRelease(queryRef)
        }
    }

    override suspend fun clear() {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName
        )
        val queryRef = CFBridgingRetain(query as NSDictionary) as CFDictionaryRef
        try {
            SecItemDelete(queryRef)
        } finally {
            CFBridgingRelease(queryRef)
        }
    }
}
