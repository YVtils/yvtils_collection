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

import com.destroystokyo.paper.profile.ProfileProperty
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import yv.tils.gui.logic.InventoryHandler
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.HeadUtils
import yv.tils.gui.utils.Heads
import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils
import java.net.URI
import java.util.*

/**
 * Builds Bukkit inventories from GUI definitions and slots.
 * Handles all item creation, customization, and inventory population.
 */
object GuiBuilder {
    /**
     * Builds a complete inventory from a GUI definition and context.
     */
    fun build(definition: GuiDefinition, context: GuiContext, holder: GuiGenericHolder): Inventory {
        val title: Component = MessageUtils.convert(definition.title)
        val inv = InventoryHandler.createInventory(holder, title, definition.size)
        holder.setInventory(inv)
        context.inventory = inv
        context.definition = definition

        // Fill slots with their items
        definition.slots.forEach { (position, slot) ->
            inv.setItem(position, buildItemStack(slot))
        }

        // Fill empty slots if requested
        if (definition.fillEmptySlots) {
            val blockedSlots = definition.slots.keys.toList()
            Filler.fillInventory(inv, blockedSlots = blockedSlots)
        }

        return inv
    }

    /**
     * Builds an ItemStack from a GuiSlot definition.
     */
    fun buildItemStack(slot: GuiSlot): ItemStack {
        val item = ItemStack(slot.material, slot.amount)

        // Handle player heads
        if (slot.material == Material.PLAYER_HEAD && slot.skullOwner != null) {
            val meta = item.itemMeta as? SkullMeta
            meta?.let {
                val playerProfile = Bukkit.createProfile(UUID.randomUUID())
                val texture = playerProfile.textures

                try {
                    if (slot.skullOwner.startsWith("http")) {
                        texture.skin = URI(slot.skullOwner).toURL()
                    } else {
                        val headItem = HeadUtils.createCustomHead(
                            headTexture = slot.skullOwner,
                            itemName = "",
                        )

                        val headMeta = headItem.itemMeta as SkullMeta
                        val headProfile = headMeta.playerProfile
                        val headTextures = headProfile?.textures ?: return@let
                        texture.skin = headTextures.skin
                    }
                } catch (e: Exception) {
                    Logger.error("Failed to load skull texture from URL: ${slot.skullOwner}")
                }

                playerProfile.setTextures(texture)
                it.playerProfile = playerProfile
                item.itemMeta = it
            }
        }

        val meta = item.itemMeta ?: return item

        // Display name
        meta.displayName(MessageUtils.convert(slot.displayName))

        // Lore
        if (slot.lore.isNotEmpty()) {
            meta.lore(MessageUtils.handleLore(slot.lore.joinToString("<newline>")))
        }

        // Item flags
        if (slot.itemFlags.isNotEmpty()) {
            meta.addItemFlags(*slot.itemFlags.toTypedArray())
        }

        // Enchantments
        slot.enchantments.forEach { (enchant, level) ->
            meta.addEnchant(enchant, level, true)
        }

        // Glowing effect
        if (slot.glowing && slot.enchantments.isEmpty()) {
            // Add invisible enchantment for glow effect
            meta.addEnchant(Enchantment.UNBREAKING, 1, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }

        // Custom model data
        slot.customModelData?.let {
            meta.setCustomModelData(it)
        }

        item.itemMeta = meta

        // Apply custom itemizer if provided
        return slot.itemCustomizer?.invoke(item) ?: item
    }

    /**
     * Refreshes an existing inventory with updated slot data.
     * This is more efficient than rebuilding the entire GUI.
     */
    fun refresh(context: GuiContext) {
        val inv = context.inventory ?: return
        val definition = context.definition ?: return

        // Clear all slots first to remove old items
        inv.clear()

        // Update slots with new definitions
        definition.slots.forEach { (position, slot) ->
            inv.setItem(position, buildItemStack(slot))
        }

        // Fill empty slots if requested
        if (definition.fillEmptySlots) {
            val blockedSlots = definition.slots.keys.toList()
            Filler.fillInventory(inv, blockedSlots = blockedSlots)
        }
    }
}
