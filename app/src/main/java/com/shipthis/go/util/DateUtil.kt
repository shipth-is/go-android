package com.shipthis.go.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Utility object for date formatting operations.
 */
object DateUtil {
    /**
     * Formats an ISO 8601 date string to a localized medium date format.
     * Falls back to the original string if parsing fails.
     *
     * @param iso8601String The ISO 8601 formatted date string
     * @return A formatted date string in medium format, or the original string if parsing fails
     */
    fun formatDate(iso8601String: String): String {
        return try {
            val instant = Instant.parse(iso8601String)
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            formatter.format(instant.atZone(ZoneId.systemDefault()))
        } catch (e: Exception) {
            iso8601String // Fallback to original string if parsing fails
        }
    }
}

