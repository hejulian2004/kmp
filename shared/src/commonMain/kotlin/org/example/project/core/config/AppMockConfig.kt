/**
 * @File: AppMockConfig.kt
 * @Package: org.example.project.core.config
 * @Description: 全局假数据与网络真实联调一键配置中心
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.config

/**
 * 业务模块枚举标识
 */
enum class MockModule {
    FEEDLINE,
    INSTAGRAM,
    AIRBNB,
    WECHAT_MP
}

/**
 * 全局假数据一键配置中心
 *
 * 用于在本地离线开发阶段使用高质量拟真数据，或在联调测试阶段一键关闭假数据以请求真实后端接口。
 */
object AppMockConfig {

    /**
     * 全局假数据总开关
     * - `true`: 开启全局假数据（默认本地离线开发模式，自动注入 Mock 种子数据并模拟网络延迟）
     * - `false`: 一键关闭全局假数据（联调/生产模式，全量请求真实后端 API 接口）
     */
    var isMockEnabled: Boolean = true

    /**
     * FeedLine (微信朋友圈) 模块独立 Mock 开关
     */
    var isFeedLineMockEnabled: Boolean = true

    /**
     * Instagram 模块独立 Mock 开关
     */
    var isInstagramMockEnabled: Boolean = true

    /**
     * Airbnb 模块独立 Mock 开关
     */
    var isAirbnbMockEnabled: Boolean = true

    /**
     * WeChatMp (微信公众号) 模块独立 Mock 开关
     */
    var isWeChatMpMockEnabled: Boolean = true

    /**
     * 模拟网络请求延迟时间 (毫秒)，在 Mock 开启时生效
     */
    var mockNetworkDelayMs: Long = 500L

    /**
     * 判断指定业务模块当前是否处于假数据生效状态
     *
     * @param module目标业务模块
     * @return若全局开关与该模块开关均开启则返回 true，否则返回 false
     */
    fun isMockActiveFor(module: MockModule): Boolean {
        if (!isMockEnabled) return false
        return when (module) {
            MockModule.FEEDLINE -> isFeedLineMockEnabled
            MockModule.INSTAGRAM -> isInstagramMockEnabled
            MockModule.AIRBNB -> isAirbnbMockEnabled
            MockModule.WECHAT_MP -> isWeChatMpMockEnabled
        }
    }

    /**
     * 一键重置为默认配置 (全模块开启 Mock，仅供单元测试恢复环境)
     */
    fun resetToDefaults() {
        isMockEnabled = true
        isFeedLineMockEnabled = true
        isInstagramMockEnabled = true
        isAirbnbMockEnabled = true
        isWeChatMpMockEnabled = true
        mockNetworkDelayMs = 500L
    }
}
