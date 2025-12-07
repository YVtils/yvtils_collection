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

package yv.tils.multiMine.utils

import yv.tils.multiMine.configs.ConfigFile
import yv.tils.utils.logger.DEBUGLEVEL
import yv.tils.utils.logger.Logger
import java.util.*

class CooldownUtils {
    companion object {
        val cooldownTime = ConfigFile.config["cooldownTime"] as Int

        val cooldownMap: MutableMap<UUID, Int> = mutableMapOf()
    }

    /**
     * Sets the cooldown for the player
     * @param player The player's UUID
     */
    fun setCooldown(player: UUID) {
        cooldownMap[player] = cooldownTime
    }

    /**
     * Checks if the player is in cooldown
     * @param player The player's UUID
     * @return true if the player is in cooldown
     */
    fun checkCooldown(player: UUID): Boolean {
        return cooldownMap[player] != null && cooldownMap[player] != 0
    }

    /**
     * Handles the cooldowns for all players
     * Decreases the cooldown time by 1 second for each player in the map
     */
    fun cooldownHandler() {
        Logger.debug("Handling multiMine cooldowns...", DEBUGLEVEL.SPAM)
        for (entry in cooldownMap) {
            if (entry.value == 0) continue
            cooldownMap[entry.key] = entry.value - 1
        }
    }
}