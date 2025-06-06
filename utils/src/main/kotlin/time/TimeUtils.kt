package time

import logger.Logger
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TimeUtils {
    companion object {
        var timeZone = "default"
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
        } catch (e: Exception) {
            // YVtils.instance.logger.warning(Language().getRawMessage(LangStrings.CONFIG_PARSE_ERROR_TIMEZONE))
            Logger.warn("Error parsing timezone: ${e.message}") // TODO: Replace with actual warning message
            return "xx/xx/xxxx xx:xx:xx"
        }
    }
}