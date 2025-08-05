package yv.tils.utils.time

import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils
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
            Logger.Companion.warn(MessageUtils.Companion.convert(CONFIG_ERROR_INVALID_TIMEZONE))
            return "xx/xx/xxxx xx:xx:xx"
        }
    }
}
