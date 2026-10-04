/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.remadeEnderDragon

import dev.jorel.commandapi.CommandAPI
import org.bukkit.event.HandlerList
import org.bukkit.Bukkit
import yv.tils.common.permissions.PermissionManager
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.remadeEnderDragon.data.PermissionsData
import yv.tils.remadeEnderDragon.gui.ManageGUI
import yv.tils.utils.logger.Logger
import yv.tils.remadeEnderDragon.commands.DragonCommand
import yv.tils.remadeEnderDragon.configs.ConfigFile
import yv.tils.remadeEnderDragon.language.Messages
import yv.tils.remadeEnderDragon.logic.FightManager
import yv.tils.remadeEnderDragon.logic.TemporaryTerrain
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module

class RemadeEnderDragonYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "remade-ender-dragon",
            Module.readVersion(RemadeEnderDragonYVtils::class.java, "remade-ender-dragon"),
            "Remade Ender Dragon — player-scaled encounters and team support",
            "YVtils", "https://docs.yvtils.net/remade-ender-dragon/",
            configGuiOpener = { player -> activeManager?.let { ManageGUI(it).openGUI(player) } },
        )
        private var activeManager: FightManager? = null
    }

    private var manager: FightManager? = null

    override fun onLoad() { Messages.register() }

    override fun enablePlugin() {
        ConfigFile.load()
        InvUIBootstrap.ensure()
        val terrain = TemporaryTerrain()
        val fights = FightManager(terrain)
        manager = fights
        activeManager = fights
        PermissionManager.registerPermissions(PermissionsData().getPermissionList(true))
        Core.instance.server.pluginManager.registerEvents(terrain, Core.instance)
        Core.instance.server.pluginManager.registerEvents(fights, Core.instance)
        fights.enable()
        DragonCommand(fights)
        Module.addModule(MODULE)
        Logger.info("[Remade Ender Dragon] Enabled with activation mode ${ConfigFile.state.activation}.")
    }

    override fun disablePlugin() {
        manager?.let {
            it.close()
            if (Core.instance.isEnabled) {
                Bukkit.getScheduler().runTaskLater(Core.instance, Runnable { HandlerList.unregisterAll(it) }, 200L)
            } else HandlerList.unregisterAll(it)
            HandlerList.unregisterAll(it.terrain)
        }
        manager = null
        activeManager = null
        CommandAPI.unregister("redragon", true)
        CommandAPI.unregister("remadeenderdragon", true)
        Module.removeModule(MODULE)
        Logger.info("[Remade Ender Dragon] Disabled; encounter cleanup complete.")
    }
}
