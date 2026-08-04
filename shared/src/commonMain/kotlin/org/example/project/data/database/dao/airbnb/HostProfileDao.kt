/**
 * @File: HostProfileDao.kt
 * @Package: org.example.project.data.database.dao.airbnb
 * @Description: Airbnb房东与房源数据的Room DAO接口
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.data.database.dao.airbnb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.project.data.database.entity.airbnb.HostEntity
import org.example.project.data.database.entity.airbnb.HostReviewEntity
import org.example.project.data.database.entity.airbnb.PropertyListingEntity
import org.example.project.data.database.entity.airbnb.TravelGuideEntity

@Dao
interface HostProfileDao {
    @Query("SELECT * FROM hosts")
    fun observeHosts(): Flow<List<HostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHosts(hosts: List<HostEntity>)

    @Query("SELECT * FROM property_listings")
    fun observeProperties(): Flow<List<PropertyListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperties(properties: List<PropertyListingEntity>)

    @Query("SELECT * FROM host_reviews")
    fun observeReviews(): Flow<List<HostReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<HostReviewEntity>)

    @Query("SELECT * FROM travel_guides")
    fun observeGuides(): Flow<List<TravelGuideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuides(guides: List<TravelGuideEntity>)

    @Query("DELETE FROM hosts")
    suspend fun clearHosts()

    @Query("DELETE FROM property_listings")
    suspend fun clearProperties()

    @Query("DELETE FROM host_reviews")
    suspend fun clearReviews()

    @Query("DELETE FROM travel_guides")
    suspend fun clearGuides()
}
