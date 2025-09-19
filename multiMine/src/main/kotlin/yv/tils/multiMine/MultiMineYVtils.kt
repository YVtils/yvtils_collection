package yv.tils.multiMine

import yv.tils.common.permissions.PermissionManager
import yv.tils.multiMine.commands.MultiMineCommand
import yv.tils.multiMine.configs.ConfigFile
import yv.tils.multiMine.configs.SaveFile
import yv.tils.multiMine.data.PermissionsData
import yv.tils.multiMine.language.RegisterStrings
import yv.tils.multiMine.listeners.BlockBreak
import yv.tils.multiMine.listeners.PlayerJoin
import yv.tils.multiMine.logic.MultiMineHandler
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.data.Data
import yv.tils.multiMine.utils.CooldownUtils

class MultiMineYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "multiMine",
            "2.0.0-beta.1",
            "MultiMine module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/multiMine/"
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
        registerPermissions()

        loadConfigs()
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {

    }

    private fun registerCommands() {
        MultiMineCommand()
    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(BlockBreak(), plugin)
        pm.registerEvents(PlayerJoin(), plugin)
    }

    private fun registerCoroutines() {
        CoroutineHandler.launchTask(
            suspend { CooldownUtils().cooldownHandler() },
            "yvtils-multiMine-cooldownHandler",
            1 * 1000L,
        )
    }

    private fun registerPermissions() {
        PermissionManager.registerPermissions(PermissionsData().getPermissionList(true))
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
        SaveFile().loadConfig()
    }
}
