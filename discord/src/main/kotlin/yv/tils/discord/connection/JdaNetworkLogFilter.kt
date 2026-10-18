package yv.tils.discord.connection

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.filter.AbstractFilter
import org.apache.logging.log4j.message.Message
import org.apache.logging.log4j.message.ParameterizedMessage

/** Only known transient diagnostics from this module's JDA classloader are replaced. */
class JdaNetworkLogFilter(
    private val notices: NetworkNotices,
    private val owned: () -> Boolean,
) : AbstractFilter() {
    companion object {
        const val REQUESTER = "net.dv8tion.jda.internal.requests.Requester"
        const val SOCKET = "net.dv8tion.jda.internal.requests.WebSocketClient"
        private val disconnect =
            Regex("Got disconnected from WebSocket \\(Code (\\d+)\\)\\. Appending to reconnect queue")

        fun classify(logger: String?, message: String): Pair<NetworkNotices.Kind, String>? {
            if (logger == REQUESTER && message.startsWith("DNS resolution failed:"))
                return NetworkNotices.Kind.REST to "Discord's address could not be resolved"
            if (logger == REQUESTER && message.startsWith("There was an I/O error while executing a REST request:")) {
                val reason = message.substringAfter("REST request:").trim().lowercase()
                val detail = when {
                    "no route to host" in reason || "network is unreachable" in reason -> "the network route to Discord is unavailable"
                    "timed out" in reason || reason == "timeout" -> "the request timed out"
                    "connection reset" in reason || "broken pipe" in reason -> "the connection was interrupted"
                    "connection refused" in reason || "failed to connect" in reason -> "a connection to Discord could not be established"
                    else -> return null // SSL, protocol and unfamiliar failures retain their original diagnostic.
                }
                return NetworkNotices.Kind.REST to detail
            }
            if (logger == SOCKET) {
                val code = disconnect.matchEntire(message)?.groupValues?.get(1)?.toIntOrNull()
                if (code in setOf(1000, 1001, 1006, 1011, 1012, 1013))
                    return NetworkNotices.Kind.GATEWAY to "close code $code"
            }
            return null
        }
    }

    @Volatile
    private var active = true
    fun deactivate() {
        active = false
    }

    private fun result(name: String?, level: Level?, message: String): Filter.Result {
        if (!active || level !in setOf(Level.WARN, Level.ERROR)) return Filter.Result.NEUTRAL
        val classified = classify(name, message) ?: return Filter.Result.NEUTRAL
        if (!owned()) return Filter.Result.NEUTRAL
        notices.failure(classified.first, classified.second)
        return Filter.Result.DENY
    }

    override fun filter(event: LogEvent): Filter.Result =
        result(event.loggerName, event.level, event.message.formattedMessage)

    override fun filter(logger: Logger, level: Level, marker: Marker?, msg: Message, t: Throwable?): Filter.Result =
        result(logger.name, level, msg.formattedMessage)

    override fun filter(logger: Logger, level: Level, marker: Marker?, msg: Any?, t: Throwable?): Filter.Result =
        result(logger.name, level, msg.toString())

    override fun filter(
        logger: Logger,
        level: Level,
        marker: Marker?,
        msg: String?,
        vararg params: Any?
    ): Filter.Result =
        result(logger.name, level, ParameterizedMessage(msg, params).formattedMessage)
}
