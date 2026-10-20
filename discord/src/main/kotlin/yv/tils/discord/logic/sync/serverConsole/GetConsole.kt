package yv.tils.discord.logic.sync.serverConsole

import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.connection.DiscordConnectionLogging
import yv.tils.discord.logic.AppLogic.Companion.getJDA
import yv.tils.utils.logger.Logger
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class GetConsole : AbstractAppender("YVtilsLogger", null, null, true, null) {
    companion object {
        @Volatile
        var active = false
        val channelID get() = ConfigFile.getValueAsString("syncFeature.consoleSync.channel") ?: ""
    }

    private val buffer = ConsoleBuffer()
    private val worker = Executors.newSingleThreadScheduledExecutor { task ->
        Thread(task, "yvtils-discord-console").apply { isDaemon = true }
    }
    private var retryAfter = 0L

    private fun processMessageQueue() {
        if (!active || !isStarted || System.nanoTime() < retryAfter) return
        try {
            val channel = getJDA().getTextChannelById(channelID) ?: return
            while (active && isStarted) {
                val batch = buffer.next() ?: return
                try {
                val formatted = "```ansi\n${batch.content}```"
                val message = if (batch.messageId == null) {
                    channel.sendMessage(formatted).complete()
                } else {
                    try {
                        channel.editMessageById(batch.messageId, formatted).complete()
                    } catch (error: ErrorResponseException) {
                        // Only a deleted message warrants replacement; never replay uncertain sends.
                        if (error.errorResponse != ErrorResponse.UNKNOWN_MESSAGE) throw error
                        channel.sendMessage(formatted).complete()
                    }
                }
                buffer.sent(batch, message.id)
                } catch (error: Exception) {
                    // The server may have accepted a send before its response was lost.
                    buffer.failed(batch)
                    throw error
                }
            }
        } catch (error: Exception) {
            if (!isStarted || Thread.currentThread().isInterrupted) return
            retryAfter = System.nanoTime() + TimeUnit.SECONDS.toNanos(60)
            if (!DiscordConnectionLogging.restFailure(error))
                Logger.error("[Discord] [ConsoleSync] Could not forward console output: ${error.message}")
        }
    }

    override fun append(event: LogEvent) {
        if (!active || !isStarted) return
        val message = event.message.formattedMessage
        if (event.loggerName.startsWith("net.dv8tion.jda.") ||
            "[Discord] [Connection]" in message || "[Discord] [ConsoleSync]" in message
        ) return
        val timestamp = SimpleDateFormat("HH:mm:ss").format(event.timeMillis)
        val color = when (event.level.toString()) {
            "ERROR" -> "\u001B[31m"
            "WARN" -> "\u001B[33m"
            "DEBUG" -> "\u001B[34m"
            else -> "\u001B[37m"
        }
        buffer.offer(event.timeMillis, "$color[$timestamp ${event.level}] [${event.loggerName}] $message\u001B[0m\n")
    }

    fun syncTask() {
        worker.scheduleWithFixedDelay(::processMessageQueue, 1, 1, TimeUnit.SECONDS)
    }

    fun clearHistory() = buffer.clear()

    override fun stop() {
        super.stop()
        worker.shutdownNow()
        buffer.clear()
    }
}
