package yv.tils.discord.logic.sync.serverConsole

import coroutine.CoroutineHandler
import logger.Logger
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.logic.AppLogic.Companion.getJDA
import java.text.SimpleDateFormat
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class GetConsole : AbstractAppender("YVtilsLogger", null, null, true, null) {
    companion object {
        var active = ConfigFile.getValueAsBoolean("syncFeature.consoleSync.enabled") ?: true
        val channelID = ConfigFile.getValueAsString("syncFeature.consoleSync.channel") ?: ""
        private const val MAX_MESSAGE_LENGTH = 1900
        private const val MARKDOWN_OVERHEAD = 8
        private var currentMessageContent = StringBuilder()
        private var lastMessageID: String? = null

        private val messageQueue = ConcurrentLinkedQueue<String>()
        private val isProcessing = AtomicBoolean(false)

        private var lastProcessedTime = System.currentTimeMillis()

        private var lastSentIndex = 0  // Track how much of the buffer we've already sent
        private val messageIDs = mutableListOf<String>()  // Added missing messageIDs list
    }

    init {
        start()
    }

    private fun processMessageQueue() {
        if (!isProcessing.compareAndSet(false, true)) return

        try {
            while (messageQueue.isNotEmpty()) {
                val message = messageQueue.poll() ?: break
                
                // Check if adding this message would exceed the limit
                if (currentMessageContent.length + message.length >= MAX_MESSAGE_LENGTH - MARKDOWN_OVERHEAD) {
                    // Send current content and start new message
                    if (currentMessageContent.isNotEmpty()) {
                        sendFormattedMessage(currentMessageContent.toString())
                        currentMessageContent.clear()
                        lastMessageID = null
                    }
                }
                
                currentMessageContent.append(message)
            }

            // Send any remaining content
            if (currentMessageContent.isNotEmpty()) {
                sendFormattedMessage(currentMessageContent.toString())
            }
        } finally {
            isProcessing.set(false)
        }
    }

    private fun sendFormattedMessage(content: String) {
        try {
            if (content.isEmpty()) return

            val formattedContent = "```ansi\n$content```"
            if (formattedContent.length > 2000) return

            val channel = getJDA().getTextChannelById(channelID) ?: return

            try {
                if (lastMessageID == null) {
                    // Clean up old messages if needed
                    while (messageIDs.size >= 10) {
                        val oldestMessageID = messageIDs.removeFirst()
                        channel.deleteMessageById(oldestMessageID).queue()
                    }

                    // Create new message
                    val message = channel.sendMessage(formattedContent).complete()
                    lastMessageID = message.id
                    messageIDs.add(lastMessageID!!)
                } else {
                    // Try to edit existing message
                    channel.editMessageById(lastMessageID!!, formattedContent).complete()
                }
            } catch (e: Exception) {
                // If edit fails, create new message
                val message = channel.sendMessage(formattedContent).complete()
                lastMessageID = message.id
                messageIDs.add(lastMessageID!!)
            }
        } catch (e: Exception) {
            Logger.error("Failed to send console message to Discord: ${e.message}")
        }
    }

    override fun append(event: LogEvent) {
        if (!active) return

        // Only process messages that occurred after the last command
        if (event.timeMillis < lastProcessedTime) return

        val formattedMessage = formatLogMessage(event)
        messageQueue.offer(formattedMessage)
    }

    private fun formatLogMessage(event: LogEvent): String {
        val timestamp = SimpleDateFormat("HH:mm:ss").format(event.timeMillis)
        val level = event.level.toString()
        val color = when(event.level.toString()) {
            "ERROR" -> "\u001B[31m" // Red
            "WARN" -> "\u001B[33m"  // Yellow
            "INFO" -> "\u001B[32m"  // Green
            else -> "\u001B[37m"    // White
        }
        return "$color[$timestamp $level] ${event.message.formattedMessage}\u001B[0m\n"
    }

    fun syncTask() {
        if (!active) return
        CoroutineHandler.launchTask(
            task = {
                processMessageQueue()
            },
            taskName = "yvtils-discord-consoleSync",
            afterDelay = 1
        )
    }

    fun clearHistory() {
        currentMessageContent.clear()
        messageQueue.clear()
        lastMessageID = null
        messageIDs.clear()
        lastProcessedTime = System.currentTimeMillis()
    }
}
