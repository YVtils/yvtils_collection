package yv.tils.discord.logic.sync.serverConsole

import colors.Colors
import coroutine.CoroutineHandler
import data.Data
import logger.Logger
import message.MessageUtils
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.logic.AppLogic
import yv.tils.discord.logic.sync.serverConsole.GetConsole.Companion.active
import yv.tils.discord.logic.sync.serverConsole.GetConsole.Companion.channelID

class SendCMD : ListenerAdapter() {
    override fun onMessageReceived(e: MessageReceivedEvent) {
        if (!active) return

        if (ConfigFile.getValueAsBoolean("general.settings.ignoreBotMessages") == true && e.author.isBot) {
            return
        } else {
            if (e.author.id == AppLogic.appID) return
        }

        if (e.channelType.compareTo(ChannelType.TEXT) != 0) return

        val textChannel = e.channel.asTextChannel()
        if (textChannel.id != channelID) return

        val msg = e.message
        var content = msg.contentDisplay

        content = content.replace("/", "")

        CoroutineHandler.launchTask(
            task = {
                e.message.addReaction(Emoji.fromUnicode("🖥️")).queue()
                Logger.info(MessageUtils.convert("<gray>[<${Colors.MAIN.color}>DC Console<gray>]<white> $content"))
                GetConsole().clearHistory()
                Data.instance.server.scheduler.runTask(Data.instance, Runnable {
                    try {
                        Data.instance.server.dispatchCommand(Data.instance.server.consoleSender, content)
                    } catch (ex: Exception) {
                        Logger.error("Error executing command from discord console: ${ex.message}")
                    }
                } )
            },
            isOnce = true
        )
    }
}
