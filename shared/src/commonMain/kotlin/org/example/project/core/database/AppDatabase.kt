/**
 * @File: AppDatabase.kt
 * @Package: org.example.project.core.database
 * @Description: 项目全局Room KMP本地数据库定义（包含Airbnb、FeedLine、Instagram模块DAO契约）
 * @Author: 何聚敛
 * @Date: 2026-08-05
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
