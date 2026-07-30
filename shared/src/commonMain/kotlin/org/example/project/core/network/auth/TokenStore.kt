/**
 * @File: TokenStore.kt
 * @Package: org.example.project.core.network.auth
 * @Description: Token凭据本地存储与管理组件
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.auth

/**
 * 访问与刷新Token数据模型
 * 
 * @param accessToken 业务接口鉴权使用的Bearer Token
 * @param refreshToken 用于在AccessToken过期时换取新Token的凭据
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

/**
 * [TokenStore]
 * 负责AccessToken与RefreshToken在本地SecureStorage中的存取操作。
 * 
 * @param secureStorage 跨平台安全存储组件
 */
class TokenStore(
    private val secureStorage: SecureStorage
) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
    }

    /**
     * 读取本地保存的凭据
     * 架构约束：只能读取本地安全存储，禁止发起网络请求。
     * 
     * @return 若凭据存在则返回 AuthTokens，否则返回 null
     */
    suspend fun loadTokens(): AuthTokens? {
        val accessToken = secureStorage.getString(KEY_ACCESS_TOKEN)
        val refreshToken = secureStorage.getString(KEY_REFRESH_TOKEN)
        return if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
            AuthTokens(accessToken, refreshToken)
        } else {
            null
        }
    }

    /**
     * 保存新的凭据到安全存储
     * 
     * @param tokens 新的凭据组合
     */
    suspend fun saveTokens(tokens: AuthTokens) {
        secureStorage.putString(KEY_ACCESS_TOKEN, tokens.accessToken)
        secureStorage.putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
    }

    /**
     * 清除本地存储的凭据（用于退出登录或会话完全失效场景）
     */
    suspend fun clearTokens() {
        secureStorage.remove(KEY_ACCESS_TOKEN)
        secureStorage.remove(KEY_REFRESH_TOKEN)
    }
}
