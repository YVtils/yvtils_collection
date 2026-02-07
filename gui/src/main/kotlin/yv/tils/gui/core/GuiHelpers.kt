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

package yv.tils.gui.core

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import yv.tils.config.language.LanguageHandler
import yv.tils.gui.utils.HeadUtils
import yv.tils.gui.utils.Heads
import yv.tils.utils.colors.Colors

/**
 * Provides helper functions for creating common GUI patterns and components.
 * These utilities make it easier to build consistent GUIs across the system.
 */
object GuiHelpers {
    /**
     * Creates a "back" button slot that navigates to the parent GUI.
     * Typically placed in the bottom-left corner.
     */
    fun createBackButton(player: Player, position: Int = 0): Pair<Int, GuiSlot> {
        return position to GuiSlot(
            material = Material.PLAYER_HEAD,
            displayName = "<${Colors.MAIN.color}>${LanguageHandler.getRawMessage("action.gui.nav.back", player.uniqueId)}",
            lore = listOf("<gray>${LanguageHandler.getRawMessage("action.gui.nav.back.desc", player.uniqueId)}"),
            skullOwner = Heads.PREVIOUS_PAGE.texture,
            clickHandlers = mapOf(
                ClickType.LEFT to { p, ctx ->
                    if (ctx.hasParent()) {
                        GuiManager.openParent(p)
                    } else {
                        p.closeInventory()
                    }
                }
            )
        )
    }

    /**
     * Creates a "close" button slot that closes the GUI.
     * Typically placed in the bottom-right corner.
     */
    fun createCloseButton(player: Player, position: Int): Pair<Int, GuiSlot> {
        return position to GuiSlot(
            material = Material.PLAYER_HEAD,
            displayName = "<${Colors.MAIN.color}>${LanguageHandler.getRawMessage("action.gui.nav.quit", player.uniqueId)}",
            lore = listOf("<gray>Click to close"),
            skullOwner = Heads.X_CHARACTER.texture,
            clickHandlers = mapOf(
                ClickType.LEFT to { p, _ -> p.closeInventory() }
            )
        )
    }

    /**
     * Creates pagination navigation buttons (previous and next).
     * @param size The inventory size (to calculate bottom row positions)
     * @param currentPage Current page number (1-based)
     * @param totalPages Total number of pages
     * @param onPageChange Callback when page changes
     * @return Map of slot positions to GuiSlot definitions
     */
    fun createPaginationButtons(
        player: Player,
        size: Int,
        currentPage: Int,
        totalPages: Int,
        onPageChange: (GuiContext, Int) -> Unit
    ): Map<Int, GuiSlot> {
        val slots = mutableMapOf<Int, GuiSlot>()
        val leftPos = size - 9
        val rightPos = size - 1

        if (currentPage > 1) {
            slots[leftPos] = GuiSlot(
                material = Material.PLAYER_HEAD,
                displayName = "<${Colors.MAIN.color}>${LanguageHandler.getRawMessage("action.gui.nav.previousPage", player.uniqueId)}",
                lore = listOf("<gray>Page $currentPage of $totalPages"),
                skullOwner = Heads.PREVIOUS_PAGE.texture,
                clickHandlers = mapOf(
                    ClickType.LEFT to { _, ctx ->
                        ctx.page = (ctx.page - 1).coerceAtLeast(1)
                        onPageChange(ctx, ctx.page)
                    }
                )
            )
        }

        if (currentPage < totalPages) {
            slots[rightPos] = GuiSlot(
                material = Material.PLAYER_HEAD,
                displayName = "<${Colors.MAIN.color}>${LanguageHandler.getRawMessage("action.gui.nav.nextPage", player.uniqueId)}",
                lore = listOf("<gray>Page $currentPage of $totalPages"),
                skullOwner = Heads.NEXT_PAGE.texture,
                clickHandlers = mapOf(
                    ClickType.LEFT to { _, ctx ->
                        ctx.page = (ctx.page + 1).coerceAtMost(totalPages)
                        onPageChange(ctx, ctx.page)
                    }
                )
            )
        }

        return slots
    }

