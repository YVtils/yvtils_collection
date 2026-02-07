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

package yv.tils.multiMine.utils

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import yv.tils.multiMine.configs.ConfigFile

class ToolUtils {
    companion object {
        var toolBroke = false
    }

    /**
     * Damages the tool
     * @param player The player who is breaking the block
     * @param damage The amount of damage to deal to the item
     * @param tool The tool to damage
     * @return true if the item broke
     */
    fun damageTool(player: Player, damage: Int, tool: ItemStack): Boolean {
        val damageable: Damageable = tool.itemMeta as Damageable

        if (damageable.damage + damage >= tool.type.maxDurability) {
            if (ConfigFile.getBoolean("canToolsBreak") == false) {
                val restDurability = tool.type.maxDurability - damageable.damage - 1
                tool.damage(restDurability, player)
                toolBroke = true
                return true
            }

            toolBroke = true
            player.inventory.removeItem(tool)
            player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 1f, 1f)
            return true
        } else {
            tool.damage(damage, player)
            return false
        }
    }

    /**
     * Checks if the tool can break the block
     * @param block The block to check
     * @param tool The tool to check
     * @return true if the tool can break the block
     */
    fun checkTool(block: Block, tool: ItemStack): Boolean {
        if (tool.type == Material.AIR) return false
        if (tool.type.maxDurability.toInt() == 0) return false

        return block.getDrops(tool).isNotEmpty()
    }
}