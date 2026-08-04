/**
 * @File: AppDatabase.kt
 * @Package: org.example.project.core.database
 * @Description: 项目全局 Room KMP 本地数据库定义
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.core.database

import org.example.project.data.database.dao.airbnb.HostProfileDao
import org.example.project.data.database.dao.airbnb.HostProfileDaoImpl

open class AppDatabase(
    private val hostProfileDaoImpl: HostProfileDao = HostProfileDaoImpl()
) {
    fun hostProfileDao(): HostProfileDao = hostProfileDaoImpl
}
