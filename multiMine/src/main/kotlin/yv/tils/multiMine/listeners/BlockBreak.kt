package yv.tils.multiMine.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import yv.tils.multiMine.logic.MultiMineHandler

class BlockBreak : Listener {
    @EventHandler
    fun onEvent(e: BlockBreakEvent) {
        MultiMineHandler().trigger(e)
    }
}