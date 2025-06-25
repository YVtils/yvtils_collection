package yv.tils.server

import data.Data
import logger.Logger
import yv.tils.server.configs.ConfigFile
import yv.tils.server.language.RegisterStrings
import yv.tils.server.listeners.PaperServerListPing
import yv.tils.server.listeners.PlayerJoin
import yv.tils.server.listeners.PlayerLogin
import yv.tils.server.listeners.PlayerQuit
import yv.tils.server.maintenance.MaintenanceCMD

class ServerYVtils : Data.YVtilsModule {
    companion object {
        const val MODULE_NAME = "server"
        const val MODULE_VERSION = "1.0.0"
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
    }

    override fun enablePlugin() {
        Data.addModule("$MODULE_NAME v$MODULE_VERSION")

        registerCommands()
        registerListeners()
        registerCoroutines()
        registerPermissions()
        loadConfigs()
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {

    }

    private fun registerCommands() {
        MaintenanceCMD()
    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(PlayerJoin(), plugin)
        pm.registerEvents(PlayerQuit(), plugin)
        pm.registerEvents(PaperServerListPing(), plugin)
        pm.registerEvents(PlayerLogin(), plugin)
    }

    private fun registerCoroutines() {

    }

    private fun registerPermissions() {
        val pm = Data.instance.server.pluginManager

    }

    private fun loadConfigs() {
        Logger.debug("Loading configs for $MODULE_NAME v$MODULE_VERSION")

        ConfigFile().loadConfig()
    }
}