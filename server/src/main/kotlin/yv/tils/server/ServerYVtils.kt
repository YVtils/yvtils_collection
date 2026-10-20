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
import yv.tils.server.configs.ManageGUI
import yv.tils.server.language.RegisterStrings
import yv.tils.server.listeners.*
import yv.tils.server.maintenance.MaintenanceCMD
import yv.tils.server.maintenance.MaintenanceHandler
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module
import yv.tils.utils.logger.Logger
import java.util.function.BiConsumer
import yv.tils.server.connect.EventMessages
import yv.tils.utils.modules.ConnectionMessages

class ServerYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "server",
            Module.readVersion(ServerYVtils::class.java, "server"),
            "Server module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/server/",
            configGuiOpener = { player -> ManageGUI().openGUI(player) },
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
    }

    override fun enablePlugin() {
        Module.addModule(MODULE)

        registerPermissions()
        loadConfigs()
        registerCommands()
        registerListeners()
        registerCoroutines()
        ConnectionMessages.announcer = BiConsumer { player, joining ->
            val messages = EventMessages()
            if (joining) messages.onFakePlayerJoin(player, EventMessages.EventCause.VANISH)
            else messages.onFakePlayerQuit(player, EventMessages.EventCause.VANISH)
        }
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {
        ConnectionMessages.announcer = null
        MaintenanceHandler.applyState(false, announce = false)
    }

    private fun registerCommands() {
        MaintenanceCMD()
    }

    private fun registerListeners() {
        val plugin = Core.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(PlayerJoin(), plugin)
        pm.registerEvents(PlayerQuit(), plugin)
        pm.registerEvents(PaperServerListPing(), plugin)
        pm.registerEvents(PlayerLogin(), plugin)
    }

    private fun registerCoroutines() {

    }

    private fun registerPermissions() {
        val manager = Core.instance.server.pluginManager
        for (name in listOf("yvtils.command.maintenance", MaintenanceHandler.BYPASS_PERMISSION)) {
            if (manager.getPermission(name) == null) {
                manager.addPermission(Permission(name, PermissionDefault.OP))
            }
        }
    }

    private fun loadConfigs() {
        Logger.debug("Loading configs for ${MODULE.name} v${MODULE.version}")

        ConfigFile().loadConfig()
    }
}
