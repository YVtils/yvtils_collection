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

package yv.tils.server

import yv.tils.server.configs.ConfigFile
import yv.tils.server.language.RegisterStrings
import yv.tils.server.listeners.*
import yv.tils.server.maintenance.MaintenanceCMD
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger

class ServerYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "server",
            "1.0.0",
            "Server module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/server/"
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
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
        MaintenanceCMD()
    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(PlayerJoin(), plugin)
        pm.registerEvents(PlayerQuit(), plugin)
        pm.registerEvents(PaperServerListPing(), plugin)
        pm.registerEvents(PlayerLogin(), plugin)
    }

    private fun registerCoroutines() {

    }

    private fun registerPermissions() {
        Data.instance.server.pluginManager

    }

    private fun loadConfigs() {
        Logger.debug("Loading configs for ${MODULE.name} v${MODULE.version}")

        ConfigFile().loadConfig()
    }
}
