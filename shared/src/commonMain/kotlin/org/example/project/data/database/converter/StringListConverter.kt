/**
 * @File: StringListConverter.kt
 * @Package: org.example.project.data.database.converter
 * @Description: Room数据库List<String>类型转换器
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.data.database.converter

import androidx.room.TypeConverter

class StringListConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        return value.split(";;;")
    }

    @TypeConverter
    fun toString(list: List<String>): String {
        return list.joinToString(";;;")
    }
}
