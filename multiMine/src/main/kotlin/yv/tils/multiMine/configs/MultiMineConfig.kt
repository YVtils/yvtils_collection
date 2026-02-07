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

package yv.tils.multiMine.configs

import org.bukkit.Material
import java.util.*

class MultiMineConfig {
    /**
     * Add a player to the multiMine save file
     * @param uuid the player uuid
     */
    fun addPlayer(uuid: UUID) {
        if (SaveFile.saves.containsKey(uuid)) return

        updatePlayerSetting(uuid, ConfigFile.config["defaultState"] as Boolean)
    }

    /**
     * Get the player activation state for the multiMine
     * @param uuid the player uuid
     * @return true if the player has multiMine activated
     */
    fun getPlayerSetting(uuid: UUID): Boolean {
        SaveFile.saves[uuid]?.let {
            return it.toggled
        }

        addPlayer(uuid)

        return ConfigFile.config["defaultState"] as Boolean
    }

    /**
     * Set the player activation state for the multiMine
     * @param uuid the player uuid
     * @param state the new state
     */
    fun updatePlayerSetting(uuid: UUID, state: Boolean) {
        SaveFile().updatePlayerSetting(uuid, state)
    }

    /**
     * Update the blocklist
     * @param blocks the new blocklist
     */
    fun updateBlockList(blocks: MutableList<Material>) {
        ConfigFile().updateBlockList(blocks)
    }

    /**
     * Check if the block is in the blocklist
     * @param block the block to check
     * @return true if the block is in the blocklist
     */
    fun checkBlock(block: Material): Boolean {
        return ConfigFile.blockList.contains(block)
    }
}