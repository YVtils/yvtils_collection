package yv.tils.waypoint.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.entity.PlayerDeathEvent
import yv.tils.waypoint.logic.WaypointNavigation

class NavigationListener(private val navigation: WaypointNavigation) : Listener {
    @EventHandler
    fun quit(event: PlayerQuitEvent) = navigation.stop(event.player)
    @EventHandler
    fun world(event: PlayerChangedWorldEvent) = navigation.stop(event.player)
    @EventHandler
    fun death(event: PlayerDeathEvent) = navigation.stop(event.entity)
}
