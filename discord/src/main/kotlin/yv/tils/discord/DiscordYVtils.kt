package yv.tils.discord

import yv.tils.common.permissions.PermissionManager
import yv.tils.config.language.LanguageHandler
import yv.tils.discord.actions.commands.JDACommandsRegister
import yv.tils.discord.configs.*
import yv.tils.discord.data.PermissionsData
import yv.tils.discord.language.RegisterStrings
import yv.tils.discord.listener.*
import yv.tils.discord.logic.AppLogic
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger

class DiscordYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "discord",
            "4.0.0",
            "Discord integration for YVtils",
            "YVtils",
            "",
        )

        var i = 0
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
        SaveFile().registerStrings()
        StatsSyncSaveFile().registerStrings()
    }

    override fun enablePlugin() {
        Data.addModule(MODULE)

        registerListeners()
        registerPermissions()

        loadConfigs()

        AppLogic().startApp()
    }

    override fun onLateEnablePlugin() {
        JDACommandsRegister().registerCommands()

        if (AppLogic.started) {
            Logger.info(LanguageHandler.getMessage(RegisterStrings.LangStrings.BOT_START_SUCCESS.key))
        }
    }

    override fun disablePlugin() {
        AppLogic().stopApp()
        unregisterModule()
    }

    fun unregisterModule() {
        Data.removeModule(MODULE)
    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(AsyncChat(), plugin)
        pm.registerEvents(PlayerAdvancementDone(), plugin)
        pm.registerEvents(PlayerJoin(), plugin)
        pm.registerEvents(PlayerQuit(), plugin)
        pm.registerEvents(PlayerDeath(), plugin)
    }

    private fun registerPermissions() {
        PermissionManager.registerPermissions(PermissionsData().getPermissionList(true))
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
        SaveFile().loadConfig()
        StatsSyncSaveFile().loadConfig()
    }
}
