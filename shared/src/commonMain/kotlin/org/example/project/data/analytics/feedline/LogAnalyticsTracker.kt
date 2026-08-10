/**
 * @File: LogAnalyticsTracker.kt
 * @Package: org.example.project.data.analytics.feedline
 * @Description: 旧版本 LogAnalyticsTracker 向全局 core.analytics 模块的类型别名转发
 * @Author: 何聚敛
 * @Date: 2026-08-10
 */
package org.example.project.data.analytics.feedline

import org.example.project.core.analytics.LogAnalyticsTracker as CoreLogTracker

typealias LogAnalyticsTracker = CoreLogTracker