    /**
     * Creates a paginated layout helper.
     * @param items All items to paginate
     * @param slotsPerPage Number of slots available per page
     * @param currentPage Current page (1-based)
     * @param slotPositions List of slot positions to use for items
     * @param itemToSlot Function to convert an item to a GuiSlot
     * @return Map of slot positions to GuiSlot definitions for the current page
     */
    fun <T> createPaginatedSlots(
        items: List<T>,
        slotsPerPage: Int,
        currentPage: Int,
        slotPositions: List<Int>,
        itemToSlot: (T, Int) -> GuiSlot
    ): Map<Int, GuiSlot> {
        val startIndex = (currentPage - 1) * slotsPerPage
        val endIndex = (startIndex + slotsPerPage).coerceAtMost(items.size)
        
        if (startIndex >= items.size) return emptyMap()

        val pageItems = items.subList(startIndex, endIndex)
        val slots = mutableMapOf<Int, GuiSlot>()
        
        // Add items for this page
        pageItems.forEachIndexed { index, item ->
            val slotPos = slotPositions.getOrNull(index) ?: return@forEachIndexed
            slots[slotPos] = itemToSlot(item, index)
        }
        
        // Fill remaining content slots with AIR to ensure they're empty
        // (prevents filler from being applied to pagination area)
        for (i in pageItems.size until slotsPerPage.coerceAtMost(slotPositions.size)) {
            val slotPos = slotPositions.getOrNull(i) ?: continue
            slots[slotPos] = GuiSlot(
                material = Material.AIR,
                displayName = "",
                lore = emptyList()
            )
        }
        
        return slots
    }

    /**
     * Calculates total pages needed for pagination.
     */
    fun calculateTotalPages(totalItems: Int, itemsPerPage: Int): Int {
        return ((totalItems + itemsPerPage - 1) / itemsPerPage).coerceAtLeast(1)
    }

    /**
     * Creates a simple confirmation dialog with Yes/No buttons.
     * @param title The GUI title
     * @param confirmItem Item to display as context
     * @param onConfirm Callback when confirmed
     * @param onCancel Callback when cancelled
     */
    fun createConfirmationDialog(
        player: Player,
        title: String,
        confirmItem: GuiSlot,
        onConfirm: (GuiContext) -> Unit,
        onCancel: (GuiContext) -> Unit = { it.player.closeInventory() }
    ): GuiDefinition {
        val slots = mutableMapOf<Int, GuiSlot>()

        // Center item (what's being confirmed)
        slots[13] = confirmItem

        // Confirm button (green)
        slots[11] = GuiSlot(
            material = Material.LIME_STAINED_GLASS_PANE,
            displayName = "<green>Confirm",
            lore = listOf("<gray>Click to confirm"),
            clickHandlers = mapOf(
                ClickType.LEFT to { _, ctx ->
                    onConfirm(ctx)
                }
            )
        )

        // Cancel button (red)
        slots[15] = GuiSlot(
            material = Material.RED_STAINED_GLASS_PANE,
            displayName = "<red>Cancel",
            lore = listOf("<gray>Click to cancel"),
            clickHandlers = mapOf(
                ClickType.LEFT to { _, ctx ->
                    onCancel(ctx)
                }
            )
        )

        return GuiDefinition(
            title = title,
            slots = slots,
            size = 27,
            fillEmptySlots = true
        )
    }

    /**
     * Creates standard content slots for a chest GUI (avoids borders).
     * Returns a list of slot positions in the inner area.
     * 
     * For example, in a 6-row chest (54 slots):
     * - Excludes top row, bottom row, left column, right column
     * - Returns inner 7x4 area
     */
    fun getContentSlots(size: Int, borderSize: Int = 1): List<Int> {
        val rows = size / 9
        val slots = mutableListOf<Int>()

        for (row in borderSize until rows - borderSize) {
            for (col in borderSize until 9 - borderSize) {
                slots.add(row * 9 + col)
            }
        }

        return slots
    }
}
