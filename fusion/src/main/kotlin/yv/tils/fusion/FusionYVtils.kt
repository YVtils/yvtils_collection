/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion

import dev.jorel.commandapi.CommandAPI
import org.bukkit.event.HandlerList
import org.bukkit.plugin.ServicePriority
import yv.tils.common.permissions.PermissionManager
import yv.tils.fusion.api.FusionApi
import yv.tils.fusion.api.FusionRegistry
import yv.tils.fusion.commands.FusionCommand
import yv.tils.fusion.configs.RecipeStore
import yv.tils.fusion.data.PermissionsData
import yv.tils.fusion.gui.FusionGui
import yv.tils.fusion.language.FusionText
import yv.tils.fusion.listeners.FrameListener
import yv.tils.fusion.listeners.FusionInteractionListener
import yv.tils.fusion.listeners.PlayerQuit
import yv.tils.fusion.logic.crafting.Crafting
import yv.tils.fusion.logic.items.BuiltinOutputTypes
import yv.tils.fusion.logic.items.flask.NourishingFlask
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module

class FusionYVtils : Module.YVtilsModule {
    companion object {
        var active = false
            private set
        val MODULE = Module.YVtilsModuleData(
            "fusion", Module.readVersion(FusionYVtils::class.java, "fusion"),
            "Custom crafting and recipe management", configGuiOpener = { FusionGui.browser(it, manage = true) })
    }

    private val listeners = listOf(FrameListener(), FusionInteractionListener(), PlayerQuit())

    override fun onLoad() {
        FusionText.register()
        BuiltinOutputTypes.register()
        Core.instance.server.servicesManager.register(
            FusionApi::class.java,
            FusionRegistry,
            Core.instance,
            ServicePriority.Normal
        )
    }

    override fun enablePlugin() {
        RecipeStore.reload()
        InvUIBootstrap.ensure()
        PermissionManager.registerPermissions(PermissionsData().getPermissionList())
        FusionCommand()
        listeners.forEach { Core.instance.server.pluginManager.registerEvents(it, Core.instance) }
        active = true
        Module.addModule(MODULE)
    }

    override fun disablePlugin() {
        active = false
        listeners.forEach(HandlerList::unregisterAll)
        CommandAPI.unregister("fusion")
        Crafting.clear()
        NourishingFlask.clear()
        Core.instance.server.servicesManager.unregister(FusionApi::class.java, FusionRegistry)
        FusionRegistry.clear()
        Module.removeModule(MODULE)
    }
}
