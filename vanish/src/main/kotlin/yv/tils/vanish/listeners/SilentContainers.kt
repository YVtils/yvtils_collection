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
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class SilentContainers(private val service: VanishService) : Listener {
    private data class View(
        val uuid: UUID, val location: Location, val type: Material,
        val gui: Gui, val contents: List<ItemStack?>
    )

    private val windows = mutableMapOf<Window, View>()

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
        windows[window] = View(
            player.uniqueId, container.location, container.type, gui,
            container.inventory.contents.map { it?.clone() })
        window.open()
    }

    fun close() {
        windows.keys.toList().forEach { it.close() }
        windows.clear()
    }

    fun refresh() {
        windows.toMap().forEach { (window, view) ->
            if (!service.capability(
                    view.uuid,
                    VanishPermissions.SILENT_CONTAINERS,
                    ConfigFile.state.silentContainers
                )
            ) {
                window.close(); return@forEach
            }
            val world = view.location.world
            val viewer = Bukkit.getPlayer(view.uuid)
            if (viewer == null || viewer.world != world || viewer.location.distanceSquared(view.location) > 64.0) {
                window.close(); return@forEach
            }
            if (!world.isChunkLoaded(view.location.blockX shr 4, view.location.blockZ shr 4)) {
                window.close(); return@forEach
            }
            val container = view.location.block.state as? Container
            if (container == null || container.type != view.type) {
                window.close(); return@forEach
            }
            val next = container.inventory.contents.take(54).map { it?.clone() }
            for (index in 0 until maxOf(next.size, view.contents.size).coerceAtMost(54)) {
                if (next.getOrNull(index) != view.contents.getOrNull(index)) {
                    val stack = next.getOrNull(index)
                    view.gui.setItem(
                        index, if (stack == null || stack.type.isAir) Filler.item()
                        else Item.builder().setItemProvider(ItemWrapper(stack)).build()
                    )
                }
            }
            windows[window] = view.copy(contents = next)
        }
    }
}
