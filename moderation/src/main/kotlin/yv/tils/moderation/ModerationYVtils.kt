/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.moderation

import yv.tils.common.permissions.PermissionManager
import yv.tils.moderation.commands.*
import yv.tils.moderation.configs.ConfigFile
import yv.tils.moderation.configs.saveFile.MuteSaveFile
import yv.tils.moderation.data.PermissionsData
import yv.tils.moderation.language.RegisterStrings
import yv.tils.moderation.listeners.AsyncChat
import yv.tils.moderation.utils.MojangProfileLogFilter
import yv.tils.moderation.utils.TargetUtils
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.data.Data

class ModerationYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "moderation",
            "1.0.0-beta.1",
            "Moderation module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/moderation/"
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
        MuteSaveFile().registerStrings()
    }

    override fun enablePlugin() {
        Data.addModule(MODULE)

        registerLogFilters()
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
        BanCommand()
        TempBanCommand()
        UnbanCommand()

        MuteCommand()
        TempMuteCommand()
        UnmuteCommand()

        KickCommand()

        WarnCommand()

        //ModGUICommand()
    }

    private fun registerLogFilters() {
        MojangProfileLogFilter.register()
    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(AsyncChat(), plugin)
    }

    private fun registerCoroutines() {
        CoroutineHandler.launchTask(
            suspend { TargetUtils().cleanupMutedPlayers() },
            "yvtils-moderation-mutedPlayersHandler",
            300 * 1000L,
        )
    }

    private fun registerPermissions() {
        PermissionManager.registerPermissions(PermissionsData().getPermissionList(true))
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
        MuteSaveFile().loadConfig()
    }
}
