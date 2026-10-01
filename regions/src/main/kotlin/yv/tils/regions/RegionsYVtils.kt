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

import com.sk89q.worldguard.WorldGuard
import yv.tils.common.permissions.PermissionManager
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.regions.commands.RegionsCommand
import yv.tils.regions.configs.ConfigFile
import yv.tils.regions.configs.ManageGUI
import yv.tils.regions.data.PermissionsData
import yv.tils.regions.language.RegisterStrings
import yv.tils.regions.listeners.RegionsJoin
import yv.tils.regions.logic.ClaimFlags
import yv.tils.regions.logic.ClaimSelection
import yv.tils.regions.logic.ClaimService
import yv.tils.regions.commands.RegionAlias
import yv.tils.regions.data.ClaimMetadata
import yv.tils.regions.logic.ClaimSubzones
import yv.tils.regions.logic.ClaimOccupancy
import yv.tils.regions.logic.SubzoneSelection
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module

class RegionsYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "regions",
            Module.readVersion(RegionsYVtils::class.java, "regions"),
            "Survival claims and area protection backed by WorldGuard.",
            "YVtils",
            "https://docs.yvtils.net/regions/",
            configGuiOpener = { player -> ManageGUI().openGUI(player) },
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
    }

    override fun enablePlugin() {
        // Regions is built entirely on top of WorldGuard - without it there's nothing to enable.
        val worldguardPlugin = Core.instance.server.pluginManager.getPlugin("WorldGuard")
        if (worldguardPlugin == null || !worldguardPlugin.isEnabled) {
            Logger.warn("regions is enabled but WorldGuard is not installed/enabled - the module stays disabled.")
            return
        }

        // This module is bundled into core's main jar, whose WorldGuard dependency shares
        // the live plugin's classes. Verify API access before registering module features.
        try {
            WorldGuard.getInstance().platform.regionContainer
        } catch (e: Throwable) {
            Logger.error(
                "regions could not hook into the WorldGuard API (incompatible/mismatched version?) - " +
                        "the module stays disabled.",
                e
            )
            return
        }

        InvUIBootstrap.ensure()
        loadConfigs()
        if (!ConfigFile.state.enabled) return
        ClaimMetadata.load()
        Core.instance.server.worlds.forEach(ClaimService::upgradeNames)
        Core.instance.server.worlds.forEach(ClaimService::refreshMetadata)
        Core.instance.server.worlds.filter {
            WorldGuard.getInstance().platform.regionContainer.get(
                com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(
                    it
                )
            ) != null
        }
            .forEach(ClaimFlags::catchUp)

        Module.addModule(MODULE)

        registerPermissions()
        registerCommands()
        registerListeners()
        Core.instance.server.worlds.forEach(ClaimSubzones::refresh)
        ClaimOccupancy.start()

        Logger.info("regions hooked into WorldGuard successfully.")
    }

    override fun disablePlugin() {
        RegionAlias.shutdown()
        ClaimSelection.shutdown()
        SubzoneSelection.shutdown()
        ClaimOccupancy.shutdown()
        Module.removeModule(MODULE)
    }

    private fun registerCommands() {
        RegionsCommand()
        RegionAlias.register()
    }

    private fun registerListeners() {
        val plugin = Core.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(RegionsJoin(), plugin)
    }

    private fun registerPermissions() {
        PermissionManager.registerPermissions(PermissionsData().getPermissionList(includeWildcard = true))
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
    }
}
