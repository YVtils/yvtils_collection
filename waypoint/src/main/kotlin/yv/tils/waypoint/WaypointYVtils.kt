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

package yv.tils.waypoint

import dev.jorel.commandapi.CommandAPI
import org.bukkit.Bukkit
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module
import yv.tils.waypoint.commands.WaypointCommand
import yv.tils.waypoint.config.ConfigFile
import yv.tils.waypoint.config.ManageGUI
import yv.tils.waypoint.config.WaypointStore
import yv.tils.waypoint.language.WaypointText
import yv.tils.waypoint.listeners.NavigationListener
import yv.tils.waypoint.logic.WaypointNavigation
import yv.tils.common.permissions.PermissionManager
import yv.tils.waypoint.permissions.PermissionsData

class WaypointYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "waypoint", Module.readVersion(WaypointYVtils::class.java, "waypoint"),
            "Persistent waypoints with private terrain highlights", "YVtils",
            configGuiOpener = { ManageGUI.open(it) }
        )
    }

    private val store = WaypointStore()
    private val navigation = WaypointNavigation()

    override fun onLoad() = WaypointText.register()

    override fun enablePlugin() {
        ConfigFile.load()
        InvUIBootstrap.ensure()
        store.load()
        PermissionManager.registerPermissions(PermissionsData().getPermissionList(includeWildcard = true))
        WaypointCommand(store, navigation)
        Bukkit.getPluginManager().registerEvents(NavigationListener(navigation), Core.instance)
        navigation.start()
        Module.addModule(MODULE)
    }

    override fun disablePlugin() {
        navigation.close()
        CommandAPI.unregister("waypoint")
        CommandAPI.unregister("wp")
        Module.removeModule(MODULE)
    }
}
