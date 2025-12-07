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

package yv.tils.essentials

import dev.jorel.commandapi.CommandAPI
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import yv.tils.essentials.commands.register.*
import yv.tils.essentials.language.RegisterStrings
import yv.tils.essentials.listeners.*
import yv.tils.utils.data.Data

class EssentialYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "essentials",
            "1.0.0",
            "Essentials module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/essentials/"
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
    }

    override fun enablePlugin() {
        Data.addModule(MODULE)

        unregisterCommands()

        registerCommands()
        registerListeners()
        registerPermissions()
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {

    }

    private fun registerCommands() {
        FlyCMD()
        GamemodeCMD()
        GlobalMuteCMD()
        GodCMD()
        HealCMD()
        SeedCMD()
        SpeedCMD()
        PingCMD()
    }

    private fun unregisterCommands() {
        CommandAPI.unregister("gamemode")
        CommandAPI.unregister("seed")
    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(AsyncChat(), plugin)
        pm.registerEvents(EntityDamage(), plugin)
        pm.registerEvents(PlayerChangedWorld(), plugin)
        pm.registerEvents(PlayerGameModeChange(), plugin)
        pm.registerEvents(PlayerJoin(), plugin)
    }

    private fun registerPermissions() {
        val pm = Data.instance.server.pluginManager
        pm.addPermission(Permission.loadPermission("yvtils.bypass.globalmute", mapOf(
            "description" to "Bypass the global mute",
            "default" to PermissionDefault.OP
        )))
    }
}
