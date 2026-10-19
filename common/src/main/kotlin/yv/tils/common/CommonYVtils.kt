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

package yv.tils.common

import yv.tils.common.config.ConfigFile
import yv.tils.common.language.RegisterStrings
import yv.tils.utils.modules.Module
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import yv.tils.common.listeners.PlayerJoin
import yv.tils.common.listeners.PlayerLocaleChange
import yv.tils.common.permissions.PermissionManager
import yv.tils.common.permissions.PermissionsData
import yv.tils.common.updateChecker.PluginVersion
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.modules.Core
import yv.tils.utils.time.TimeUtils

class CommonYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "common",
            Module.readVersion(CommonYVtils::class.java, "common"),
            "Common module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/common/",
        )
    }

    override fun onLoad() {

    }

    override fun enablePlugin() {
        ConfigFile().loadConfig()

        RegisterStrings().registerStrings()

        Module.addModule(MODULE)
        PermissionManager.registerPermissions(
            PermissionsData().getPermissionList(true)
        )
        val plugin = Core.instance
        plugin.server.pluginManager.registerEvents(PlayerLocaleChange(), plugin)
        plugin.server.pluginManager.registerEvents(PlayerJoin(), plugin)
        plugin.server.pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun join(event: PlayerJoinEvent) {
                LanguageHandler()
                    .setPlayerLanguage(event.player.uniqueId, event.player.locale())
            }

            @EventHandler
            fun quit(event: PlayerQuitEvent) {
                LanguageHandler().removePlayerLanguage(event.player.uniqueId)
            }
        }, plugin)
    }

    override fun onLateEnablePlugin() {
        TimeUtils.CONFIG_ERROR_INVALID_TIMEZONE =
            LanguageHandler.getRawMessage("config.error.invalid.timezone")
        PermissionManager.registerPermissions(
            listOf(
                PermissionManager.YVtilsPermission(
                    "yvtils.*", "All YVtils permissions",
                    children = Module.getModules().associate { "yvtils.${it.name}.*" to true })
            )
        )
        PluginVersion().launchVersionCheck()
    }

    override fun disablePlugin() {
        Module.removeModule(MODULE)
    }
}
