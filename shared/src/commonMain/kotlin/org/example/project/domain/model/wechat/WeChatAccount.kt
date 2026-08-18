/**
 * @File: WeChatAccount.kt
 * @Package: org.example.project.domain.model.wechat
 * @Description: 微信公众号主体领域数据模型
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.domain.model.wechat

import kotlinx.serialization.Serializable

/**
 * 微信公众号主体模型
 *
 * @property id公众号唯一标识
 * @property name公众号名称
 * @property avatarUrl公众号头像图片地址
 * @property isFollowed是否已关注
 * @property isFrequentlyRead是否为常读公众号
 * @property hasUnread是否包含未读更新 (显示小绿点)
 * @property signature公众号简介
 */
@Serializable
data class WeChatAccount(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val isFollowed: Boolean = true,
    val isFrequentlyRead: Boolean = false,
    val hasUnread: Boolean = false,
    val signature: String = ""
)
