package yv.tils.multiMine.utils

import logger.Logger
import yv.tils.multiMine.configs.ConfigFile
import java.util.UUID
import kotlin.collections.get
import kotlin.text.set

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
        Logger.debug("Handling multiMine cooldowns...")
        for (entry in cooldownMap) {
            if (entry.value == 0) continue
            cooldownMap[entry.key] = entry.value - 1
        }
    }
}