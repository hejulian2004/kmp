/**
 * @File: AppDatabase.kt
 * @Package: org.example.project.core.database
 * @Description: 项目全局 Room KMP 本地数据库定义（包含 Airbnb、FeedLine、Instagram 模块 DAO 契约）
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.core.database

import org.example.project.data.database.dao.airbnb.HostProfileDao
import org.example.project.data.database.dao.airbnb.HostProfileDaoImpl
import org.example.project.data.database.dao.feedline.FeedLineDao
import org.example.project.data.database.dao.feedline.FeedLineDaoImpl
import org.example.project.data.database.dao.instagram.InstagramDao
import org.example.project.data.database.dao.instagram.InstagramDaoImpl

open class AppDatabase(
    private val hostProfileDaoImpl: HostProfileDao = HostProfileDaoImpl(),
    private val feedLineDaoImpl: FeedLineDao = FeedLineDaoImpl(),
    private val instagramDaoImpl: InstagramDao = InstagramDaoImpl(),
) {
    fun hostProfileDao(): HostProfileDao = hostProfileDaoImpl
    fun feedLineDao(): FeedLineDao = feedLineDaoImpl
    fun instagramDao(): InstagramDao = instagramDaoImpl
}
