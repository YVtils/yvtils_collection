package yv.tils.essentials.commands.handler

import dev.jorel.commandapi.executors.CommandArguments
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger

class GlobalMuteHandler {
    companion object {
        var globalMute: Boolean = false
    }

    var oldState: Boolean = false

    /**
     * Toggle global mute
     * @param sender CommandSender to send messages
     * @param args CommandArguments to get state
     */
    fun globalMute(sender: CommandSender, args: CommandArguments? = null) {
        val state = if (args?.get(0) == null) {
            "toggle"
        } else {
            args[0].toString()
        }

        when (state) {
            "toggle" -> {
                oldState = globalMute
                globalMute = !globalMute

                // SyncChats.active = !globalMute
            }

            "true" -> {
                oldState = globalMute
                globalMute = true

                // SyncChats.active = false
            }

            "false" -> {
                oldState = globalMute
                globalMute = false

                // SyncChats.active = true
            }
        }

        globalAnnouncement()
        senderAnnouncement(sender)
    }

    /**
     * Send global announcement
     * @param event String of event
     */
    private fun globalAnnouncement() {
        if (oldState == globalMute) {
            return
        }

        for (player in Bukkit.getOnlinePlayers()) {
            if (globalMute) {
                player.sendMessage(
                    LanguageHandler.getMessage(
                        "command.globalmute.enable",
                        player.uniqueId,
                        params = mapOf(
                            "prefix" to Data.prefix,
                        )
                    )
                )
            } else {
                player.sendMessage(
                    LanguageHandler.getMessage(
                        "command.globalmute.disable",
                        player.uniqueId,
                        params = mapOf(
                            "prefix" to Data.prefix,
                        )
                    )
                )
            }
        }

        if (globalMute) {
            Logger.info(LanguageHandler.getMessage(
                "command.globalmute.enable",
                params = mapOf(
                    "prefix" to Data.prefix,
                )
            ))
        } else {
            Logger.info(LanguageHandler.getMessage(
                "command.globalmute.disable",
                params = mapOf(
                    "prefix" to Data.prefix,
                )
            ))
        }
    }

    /**
     * Send sender announcement
     * @param sender CommandSender to send messages
     * @param event String of event
     */
    private fun senderAnnouncement(sender: CommandSender) {
        if (oldState == globalMute) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.globalmute.already",
                    sender,
                    params = mapOf(
                        "prefix" to Data.prefix,
                    )
                )
            )
            return
        }
    }

    /**
     * Handle player chat event for global mute
     * @param e AsyncChatEvent
     */
    fun playerChatEvent(e: AsyncChatEvent) {
        if (globalMute) {
            if (e.player.hasPermission("yvtils.bypass.globalmute")) {
                return
            }

            e.isCancelled = true
            e.player.sendMessage(
                LanguageHandler.getMessage(
                    "globalmute.try_to_write",
                    e.player.uniqueId,
                    params = mapOf(
                        "prefix" to Data.prefix,
                    )
                )
            )
        }
    }
}
