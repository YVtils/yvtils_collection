package yv.tils.sit

import data.Data
import yv.tils.sit.commands.SitCommand
import yv.tils.sit.listeners.EntityDismount
import yv.tils.sit.listeners.PlayerQuit

class SitYVtils {
    companion object {
        const val MODULENAME = "sit"
        const val MODULEVERSION = "1.0.0"
    }

    fun enablePlugin() {
        Data.loadedModules.add("$MODULENAME v$MODULEVERSION")

        registerCommands()
        registerListeners()
    }

    fun disablePlugin() {

    }

    private fun registerCommands() {
        SitCommand()
    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(EntityDismount(), plugin)
        pm.registerEvents(PlayerQuit(), plugin)
    }
}