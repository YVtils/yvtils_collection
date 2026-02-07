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
import org.bukkit.event.inventory.ClickType
import yv.tils.gui.core.GuiDefinition
import yv.tils.gui.core.GuiHelpers.createPaginatedSlots
import yv.tils.gui.core.GuiHelpers.createPaginationButtons
import yv.tils.gui.core.GuiManager
import yv.tils.gui.core.GuiSlot
import yv.tils.utils.colors.Colors
import yv.tils.utils.data.Data

class PlayerGUI {
    fun openGUI(sender: CommandSender, page: Int = 1) {
        if (sender !is Player) {
            // TODO: Command can only be executed by player
            return
        }

        val gui = buildPlayerGui(sender, page)
        val context = GuiManager.open(sender, gui)
        
        context.data["currentPage"] = page
    }

    private fun buildPlayerGui(sender: Player, page: Int): GuiDefinition {
        val slotRanges = listOf(10..16, 19..25, 28..34, 37..43)
        val slotRange = slotRanges.flatMap { it.toList() }

        val onlinePlayers = Data.instance.server.onlinePlayers.toList()
        val items: MutableList<GuiSlot> = mutableListOf()

        for (player in onlinePlayers) {
            val guiSlot = GuiSlot(
                material = Material.PLAYER_HEAD,
                displayName = "<${Colors.MAIN.color}>${player.name}",
                lore = listOf(),
                skullOwner = player.playerProfile.textures.skin.toString(),
                clickHandlers = mapOf(
                    ClickType.LEFT to { p, ctx ->
                        p.sendMessage("<${Colors.MAIN.color}>You clicked on ${player.name}'s head!")
                    }
                )
            )
            items += guiSlot
        }

        val slotMap = mutableMapOf<Int, GuiSlot>()
        
        slotMap.putAll(
            createPaginatedSlots(
                items = items,
                slotPositions = slotRange,
                currentPage = page,
                itemToSlot = { item, _ -> item },
                slotsPerPage = slotRange.size
            )
        )

        val pageCount = items.size / slotRange.size + if (items.size % slotRange.size > 0) 1 else 0

        if (pageCount > 1) {
            val navButtons = createPaginationButtons(
                sender, 54, page, pageCount
            ) { ctx, newPage ->
                ctx.data["currentPage"] = newPage
                GuiManager.refresh(ctx)
            }
            slotMap.putAll(navButtons)
        }

        return GuiDefinition(
            title = "<${Colors.MAIN.color}>Player Moderation",
            size = 54,
            slots = slotMap,
            onRefresh = { ctx ->
                val currentPage = ctx.data["currentPage"] as? Int ?: 1
                val newDef = buildPlayerGui(ctx.player, currentPage)
                ctx.definition = newDef
            }
        )
    }
}