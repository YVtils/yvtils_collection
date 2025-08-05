package yv.tils.status

import yv.tils.status.commands.StatusCommand
import yv.tils.status.configs.ConfigFile
import yv.tils.status.configs.SaveFile
import yv.tils.status.language.RegisterStrings
import yv.tils.status.listeners.PlayerJoin
import yv.tils.status.listeners.PlayerQuit
import yv.tils.utils.data.Data

class StatusYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "status",
            "1.0.0",
            "Status module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/status/"
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
        SaveFile().registerStrings()
    }

    override fun enablePlugin() {
        Data.addModule(MODULE)

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
