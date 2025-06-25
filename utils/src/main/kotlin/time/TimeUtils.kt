package time

import logger.Logger
import message.MessageUtils
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
}