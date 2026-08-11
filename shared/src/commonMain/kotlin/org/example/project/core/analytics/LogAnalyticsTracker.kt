/**
 * @File: LogAnalyticsTracker.kt
 * @Package: org.example.project.core.analytics
 * @Description: 数据上报/埋点追踪器的控制台日志打印具体实现类
 * @Author: 何聚敛
 * @Date: 2026-08-10
 */
package org.example.project.core.analytics

/**
 * 控制台/Logcat 输出的埋点追踪器实现
 */
class LogAnalyticsTracker : AnalyticsTracker {
    override fun trackEvent(eventName: String, params: Map<String, Any>) {
        val paramsString = if (params.isNotEmpty()) {
            params.entries.joinToString(prefix = "{", postfix = "}") { "${it.key}: ${it.value}" }
        } else {
            "无参数"
        }
        val message = "【数据上报】事件: $eventName | 参数: $paramsString"
        println("LogAnalyticsTracker: $message")
    }
}
