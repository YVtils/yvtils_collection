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

package yv.tils.regions

import yv.tils.regions.commands.RegionCommand
import yv.tils.regions.configs.*
import yv.tils.regions.data.FlagManager
import yv.tils.regions.language.RegisterStrings
import yv.tils.regions.listeners.*
import yv.tils.regions.listeners.cause.*
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module

class RegionsYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "regions",
            "1.0.0-beta.2",
            "Regions module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/regions/",
            configGuiOpener = { player -> ManageGUI().openGUI(player) },
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
    }

    override fun enablePlugin() {
        Module.addModule(MODULE)

        unregisterCommands()

        registerCommands()
        registerListeners()

        loadConfigs()

        FlagManager().initFlagList()
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {
    }

    private fun registerCommands() {
        RegionCommand()
    }

    private fun unregisterCommands() {

    }

    private fun registerListeners() {
        val plugin = Core.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(PlayerMove(), plugin)
        pm.registerEvents(BlockBreak(), plugin)
        pm.registerEvents(BlockPlace(), plugin)
        pm.registerEvents(PlayerInteract(), plugin)
        pm.registerEvents(EntityDamageByEntity(), plugin)
        pm.registerEvents(InventoryOpen(), plugin)
        pm.registerEvents(PlayerItemFrameChange(), plugin)
        pm.registerEvents(PlayerTeleport(), plugin)

        pm.registerEvents(PlayerEntryRegion(), plugin)
        pm.registerEvents(PlayerLeaveRegion(), plugin)
        pm.registerEvents(BlockFlagTrigger(), plugin)
        pm.registerEvents(PlayerFlagTrigger(), plugin)
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
        PlayerSaveFile().loadConfig()
        RegionSaveFile().loadConfig()
    }
}
