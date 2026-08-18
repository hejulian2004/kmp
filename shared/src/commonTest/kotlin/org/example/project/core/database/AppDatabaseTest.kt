/**
 * @File: AppDatabaseTest.kt
 * @Package: org.example.project.core.database
 * @Description: AppDatabase 聚合容器 DAO 成员依赖获取单元测试套件
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.core.database

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

open class AppDatabaseTest {

    @Test
    fun testAppDatabaseDaoInstancesNotNull() {
        val dbPath = getTestDatabasePath("app_db_test_${org.example.project.platform.currentTimeMillis()}")
        val db = getRoomDatabase(getTestDatabaseBuilder(dbPath))

        val hostProfileDao = db.hostProfileDao()
        val feedLineDao = db.feedLineDao()
        val instagramDao = db.instagramDao()
        val weChatMpDao = db.weChatMpDao()

        assertNotNull(hostProfileDao, "HostProfileDao 实例不应为空")
        assertNotNull(feedLineDao, "FeedLineDao 实例不应为空")
        assertNotNull(instagramDao, "InstagramDao 实例不应为空")
        assertNotNull(weChatMpDao, "WeChatMpDao 实例不应为空")

        val className = db::class.simpleName ?: ""
        assertTrue(className.contains("AppDatabase"), "Database class should be AppDatabase generated implementation")

        db.close()
    }
}
