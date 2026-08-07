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

package yv.tils.essentials

import dev.jorel.commandapi.CommandAPI
import yv.tils.common.permissions.PermissionManager
import yv.tils.configv2.language.LanguageProvider
import yv.tils.essentials.commands.register.*
import yv.tils.essentials.config.StatesFile
import yv.tils.essentials.language.LangStrings
import yv.tils.essentials.listeners.*
import yv.tils.essentials.permissions.PermissionsData
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module

class EssentialYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "essentials",
            "26.08.01",
            "Essentials module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/essentials/"
        )
    }

    override fun onLoad() {
        LanguageProvider.registerEnumStrings<LangStrings>()
    }

    override fun enablePlugin() {
        Module.addModule(MODULE)

        unregisterCommands()

        registerCommands()
        registerListeners()
        registerPermissions()

        loadConfigs()
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {

    }

    private fun registerCommands() {
        DimensionCMD()
        FlyCMD()
        GamemodeCMD()
        GlobalMuteCMD()
        GodCMD()
        HealCMD()
        PingCMD()
        PvPCMD()
        SeedCMD()
        SpeedCMD()
    }

    private fun unregisterCommands() {
        CommandAPI.unregister("gamemode")
        CommandAPI.unregister("seed")
    }

    private fun registerListeners() {
        val plugin = Core.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(AsyncChat(), plugin)
        pm.registerEvents(EntityDamage(), plugin)
        pm.registerEvents(EntityDamageByEntity(), plugin)
        pm.registerEvents(PlayerChangedWorld(), plugin)
        pm.registerEvents(PlayerGameModeChange(), plugin)
        pm.registerEvents(PlayerJoin(), plugin)
        pm.registerEvents(PlayerPortal(), plugin)
    }

    private fun registerPermissions() {
        PermissionManager.registerPermissions(PermissionsData().getPermissionList(true))
    }

    private fun loadConfigs() {
        StatesFile().loadConfig()
    }
}
