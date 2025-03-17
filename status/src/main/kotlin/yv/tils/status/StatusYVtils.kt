package yv.tils.status

import data.Data
import logger.Logger
import yv.tils.status.commands.StatusCommand
import yv.tils.status.configs.ConfigFile
import yv.tils.status.configs.SaveFile
import yv.tils.status.language.RegisterStrings
import yv.tils.status.listeners.PlayerJoin
import yv.tils.status.listeners.PlayerQuit

class StatusYVtils {
    companion object {
        const val MODULENAME = "status"
        const val MODULEVERSION = "1.0.0"
    }

    init {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
        SaveFile().registerStrings()
    }

    fun enablePlugin() {
        Data.loadedModules.add("$MODULENAME v$MODULEVERSION")

        registerCommands()
        registerListeners()
        registerCoroutines()

        loadConfigs()
    }

    fun disablePlugin() {

    }

    private fun registerCommands() {
        StatusCommand()
    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(PlayerJoin(), plugin)
        pm.registerEvents(PlayerQuit(), plugin)
    }

    private fun registerCoroutines() {

    }

    private fun registerPermissions() {
        val pm = Data.instance.server.pluginManager

    }

    private fun loadConfigs() {
        Logger.debug("Loading configs for $MODULENAME v$MODULEVERSION")

        ConfigFile().loadConfig()
        SaveFile().loadConfig()
    }
}