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

package yv.tils.sit

import yv.tils.sit.commands.SitCommand
import yv.tils.sit.listeners.EntityDismount
import yv.tils.sit.listeners.PlayerQuit
import yv.tils.utils.data.Data

class SitYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "sit",
            "1.0.0",
            "Sit module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/sit/"
        )
    }

    override fun onLoad() {}

    override fun enablePlugin() {
        Data.addModule(MODULE)

        registerCommands()
        registerListeners()
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {

    }

    private fun registerCommands() {
        SitCommand()
    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(EntityDismount(), plugin)
        pm.registerEvents(PlayerQuit(), plugin)
    }
}
