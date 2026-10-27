package com.familyshield.parent

fun formatMinutes(m: Int?): String {
    if (m == null) return "—"
    val h = m / 60
    val r = m % 60
    return if (h > 0) "${h}h ${r}m" else "${r}m"
}

fun formatAgo(millis: Long?, now: Long = System.currentTimeMillis()): String {
    if (millis == null) return "Never synced"
    val d = (now - millis) / 1000
    return when {
        d < 60 -> "Just now"
        d < 3600 -> "${d / 60} min ago"
        d < 86400 -> "${d / 3600} h ago"
        else -> "${d / 86400} d ago"
    }
}
