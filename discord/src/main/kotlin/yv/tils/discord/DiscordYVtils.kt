/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.discord

import dev.jorel.commandapi.CommandAPI
import yv.tils.common.permissions.PermissionManager
import yv.tils.config.language.LanguageHandler
import yv.tils.discord.commands.DiscordAccountsCommand
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.configs.ManageGUI
import yv.tils.discord.configs.SaveFile
import yv.tils.discord.configs.StatsSyncSaveFile
import yv.tils.discord.data.PermissionsData
import yv.tils.discord.language.RegisterStrings
import yv.tils.discord.listener.*
import yv.tils.discord.logic.AppLogic
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module

class DiscordYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "discord",
            Module.readVersion(DiscordYVtils::class.java, "discord"),
            "Discord integration for YVtils",
            "YVtils",
            "",
            configGuiOpener = { player -> ManageGUI().openGUI(player) },
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
    }

    override fun enablePlugin() {
        Module.addModule(MODULE)

        registerListeners()
        registerPermissions()

        loadConfigs()
        DiscordAccountsCommand()

        AppLogic().startApp()
    }

    override fun onLateEnablePlugin() {

        if (AppLogic.started) {
            Logger.info(LanguageHandler.getMessage(RegisterStrings.LangStrings.BOT_START_SUCCESS.key))
        }
    }

    override fun disablePlugin() {
        CommandAPI.unregister("discordaccounts")
        try {
            AppLogic().stopApp()
        } finally {
            yv.tils.discord.connection.DiscordConnectionLogging.close()
            unregisterModule()
        }
    }

    fun unregisterModule() {
        Module.removeModule(MODULE)
    }

    private fun registerListeners() {
        val plugin = Core.instance
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
