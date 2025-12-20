/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.gui.core

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import java.util.*

/**
 * Central manager for the generic GUI system.
 * Handles opening, closing, refreshing, and navigating between GUIs.
 */
object GuiManager {
    // Track active GUI contexts by player UUID
    private val activeContexts = mutableMapOf<UUID, GuiContext>()

    /**
     * Opens a GUI for a player with a new context.
     */
    fun open(player: Player, definition: GuiDefinition, guiId: String? = null, parentContext: GuiContext? = null): GuiContext {
        val context = GuiContext(
            guiId = guiId ?: UUID.randomUUID().toString(),
            player = player,
            parent = parentContext
        )
        return open(player, definition, context)
    }

    /**
     * Opens a GUI for a player with an existing context.
     */
    fun open(player: Player, definition: GuiDefinition, context: GuiContext): GuiContext {
        val holder = GuiGenericHolder(context)
        val inv = GuiBuilder.build(definition, context, holder)

        // Store context
        activeContexts[player.uniqueId] = context

        // Call onOpen callback
        definition.onOpen?.invoke(context)

        // Open inventory
        player.openInventory(inv)

        return context
    }

    /**
     * Refreshes the current GUI for a player.
     * This rebuilds the inventory with potentially updated data.
     */
    fun refresh(player: Player) {
        val context = activeContexts[player.uniqueId] ?: return
        val definition = context.definition ?: return

        // Call onRefresh callback if defined
        definition.onRefresh?.invoke(context)

        // Rebuild and reopen
        GuiBuilder.refresh(context)
    }

    /**
     * Refreshes a specific context (useful when you have the context reference).
     */
    fun refresh(context: GuiContext) {
        refresh(context.player)
    }

    /**
     * Closes the GUI for a player and cleans up the context.
     */
    fun close(player: Player) {
        val context = activeContexts.remove(player.uniqueId) ?: return
        context.definition?.onClose?.invoke(context)
        player.closeInventory()
    }

    /**
     * Opens the parent GUI if one exists (navigates back).
     */
    fun openParent(player: Player): Boolean {
        val context = activeContexts[player.uniqueId] ?: return false
        val parent = context.parent ?: return false
        val parentDef = parent.definition ?: return false

        // Open parent GUI
        open(player, parentDef, parent)
        return true
    }

    /**
     * Gets the active context for a player, if any.
     */
    fun getContext(player: Player): GuiContext? {
        return activeContexts[player.uniqueId]
    }

    /**
     * Gets the active context by UUID, if any.
     */
    fun getContext(uuid: UUID): GuiContext? {
        return activeContexts[uuid]
    }

    /**
     * Handles a click event in a generic GUI.
     * Returns true if the click was handled.
     */
    fun handleClick(player: Player, slot: Int, clickType: ClickType): Boolean {
        val context = activeContexts[player.uniqueId] ?: return false
        val definition = context.definition ?: return false
        val guiSlot = definition.getSlot(slot) ?: return false

        return guiSlot.handleClick(clickType, player, context)
    }

    /**
     * Clears all active contexts (useful for plugin reload/disable).
     */
    fun clearAll() {
        activeContexts.values.forEach { context ->
            context.player.closeInventory()
            context.definition?.onClose?.invoke(context)
        }
        activeContexts.clear()
    }

    /**
     * Removes a player's context without calling callbacks.
     * Used internally by the close listener.
     */
    internal fun removeContext(player: Player) {
        activeContexts.remove(player.uniqueId)
    }
}
