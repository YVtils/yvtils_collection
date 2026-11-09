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
package yv.tils.remadeEnderDragon.logic

import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.BundleMeta
import yv.tils.remadeEnderDragon.configs.DragonEggSettings

internal object DragonEggRules {
    fun allowsStorage(settings: DragonEggSettings, enderChest: Boolean): Boolean =
        if (enderChest) settings.enderChestPuttable else settings.chestPuttable

    /** Inspect nested portable containers without changing their item metadata. */
    fun containsEgg(stack: ItemStack?): Boolean {
        if (stack == null || stack.amount <= 0) return false
        if (stack.type == Material.DRAGON_EGG) return true
        if (!stack.hasItemMeta()) return false
        return when (val meta = stack.itemMeta) {
            is BundleMeta -> meta.items.any(::containsEgg)
            is BlockStateMeta -> meta.hasBlockState() &&
                    (meta.blockState as? Container)?.inventory?.contents?.any(::containsEgg) == true

            else -> false
        }
    }

    /** Vanilla may already have queued these stacks. Add only missing quantities. */
    fun addMissingDeathDrops(carried: List<ItemStack>, drops: MutableList<ItemStack>) {
        val queued = drops.filter(::containsEgg).map { it.clone() }
        carried.forEach { stack ->
            var missing = stack.amount
            queued.forEach { existing ->
                if (missing > 0 && existing.amount > 0 && existing.isSimilar(stack)) {
                    val used = minOf(missing, existing.amount)
                    missing -= used
                    existing.amount -= used
                }
            }
            if (missing > 0) drops.add(stack.clone().apply { amount = missing })
        }
    }

    /** Only reject insertion, never retrieval or rearranging the player's own inventory. */
    fun insertsEgg(
        action: InventoryAction,
        clickedTop: Boolean,
        cursorHasEgg: Boolean,
        currentHasEgg: Boolean,
        hotbarHasEgg: Boolean,
    ): Boolean = when (action) {
        InventoryAction.MOVE_TO_OTHER_INVENTORY -> !clickedTop && currentHasEgg
        InventoryAction.PLACE_ALL, InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME,
        InventoryAction.SWAP_WITH_CURSOR, InventoryAction.PLACE_FROM_BUNDLE,
        InventoryAction.PLACE_ALL_INTO_BUNDLE, InventoryAction.PLACE_SOME_INTO_BUNDLE -> clickedTop && cursorHasEgg

        InventoryAction.HOTBAR_SWAP -> clickedTop && hotbarHasEgg
        InventoryAction.UNKNOWN -> if (clickedTop) cursorHasEgg || hotbarHasEgg else currentHasEgg
        else -> false
    }
}
