package org.example.project.utils

object TimeUtils {
    fun formatTime(
        timeMillis: Long,


        pattern: String = "yyyy-MM-dd HH:mm"
    ): String {
        return formatPlatformTime(timeMillis, pattern)
    }
}

expect fun formatPlatformTime(timeMillis: Long, pattern: String): String