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

/**
 * Defines a complete GUI with all its properties and behavior.
 * This is used to create GUI instances through the GuiManager.
 *
 * @property title The title displayed at the top of the GUI
 * @property slots Map of slot positions (0-53) to their GuiSlot definitions
 * @property size Total size of the inventory (must be multiple of 9, max 54)
 * @property fillEmptySlots Whether to fill empty slots with a filler item
 * @property clickMode Determines which clicks are allowed/cancelled in this GUI
 * @property onOpen Callback invoked when the GUI is opened
 * @property onClose Callback invoked when the GUI is closed
 * @property onRefresh Callback invoked when the GUI is refreshed/rebuilt
 */
data class GuiDefinition(
    val title: String,
    val slots: Map<Int, GuiSlot>,
    val size: Int = 27,
    val fillEmptySlots: Boolean = true,
    val clickMode: ClickMode = ClickMode.CANCEL_ALL,
    val onOpen: ((GuiContext) -> Unit)? = null,
    val onClose: ((GuiContext) -> Unit)? = null,
    val onRefresh: ((GuiContext) -> Unit)? = null
) {
    init {
        require(size % 9 == 0 && size <= 54) {
            "GUI size must be a multiple of 9 and at most 54 (got $size)"
        }
        require(slots.keys.all { it in 0 until size }) {
            "All slot positions must be within 0 until $size"
        }
    }

    /**
     * Gets the GuiSlot at a specific position, if any.
     */
    fun getSlot(position: Int): GuiSlot? = slots[position]

    /**
     * Creates a copy of this definition with modified slots.
     */
    fun withSlots(newSlots: Map<Int, GuiSlot>): GuiDefinition {
        return copy(slots = newSlots)
    }

    /**
     * Creates a copy of this definition with additional slots.
     */
    fun withAdditionalSlots(additionalSlots: Map<Int, GuiSlot>): GuiDefinition {
        return copy(slots = slots + additionalSlots)
    }
}
