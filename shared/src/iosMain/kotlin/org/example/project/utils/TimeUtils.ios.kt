package org.example.project.utils

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun formatPlatformTime(timeMillis: Long, pattern: String): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = pattern
        locale = NSLocale.currentLocale
    }
    val date = NSDate.dateWithTimeIntervalSince1970(timeMillis / 1000.0)
    return formatter.stringFromDate(date)
}
