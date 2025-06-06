package yv.tils.discord

import data.Data
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.configs.SaveFile
import yv.tils.discord.configs.StatsSyncSaveFile
import yv.tils.discord.language.RegisterStrings
import yv.tils.discord.logic.AppLogic

class DiscordYVtils : Data.YVtilsModule {
    companion object {
        const val MODULE_NAME = "discord"
        const val MODULE_VERSION = "4.0.0"

        var i = 0
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
        SaveFile().registerStrings()
        StatsSyncSaveFile().registerStrings()
    }

    override fun enablePlugin() {
        Data.addModule("$MODULE_NAME v$MODULE_VERSION")

        loadConfigs()

        AppLogic().startApp()
    }

    override fun disablePlugin() {
        AppLogic().stopApp()
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
        SaveFile().loadConfig()
        StatsSyncSaveFile().loadConfig()
    }
}