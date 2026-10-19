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
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

class ToolUtils {
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
