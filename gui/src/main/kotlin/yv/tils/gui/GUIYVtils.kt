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

package yv.tils.gui

import yv.tils.gui.core.GuiManager
import yv.tils.gui.language.RegisterStrings
import yv.tils.gui.listeners.AsyncChat
import yv.tils.gui.listeners.InventoryClickListener
import yv.tils.gui.listeners.InventoryClose
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module

class GUIYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "gui",
            "1.0.0",
            "GUI module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/gui/"
        )
    }

    override fun onLoad() {
        RegisterStrings.registerStrings()
    }

    override fun enablePlugin() {
        Module.addModule(MODULE)
        registerListeners()
    }

    override fun onLateEnablePlugin() {}

    private fun registerListeners() {
        val plugin = Core.instance
        val pm = plugin.server.pluginManager
        pm.registerEvents(InventoryClickListener(), plugin)
        pm.registerEvents(InventoryClose(), plugin)
        pm.registerEvents(AsyncChat(), plugin)
    }

    override fun disablePlugin() {
        // Clean up all active GUIs
        GuiManager.clearAll()
    }
}
