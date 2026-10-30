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

package yv.tils.vanish

import dev.jorel.commandapi.CommandAPI
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module
import yv.tils.vanish.commands.VanishCommand
import yv.tils.vanish.configs.ConfigFile
import yv.tils.vanish.configs.ManageGUI
import yv.tils.vanish.language.VanishText
import yv.tils.vanish.listeners.SilentContainers
import yv.tils.vanish.listeners.VanishListener
import yv.tils.vanish.logic.VanishService
import yv.tils.vanish.permissions.VanishPermissions
import java.util.function.Predicate
import yv.tils.utils.modules.ConnectionMessages
import yv.tils.vanish.configs.PlayerSettings

class VanishYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "vanish", Module.readVersion(VanishYVtils::class.java, "vanish"),
            "Layered staff vanish", "YVtils", configGuiOpener = { ManageGUI.open(it) }
        )
    }

    private val service = VanishService()
    private val containers = SilentContainers(service)
    private var permissionRefresh: BukkitTask? = null

    override fun onLoad() = VanishText.register()
    override fun enablePlugin() {
        ConfigFile.load()
        PlayerSettings.load()
        ConnectionMessages.hidden = Predicate { service.hidden(it.uniqueId) }
        InvUIBootstrap.ensure()
        ConfigFile.changed = { containers.close(); service.refresh() }
        VanishPermissions.register()
        VanishCommand(service)
        Bukkit.getPluginManager().registerEvents(VanishListener(service), Core.instance)
        Bukkit.getPluginManager().registerEvents(containers, Core.instance)
        Module.addModule(MODULE)
        permissionRefresh = Bukkit.getScheduler().runTaskTimer(Core.instance, Runnable {
            service.refresh()
            containers.refresh()
        }, 20L, 20L)
    }

    override fun disablePlugin() {
        ConfigFile.changed = {}
        permissionRefresh?.cancel()
        containers.close()
        service.close()
        ConnectionMessages.hidden = Predicate { false }
        // Paper command unregistration schedules a reload; core handles CommandAPI shutdown once disabled.
        if (Core.instance.isEnabled && CommandAPI.isLoaded()) {
            CommandAPI.unregister("vanish")
            CommandAPI.unregister("v")
        }
        Module.removeModule(MODULE)
    }
}
