package org.example.project.data.analytics

import android.util.Log
import android.widget.Toast
import org.example.project.domain.analytics.AnalyticsTracker
import org.example.project.BaseApplication

class LogAnalyticsTracker : AnalyticsTracker {
    override fun trackEvent(eventName: String, params: Map<String, Any>) {
        val paramsString = if (params.isNotEmpty()) {
            params.entries.joinToString(prefix = "{", postfix = "}") { "${it.key}: ${it.value}" }
        } else {
            "无参数"
        }
        val message  = "【数据上报】事件: $eventName | 参数: $paramsString"
        Log.d("AnalyticsTracker", message)
        Toast.makeText(BaseApplication.context, message, Toast.LENGTH_SHORT).show()
    }
}

