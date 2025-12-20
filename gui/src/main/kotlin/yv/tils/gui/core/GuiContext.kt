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
import org.bukkit.inventory.Inventory

/**
 * Holds the runtime state and context for a GUI instance.
 * This tracks the current state, parent relationships, and custom data for a GUI.
 *
 * @property guiId Unique identifier for this GUI type
 * @property player The player viewing this GUI
 * @property data Mutable map for storing custom runtime data
 * @property page Current page number for paginated GUIs (1-based)
 * @property parent Parent GUI context for sub-GUIs (enables back navigation)
 * @property inventory Reference to the actual Bukkit inventory
 * @property definition The GUI definition used to create this context
 */
data class GuiContext(
    val guiId: String,
    val player: Player,
    val data: MutableMap<String, Any> = mutableMapOf(),
    var page: Int = 1,
    val parent: GuiContext? = null,
    var inventory: Inventory? = null,
    var definition: GuiDefinition? = null
) {
    /**
     * Gets data by key with type safety.
     */
    inline fun <reified T> getData(key: String): T? {
        return data[key] as? T
    }

    /**
     * Sets data by key.
     */
    fun setData(key: String, value: Any) {
        data[key] = value
    }

    /**
     * Checks if this GUI has a parent (is a sub-GUI).
     */
    fun hasParent(): Boolean = parent != null

    /**
     * Gets the root context (topmost parent).
     */
    fun getRoot(): GuiContext {
        var current = this
        while (current.parent != null) {
            current = current.parent
        }
        return current
    }
}
