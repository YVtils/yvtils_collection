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

package yv.tils.status

import yv.tils.gui.core.InvUIBootstrap
import yv.tils.status.commands.StatusCommand
import yv.tils.status.configs.ConfigFile
import yv.tils.status.configs.SaveFile
import yv.tils.status.language.RegisterStrings
import yv.tils.status.listeners.PlayerJoin
import yv.tils.status.listeners.PlayerQuit
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module

class StatusYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "status",
            "26.08.01",
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
        InvUIBootstrap.ensure()

        Module.addModule(MODULE)

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
        val plugin = Core.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(PlayerJoin(), plugin)
        pm.registerEvents(PlayerQuit(), plugin)
    }

    private fun registerCoroutines() {

    }

    private fun registerPermissions() {
        Core.instance.server.pluginManager

    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
        SaveFile().loadConfig()
    }
}
