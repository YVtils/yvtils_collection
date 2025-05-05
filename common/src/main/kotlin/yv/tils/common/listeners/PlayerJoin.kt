package yv.tils.common.listeners

import data.Data
import language.LanguageHandler
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import yv.tils.common.config.ConfigFile
import yv.tils.common.data.Permissions
import yv.tils.common.updateChecker.PluginVersion

class PlayerJoin : Listener {
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val player = e.player

        if (!player.hasPermission(Permissions.COMMON_UPDATE_CHECK.permission) && !player.isOp) return
        if (ConfigFile.getValueAsBoolean("updateCheck.sendToOps") == true) {
            val messageKey = PluginVersion.moderatorMessageKeyOnJoin?.key ?: return
            val latestVersion = PluginVersion.cloudVersion ?: return
            val currentVersion = PluginVersion.serverVersion ?: return

            player.sendMessage(
                LanguageHandler.getMessage(
                    messageKey,
                    player.uniqueId,
                    mapOf(
                        "oldVersion" to currentVersion,
                        "newVersion" to latestVersion,
                        "link" to "<click:open_url:${Data.pluginURL}>${Data.pluginURL}</click>",
                    )
                )
            )
        }


    }
}