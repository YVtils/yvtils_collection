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

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Represents a single slot in a GUI with all its properties and behavior.
 * This is the core building block of the generic GUI system.
 *
 * @property material The material/item type to display in this slot
 * @property displayName The display name shown to the player
 * @property lore The lore lines shown in the item tooltip
 * @property clickHandlers Map of click types to their handler functions
 * @property customData Custom business logic data (player UUIDs, IDs, etc.)
 * @property itemCustomizer Function to customize the ItemStack after creation
 * @property itemFlags Item flags to add (HIDE_ENCHANTS, etc.)
 * @property enchantments Enchantments to apply to the item
 * @property glowing Whether the item should glow (adds invisible enchantment)
 * @property amount Stack size (default 1)
 * @property skullOwner Player name for player head items
 * @property customModelData Custom model data for resource pack textures
 */
data class GuiSlot(
    val material: Material,
    val displayName: String,
    val lore: List<String> = emptyList(),
    val clickHandlers: Map<ClickType, (Player, GuiContext) -> Unit> = emptyMap(),
    val customData: Map<String, Any> = emptyMap(),
    val itemCustomizer: ((ItemStack) -> ItemStack)? = null,
    val itemFlags: Set<ItemFlag> = emptySet(),
    val enchantments: Map<Enchantment, Int> = emptyMap(),
    val glowing: Boolean = false,
    val amount: Int = 1,
    val skullOwner: String? = null,
    val customModelData: Int? = null
) {
    /**
     * Handles a click event on this slot.
     * @return true if the click was handled, false otherwise
     */
    fun handleClick(clickType: ClickType, player: Player, context: GuiContext): Boolean {
        val handler = clickHandlers[clickType] ?: return false
        handler(player, context)
        return true
    }

    /**
     * Gets custom data by key with type safety.
     */
    inline fun <reified T> getData(key: String): T? {
        return customData[key] as? T
    }
}
