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

package yv.tils.vanish.listeners

import org.bukkit.block.Container
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.Window
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.gui.utils.Filler
import yv.tils.vanish.configs.ConfigFile
import yv.tils.vanish.language.VanishText
import yv.tils.vanish.logic.VanishService
import yv.tils.vanish.permissions.VanishPermissions
import java.util.*

class SilentContainers(private val service: VanishService) : Listener {
    private val windows = mutableMapOf<Window, UUID>()

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun interact(event: PlayerInteractEvent) {
        val player = event.player
        if (event.action == Action.PHYSICAL && service.capability(
                player.uniqueId, VanishPermissions.PHYSICAL,
                ConfigFile.state.preventPhysicalInteraction
            )
        ) {
            event.isCancelled = true
            return
        }
        if (!service.capability(
                player.uniqueId,
                VanishPermissions.SILENT_CONTAINERS,
                ConfigFile.state.silentContainers
            ) ||
            !event.action.isRightClick || event.hand != EquipmentSlot.HAND
        ) return
        val container = event.clickedBlock?.state as? Container ?: return
        event.isCancelled = true
        InvUIBootstrap.ensure()
        val gui = Gui.builder().setStructure(*Array(6) { "x x x x x x x x x" })
            .addIngredient('x', Filler.item()).build()
        container.inventory.contents.take(54).forEachIndexed { index, stack ->
            if (stack != null) gui.setItem(index, Item.builder().setItemProvider(ItemWrapper(stack.clone())).build())
        }
        lateinit var window: Window
        window = Window.builder().setTitle(VanishText.text(player, "container")).setUpperGui(gui)
            .addCloseHandler { windows.remove(window) }.build(player)
        windows[window] = player.uniqueId
        window.open()
    }

    fun close() {
        windows.keys.toList().forEach { it.close() }
        windows.clear()
    }

    fun refresh() {
        windows.toMap().forEach { (window, uuid) ->
            if (!service.capability(
                    uuid,
                    VanishPermissions.SILENT_CONTAINERS,
                    ConfigFile.state.silentContainers
                )
            ) window.close()
        }
    }
}
