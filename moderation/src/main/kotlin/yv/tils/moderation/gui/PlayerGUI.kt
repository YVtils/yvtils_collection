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

package yv.tils.moderation.gui

import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.Window
import yv.tils.gui.utils.Filler
import yv.tils.utils.colors.Colors
import yv.tils.utils.message.MessageUtils
import yv.tils.utils.modules.Core

class PlayerGUI {
    fun openGUI(sender: CommandSender) {
        if (sender !is Player) {
            // TODO: Command can only be executed by player
            return
        }

        // No InvUIBootstrap.ensure() call needed here - ModerationYVtils.enablePlugin()
        // already guarantees InvUI is initialized before any command can run.

        val items = Core.instance.server.onlinePlayers.map { player ->
            Item.builder()
                .setItemProvider { ItemWrapper(buildPlayerHead(player)) }
                .addClickHandler { _, click ->
                    click.player().sendMessage(MessageUtils.convert("<${Colors.MAIN.color}>You clicked on ${player.name}'s head!"))
                }
                .build()
        }

        // page navigation buttons, only shown when there is something to navigate to
        val back = BoundItem.pagedBuilder()
            .setItemProvider { _, gui ->
                if (gui.page > 0) ItemBuilder(Material.ARROW).setName("<yellow>Previous page")
                else ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").hideTooltip(true)
            }
            .addClickHandler { _, gui, _ -> gui.page-- }

        val forward = BoundItem.pagedBuilder()
            .setItemProvider { _, gui ->
                if (gui.page < gui.pageCount - 1) ItemBuilder(Material.ARROW).setName("<yellow>Next page")
                else ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").hideTooltip(true)
            }
            .addClickHandler { _, gui, _ -> gui.page++ }

        val gui = PagedGui.itemsBuilder()
            .setStructure(
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# # # < # > # # #"
            )
            .addIngredient('#', Filler.item())
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', back)
            .addIngredient('>', forward)
            .setContent(items)
            .build()

        Window.builder()
            .setTitle(MessageUtils.convert("<${Colors.MAIN.color}>Player Moderation"))
            .setUpperGui(gui)
            .open(sender)
    }

    private fun buildPlayerHead(player: Player): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD)
        val meta = item.itemMeta as SkullMeta
        meta.playerProfile = player.playerProfile
        meta.displayName(MessageUtils.convert("<${Colors.MAIN.color}>${player.name}"))
        item.itemMeta = meta
        return item
    }
}