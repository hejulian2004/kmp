/**
 * @File: LogAnalyticsTracker.kt
 * @Package: org.example.project.data.analytics.feedline
 * @Description: 数据上报/埋点追踪器的日志输出具体实现类
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.data.analytics.feedline

import org.example.project.domain.analytics.feedline.AnalyticsTracker

class LogAnalyticsTracker : AnalyticsTracker {
    override fun trackEvent(eventName: String, params: Map<String, Any>) {
        val paramsString = if (params.isNotEmpty()) {
            params.entries.joinToString(prefix = "{", postfix = "}") { "${it.key}: ${it.value}" }
        } else {
            "无参数"
        }
        val message  = "【数据上报】事件: $eventName | 参数: $paramsString"
        println("AnalyticsTracker: $message")
    }
}