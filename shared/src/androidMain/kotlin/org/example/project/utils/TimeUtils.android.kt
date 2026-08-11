package org.example.project.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun formatPlatformTime(timeMillis: Long, pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(timeMillis))
}
