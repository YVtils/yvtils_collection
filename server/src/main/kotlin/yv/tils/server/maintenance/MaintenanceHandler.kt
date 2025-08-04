package yv.tils.server.maintenance

import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.command.CommandSender
import yv.tils.config.language.LanguageBroadcast
import yv.tils.config.language.LanguageHandler
import yv.tils.server.configs.ConfigFile
import yv.tils.utils.data.Data
import yv.tils.utils.player.PlayerUtils
import yv.tils.utils.server.ServerUtils

class MaintenanceHandler {
    companion object {
        var maintenance: Boolean = false
    }

    private var oldState: Boolean = false

    fun maintenance(sender: CommandSender, args: CommandArguments? = null) {
        val state = if (args?.get(0) == null) {
            "toggle"
        } else {
            args[0].toString()
        }

        when (state) {
            "toggle" -> {
                oldState = maintenance
                maintenance = !maintenance
            }

            "true" -> {
                oldState = maintenance
                maintenance = true
            }

            "false" -> {
                oldState = maintenance
                maintenance = false
            }
        }

        globalAnnouncement()
        senderAnnouncement(sender)
        saveState()
    }

    private fun globalAnnouncement() {
        if (oldState == maintenance) {
            return
        }

        PlayerUtils.onlinePlayersAsPlayers("yvtils.bypass.maintenance", false).forEach { player ->
            player.kick(
                LanguageHandler.getMessage(
                    "maintenance.player.join.unallowed",
                    player.uniqueId,
                    mapOf(
                        "prefix" to Data.prefix,
                    )
                )
            )
        }

        LanguageBroadcast.broadcast(
            "maintenance.announcement.global",
            "yvtils.bypass.maintenance",
            mapOf(
                "prefix" to Data.prefix,
                "state" to if (maintenance) LanguageHandler.getMessage("maintenance.state.on") else LanguageHandler.getMessage("maintenance.state.off"),
            )
        )
    }

    private fun senderAnnouncement(sender: CommandSender) {
        if (oldState == maintenance) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "maintenance.announcement.sender.already",
                    sender,
                    mapOf(
                        "prefix" to Data.prefix,
                        "state" to if (maintenance) LanguageHandler.getMessage("maintenance.state.on") else LanguageHandler.getMessage("maintenance.state.off"),
                    )
                )
            )
            return
        }

        sender.sendMessage(
            LanguageHandler.getMessage(
                "maintenance.announcement.sender.changed",
                sender,
                mapOf(
                    "prefix" to Data.prefix,
                    "state" to if (maintenance) LanguageHandler.getMessage("maintenance.state.on") else LanguageHandler.getMessage("maintenance.state.off"),
                )
            )
        )
    }

    private fun saveState() {
        ConfigFile.set("maintenance.enabled", maintenance)
        ServerUtils.setServerMaintenance(maintenance)
    }

    fun loadState() {
        maintenance = ConfigFile.get("maintenance.enabled") as Boolean
    }
}
