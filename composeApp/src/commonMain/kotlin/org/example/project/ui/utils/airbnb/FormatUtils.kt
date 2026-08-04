/**
 * @File: FormatUtils.kt
 * @Package: org.example.project.ui.utils.airbnb
 * @Description: Airbnb 模块数字与格式化工具函数
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.ui.utils.airbnb

fun formatDecimal(value: Double): String {
    val rounded = (value * 100).toInt() / 100.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}
