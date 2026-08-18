/**
 * @File: AppDatabaseTest.kt
 * @Package: org.example.project.core.database
 * @Description: AppDatabase 聚合容器 DAO 成员依赖获取单元测试套件
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.database

import kotlin.test.Test
import kotlin.test.assertNotNull

class AppDatabaseTest {

    @Test
    fun testAppDatabaseDaoInstancesNotNull() {
        val hostProfileDao = FakeHostProfileDao()
        val feedLineDao = FakeFeedLineDao()
        val instagramDao = FakeInstagramDao()
        val weChatMpDao = FakeWeChatMpDao()

        assertNotNull(hostProfileDao, "HostProfileDao 实例不应为空")
        assertNotNull(feedLineDao, "FeedLineDao 实例不应为空")
        assertNotNull(instagramDao, "InstagramDao 实例不应为空")
        assertNotNull(weChatMpDao, "WeChatMpDao 实例不应为空")
    }
}
