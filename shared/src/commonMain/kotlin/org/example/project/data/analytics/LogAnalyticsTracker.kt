package org.example.project.data.analytics

import org.example.project.domain.analytics.AnalyticsTracker

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

