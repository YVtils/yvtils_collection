package yv.tils.discord.logic.sync.serverChats

import io.papermc.paper.event.player.AsyncChatEvent
import logger.Logger
import message.MessageUtils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.entity.Player
import player.PlayerUtils
import yv.tils.discord.data.Permissions
import yv.tils.discord.logic.AppLogic
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.active
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.channel
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.channelID
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.syncDiscordMessages
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager.Companion.syncMinecraftMessages


class SyncChats : ListenerAdapter() {
    fun minecraftToDiscord(e: AsyncChatEvent) {
        val message = e.originalMessage()

        if (!active) return
        if (!syncMinecraftMessages) return
        if (!e.player.hasPermission(Permissions.SYNC_CHAT.permission)) return

        sendDiscordMessage(e.player, MessageUtils.stripChatMessage(message))
    }

    private fun sendDiscordMessage(sender: Player, message: String) {
        try {
            channel.sendMessageEmbeds(MessageEmbeds().embedForChat(sender, message).build()).queue()
        } catch (_: UninitializedPropertyAccessException) {
            Logger.warn("Discord app was not able to establish chat sync bridge between minecraft and discord. Please check your channel configuration.")
            active = false
        }
    }

    private fun discordToMinecraft(e: MessageReceivedEvent) {
        val author = e.author.name
        val message = e.message.contentDisplay

        if (!active) return
        if (!syncDiscordMessages) return
        if (ServerChatsSyncManager.ignoreBotMessages && e.author.isBot) {
            return
        } else {
            if (e.author.id == AppLogic.appID) return
        }
        if (e.channel.id != channelID) return

        try {
            if (!e.member!!.hasPermission(Permission.valueOf(ServerChatsSyncManager.permission))) return
        } catch (_: Exception) {
            Logger.debug("Invalid permission '${ServerChatsSyncManager.permission}' for Discord chat sync. Please check your configuration.")
            if (!e.member!!.hasPermission(Permission.MESSAGE_SEND)) return
        }

        val compMessage = MessageUtils.convert("<gray>[<#7289da>DISCORD<gray>]<white> $author<gray>:<white> $message")

        PlayerUtils.broadcast(compMessage)
        Logger.info(compMessage)
    }

    override fun onMessageReceived(e: MessageReceivedEvent) {
        discordToMinecraft(e)
    }
}
