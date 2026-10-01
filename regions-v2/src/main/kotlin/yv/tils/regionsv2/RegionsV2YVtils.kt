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

package yv.tils.regionsv2

import com.sk89q.worldguard.WorldGuard
import yv.tils.common.permissions.PermissionManager
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.regionsv2.commands.RegionsV2Command
import yv.tils.regionsv2.configs.ConfigFile
import yv.tils.regionsv2.configs.ManageGUI
import yv.tils.regionsv2.data.PermissionsData
import yv.tils.regionsv2.language.RegisterStrings
import yv.tils.regionsv2.listeners.RegionsV2Join
import yv.tils.regionsv2.logic.ClaimSelection
import yv.tils.regionsv2.logic.ClaimService
import yv.tils.regionsv2.logic.ClaimFlags
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module

class RegionsV2YVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "regions-v2",
            Module.readVersion(RegionsV2YVtils::class.java, "regions-v2"),
            "Regions V2 provides survival features to claim and protect areas with flags based on the widely used worldguard plugin",
            "YVtils",
            "https://docs.yvtils.net/regions-v2/",
            configGuiOpener = { player -> ManageGUI().openGUI(player) },
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
    }

    override fun enablePlugin() {
        // regions-v2 is built entirely on top of WorldGuard - without it there's nothing to enable.
        val worldguardPlugin = Core.instance.server.pluginManager.getPlugin("WorldGuard")
        if (worldguardPlugin == null || !worldguardPlugin.isEnabled) {
            Logger.warn("regions-v2 is enabled but WorldGuard is not installed/enabled - the module stays disabled.")
            return
        }

        // This module is bundled into core's main jar, whose WorldGuard dependency shares
        // the live plugin's classes. Verify API access before registering module features.
        try {
            WorldGuard.getInstance().platform.regionContainer
        } catch (e: Throwable) {
            Logger.error(
                "regions-v2 could not hook into the WorldGuard API (incompatible/mismatched version?) - " +
                        "the module stays disabled.",
                e
            )
            return
        }

        InvUIBootstrap.ensure()
        loadConfigs()
        if (!ConfigFile.state.enabled) return
        yv.tils.regionsv2.data.ClaimMetadata.load()
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
        Core.instance.server.worlds.forEach(yv.tils.regionsv2.logic.ClaimSubzones::refresh)
        yv.tils.regionsv2.logic.ClaimOccupancy.start()

        Logger.info("regions-v2 hooked into WorldGuard successfully.")
    }

    override fun onLateEnablePlugin() {
        // Runs after every module's enablePlugin() has completed - use this if this module ever
        // needs to react to another module that might not be ready yet during enablePlugin().
    }

    override fun disablePlugin() {
        yv.tils.regionsv2.commands.RegionAlias.shutdown()
        ClaimSelection.shutdown()
        yv.tils.regionsv2.logic.SubzoneSelection.shutdown()
        yv.tils.regionsv2.logic.ClaimOccupancy.shutdown()
        Module.removeModule(MODULE)
    }

    private fun registerCommands() {
        RegionsV2Command()
        yv.tils.regionsv2.commands.RegionAlias.register()
    }

    private fun registerListeners() {
        val plugin = Core.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(RegionsV2Join(), plugin)
    }

    private fun registerPermissions() {
        PermissionManager.registerPermissions(PermissionsData().getPermissionList(includeWildcard = true))
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
    }
}
