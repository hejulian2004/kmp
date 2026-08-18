/**
 * @File: HostProfileDaoTest.kt
 * @Package: org.example.project.data.database.dao
 * @Description: HostProfileDao 房东与房源数据 CRUD 及 Flow 监听单元测试套件
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.data.database.dao

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.project.core.database.FakeHostProfileDao
import org.example.project.data.database.entity.airbnb.HostEntity
import org.example.project.data.database.entity.airbnb.PropertyListingEntity
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HostProfileDaoTest {

    private lateinit var dao: FakeHostProfileDao

    @BeforeTest
    fun setUp() {
        dao = FakeHostProfileDao()
    }

    @Test
    fun testInsertAndObserveHosts() = runTest {
        dao.clearHosts()
        val host = HostEntity(
            id = "host_1",
            name = "John Doe",
            reviewCount = 120,
            rating = 4.95,
            yearsHosting = 5,
            totalListings = 3,
            languages = "English, Spanish",
            identityVerified = true,
            superHost = true,
            about = "Superhost in Tokyo",
            occupation = "Designer",
            livesIn = "Tokyo",
            hobbies = listOf("Photography", "Cooking"),
            places = listOf("Shibuya"),
            placesVisible = true,
            avatarUrl = "https://example.com/avatar.jpg"
        )

        dao.insertHosts(listOf(host))
        val hosts = dao.observeHosts().first()

        assertEquals(1, hosts.size)
        assertEquals("host_1", hosts.first().id)
        assertEquals("John Doe", hosts.first().name)
        assertTrue(hosts.first().superHost)
    }

    @Test
    fun testInsertAndObserveProperties() = runTest {
        dao.clearProperties()
        val prop = PropertyListingEntity(
            id = "prop_1",
            hostId = "host_1",
            title = "Cozy Apartment in Shibuya",
            subtitle = "Entire apartment",
            rating = 4.88,
            reviewCount = 45,
            imageUrl = "https://example.com/house.jpg"
        )

        dao.insertProperties(listOf(prop))
        val props = dao.observeProperties().first()

        assertEquals(1, props.size)
        assertEquals("prop_1", props.first().id)
        assertEquals("Cozy Apartment in Shibuya", props.first().title)
    }

    @Test
    fun testClearHostsAndProperties() = runTest {
        dao.clearHosts()
        val host = HostEntity(
            id = "host_clear",
            name = "Clear Host",
            reviewCount = 0,
            rating = 5.0,
            yearsHosting = 1,
            totalListings = 1,
            languages = "English",
            identityVerified = true,
            superHost = false,
            about = "",
            hobbies = emptyList(),
            avatarUrl = ""
        )

        dao.insertHosts(listOf(host))
        dao.clearHosts()

        assertEquals(0, dao.observeHosts().first().size)
    }
}
