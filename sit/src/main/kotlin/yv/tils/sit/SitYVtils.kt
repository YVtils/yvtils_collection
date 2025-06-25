package yv.tils.sit

import data.Data
import yv.tils.sit.commands.SitCommand
import yv.tils.sit.listeners.EntityDismount
import yv.tils.sit.listeners.PlayerQuit

class SitYVtils : Data.YVtilsModule {
    companion object {
        const val MODULE_NAME = "sit"
        const val MODULE_VERSION = "1.0.0"
    }

    override fun onLoad() {}

    override fun enablePlugin() {
        Data.addModule("$MODULE_NAME v$MODULE_VERSION")

        registerCommands()
        registerListeners()
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {

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