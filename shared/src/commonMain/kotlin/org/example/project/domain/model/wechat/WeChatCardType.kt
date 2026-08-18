/**
 * @File: WeChatCardType.kt
 * @Package: org.example.project.domain.model.wechat
 * @Description: 微信公众号信息流卡片展示类型枚举
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.domain.model.wechat

import kotlinx.serialization.Serializable

/**
 * 微信公众号文章卡片展示形态
 */
@Serializable
enum class WeChatCardType {
    /** 单列置顶大图横幅卡片 (如CSDN极客头条) */
    FEATURED_BANNER,

    /** 单列左右排版卡片 (左侧文本右侧方图，如腾讯招聘、Grok4.7) */
    HORIZONTAL_ROW,

    /** 单列通栏大图卡片 (上方标题中部通栏大图，如EMS录取通知书) */
    BANNER_LARGE,

    /** 双列瀑布流大图卡片 (垂直高图封面+底部两行标题，如天津大学、Qwen3.8) */
    WATERFALL_GRID,

    /** 带有视频播放标志的双列瀑布流卡片 (如丽水乡镇) */
    VIDEO_CARD
}
