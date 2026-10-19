package yv.tils.moderation.listeners

import io.papermc.paper.ban.BanListType
import io.papermc.paper.connection.PlayerConfigurationConnection
import io.papermc.paper.connection.PlayerLoginConnection
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import yv.tils.moderation.logic.BanScreen

/** Customize a real active profile ban without ever allowing a denied connection. */
class BannedLogin : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun validate(event: PlayerConnectionValidateLoginEvent) {
        val profile = when (val connection = event.connection) {
            is PlayerLoginConnection -> connection.authenticatedProfile
            is PlayerConfigurationConnection -> connection.profile
            else -> null
        } ?: return
        val uuid = profile.id ?: return
        val bans = Bukkit.getBanList(BanListType.PROFILE)
        if (!bans.isBanned(profile)) return
        val entry = bans.getBanEntry(profile) ?: return
        if (entry.expiration?.time?.let { it <= System.currentTimeMillis() } == true) return
        event.kickMessage(BanScreen.message(entry, uuid))
    }
}
