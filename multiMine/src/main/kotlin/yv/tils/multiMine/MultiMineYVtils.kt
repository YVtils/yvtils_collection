package yv.tils.multiMine

import coroutine.CoroutineHandler
import data.Data
import logger.Logger
import yv.tils.multiMine.commands.MultiMineCommand
import yv.tils.multiMine.configs.ConfigFile
import yv.tils.multiMine.configs.SaveFile
import yv.tils.multiMine.language.RegisterStrings
import yv.tils.multiMine.listeners.BlockBreak
import yv.tils.multiMine.listeners.PlayerJoin
import yv.tils.multiMine.logic.MultiMineHandler

class MultiMineYVtils : Data.YVtilsModule {
    companion object {
        const val MODULENAME = "multiMine"
        const val MODULEVERSION = "1.0.0"
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
        SaveFile().registerStrings()
    }

    override fun enablePlugin() {
        Data.loadedModules.add("$MODULENAME v$MODULEVERSION")

        registerCommands()
        registerListeners()
        registerCoroutines()
        registerPermissions()

        loadConfigs()
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
            suspend { MultiMineHandler().cooldownHandler() },
            "yvtils-multiMine-cooldownHandler",
            1 * 1000L,
        )
    }

    private fun registerPermissions() {
        val pm = Data.instance.server.pluginManager

//        pm.addPermission(
//            Permission.loadPermission("yvtils.use.multiMine", mapOf(
//                "description" to "Use MultiMine",
//                "default" to PermissionDefault.NOT_OP
//            ))
//        )
    }

    private fun loadConfigs() {
        Logger.debug("Loading configs for $MODULENAME v$MODULEVERSION")

        ConfigFile().loadConfig()
        SaveFile().loadConfig()
    }
}