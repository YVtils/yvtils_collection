/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.discord.logic.sync.serverConsole

import yv.tils.utils.colors.Colors
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils
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
