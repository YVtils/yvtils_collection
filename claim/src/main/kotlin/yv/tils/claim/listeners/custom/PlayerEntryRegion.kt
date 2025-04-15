package yv.tils.claim.listeners.custom

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerEntryRegion(private var player: Player) : Event() {

    fun getPlayer(): Player {
        return this.player
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}