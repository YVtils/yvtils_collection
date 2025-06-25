package yv.tils.status

import data.Data
import yv.tils.status.commands.StatusCommand
import yv.tils.status.configs.ConfigFile
import yv.tils.status.configs.SaveFile
import yv.tils.status.language.RegisterStrings
import yv.tils.status.listeners.PlayerJoin
import yv.tils.status.listeners.PlayerQuit

class StatusYVtils : Data.YVtilsModule {
    companion object {
        const val MODULE_NAME = "status"
        const val MODULE_VERSION = "1.0.0"
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
        SaveFile().registerStrings()
    }

    override fun enablePlugin() {
        Data.addModule("$MODULE_NAME v$MODULE_VERSION")

        registerCommands()
        registerListeners()
        registerCoroutines()

        loadConfigs()
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {

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
        Data.instance.server.pluginManager

    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
        SaveFile().loadConfig()
    }
}
