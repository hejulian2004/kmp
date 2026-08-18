/**
 * @File: HostProfileRepositoryImplTest.kt
 * @Package: org.example.project.data.repository.airbnb
 * @Description: HostProfileRepositoryImpl房东资料与房源数据SWR管道与磁盘持久化单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.data.repository.airbnb

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.project.core.database.FakeHostProfileDao
import org.example.project.domain.model.airbnb.Host
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HostProfileRepositoryImplTest {

    @Test
    fun testHostProfilePersistenceAndRecovery() = runTest {
        val dao1 = FakeHostProfileDao()
        val repository1 = HostProfileRepositoryImpl(dao = dao1)

        val hosts1 = repository1.getHosts()
        assertNotNull(hosts1)
        assertEquals(2, hosts1.size)

        // 模拟重启（新建 DAO 与 Repository 实例）
        val dao2 = FakeHostProfileDao()
        val repository2 = HostProfileRepositoryImpl(dao = dao2)

        val hosts2 = repository2.getHosts()
        assertNotNull(hosts2)
        assertEquals(2, hosts2.size)
        assertEquals("ArtRoomHK", hosts2.first().name)
    }

    @Test
    fun testPropertiesAndReviewsPersistence() = runTest {
        val dao1 = FakeHostProfileDao()
        val repository1 = HostProfileRepositoryImpl(dao = dao1)

        val properties1 = repository1.getProperties()
        val reviews1 = repository1.getReviews()
        val guides1 = repository1.getGuides()

        assertEquals(4, properties1.size)
        assertEquals(4, reviews1.size)
        assertEquals(2, guides1.size)

        // 模拟重启恢复验证房源与评价持久化
        val dao2 = FakeHostProfileDao()
        val repository2 = HostProfileRepositoryImpl(dao = dao2)

        val properties2 = repository2.getProperties()
        val reviews2 = repository2.getReviews()
        val guides2 = repository2.getGuides()

        assertEquals(4, properties2.size)
        assertEquals(4, reviews2.size)
        assertEquals(2, guides2.size)
    }
}
