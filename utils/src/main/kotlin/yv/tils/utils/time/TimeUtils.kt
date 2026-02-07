/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.utils.time

import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TimeUtils {
    companion object {
        var timeZone = "default"

        var CONFIG_ERROR_INVALID_TIMEZONE = ""
    }

    fun parseTimezone(format: String = "dd/MM/yyyy HH:mm:ss"): String {
        val timezone = timeZone

        if (timezone == "default") {
            return SimpleDateFormat(format).format(System.currentTimeMillis())
        }

        try {
            val zoneId = ZoneId.of(timezone)
            val zonedDateTime = ZonedDateTime.now(zoneId)
            val dateFormat = DateTimeFormatter.ofPattern(format)
            val formatTime = zonedDateTime.format(dateFormat)

            return formatTime
        } catch (_: Exception) {
            Logger.warn(MessageUtils.convert(CONFIG_ERROR_INVALID_TIMEZONE))
            return "xx/xx/xxxx xx:xx:xx"
        }
    }

    /**
     * Parse time from string to calendar
     * @param duration [Int] of time duration
     * @param unit [String] of time unit
     * @return [Calendar]
     * @throws [IllegalArgumentException]
     */
    fun parseTime(duration: Int, unit: String): Calendar {
        val time: Calendar = Calendar.getInstance()
        when (unit) {
            "s" -> time.add(Calendar.SECOND, duration)
            "m" -> time.add(Calendar.MINUTE, duration)
            "h" -> time.add(Calendar.HOUR_OF_DAY, duration)
            "d" -> time.add(Calendar.DAY_OF_MONTH, duration)
            "w" -> time.add(Calendar.WEEK_OF_YEAR, duration)
            else -> {
                throw IllegalArgumentException("unit $unit is not supported")
            }
        }

        return time
    }

    /**
     * Format duration from milliseconds to human-readable string
     * @param durationMillis [Long] duration in milliseconds
     * @return [String] formatted duration
     */
    fun formatDuration(durationMillis: Long): String {
        val seconds = (durationMillis / 1000) % 60
        val minutes = (durationMillis / (1000 * 60) % 60)
        val hours = (durationMillis / (1000 * 60 * 60) % 24)
        val days = (durationMillis / (1000 * 60 * 60 * 24))

        val parts = mutableListOf<String>()
        if (days > 0) parts.add("$days d")
        if (hours > 0) parts.add("$hours h")
        if (minutes > 0) parts.add("$minutes m")
        if (seconds > 0) parts.add("$seconds s")

        return if (parts.isEmpty()) {
            "0 s"
        } else {
            parts.joinToString(" ")
        }
    }
}
