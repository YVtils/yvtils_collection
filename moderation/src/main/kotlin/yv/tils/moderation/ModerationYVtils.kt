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

import yv.tils.common.listeners.PlayerJoin
import yv.tils.common.permissions.PermissionManager
import yv.tils.moderation.configs.ConfigFile
import yv.tils.moderation.configs.SaveFile
import yv.tils.moderation.data.PermissionsData
import yv.tils.moderation.language.RegisterStrings
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.data.Data

class ModerationYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "moderation",
            "2.0.0-beta.1",
            "Moderation module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/moderation/"
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
        SaveFile().registerStrings()
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

    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

    }

    private fun registerCoroutines() {

    }

    private fun registerPermissions() {
        PermissionManager.registerPermissions(PermissionsData().getPermissionList(true))
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
        SaveFile().loadConfig()
    }
}
