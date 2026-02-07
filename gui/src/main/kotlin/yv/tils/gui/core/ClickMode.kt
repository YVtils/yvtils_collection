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
 * Defines how clicks are handled in a GUI.
 */
enum class ClickMode {
    /**
     * All clicks are cancelled (default for most GUIs).
     * Players cannot move or modify any items.
     */
    CANCEL_ALL,

    /**
     * Clicks in the GUI are cancelled, but clicks in the player's
     * inventory (bottom) are allowed. Useful for GUIs where players
     * need to interact with their own inventory.
     */
    ALLOW_PLAYER_INVENTORY,

    /**
     * All clicks are allowed, both in GUI and player inventory.
     * Use with caution - typically only for special GUIs that
     * intentionally allow item manipulation.
     */
    ALLOW_ALL
}
