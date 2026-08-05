/**
 * @File: DatabaseBuilder.kt
 * @Package: org.example.project.core.database
 * @Description: Room 数据库构建器跨平台与底层实例获得函数
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.core.database

import org.example.project.data.database.dao.airbnb.HostProfileDaoImpl

fun getRoomDatabase(context: Any? = null): AppDatabase {
    return AppDatabase(HostProfileDaoImpl())
}
