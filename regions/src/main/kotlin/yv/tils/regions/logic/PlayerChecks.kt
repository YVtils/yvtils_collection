package yv.tils.regions.logic

import org.bukkit.OfflinePlayer
import yv.tils.regions.data.PlayerManager
import yv.tils.regions.data.RegionManager
import yv.tils.regions.data.RegionRoles
import java.util.*

class PlayerChecks {
    companion object {
        /**
         * Map of players in regions.
         * This is used to track which players are in which regions.
         * The key is the region data, and the value is a list of player UUIDs.
         */
        private var playersInRegions = mutableMapOf<RegionManager.RegionData, List<UUID>>()
        fun addPlayerToRegion(region: RegionManager.RegionData, player: OfflinePlayer) {
            val players = playersInRegions[region] ?: emptyList()
            playersInRegions[region] = players + player.uniqueId
        }
        fun removePlayerFromRegion(region: RegionManager.RegionData, player: OfflinePlayer) {
            val players = playersInRegions[region] ?: emptyList()
            playersInRegions[region] = players - player.uniqueId
        }
        fun getPlayersInRegion(region: RegionManager.RegionData): List<UUID> {
            return playersInRegions[region] ?: emptyList()
        }
        fun getPlayer(player: OfflinePlayer): RegionManager.RegionData? {
            for ((rg, players) in playersInRegions) {
                if (players.contains(player.uniqueId)) {
                    return rg
                }
            }
            return null
        }

        /**
         * Checks if the player is in a region.
         * @param player The player to check.
         * @param region The region to check against.
         * @return The region data if the player is in a region, null otherwise.
         */
        fun inSameRegion(player: OfflinePlayer, region: RegionManager.RegionData): Boolean {
            for ((rg, players) in playersInRegions) {
                if (players.contains(player.uniqueId) && rg == region) {
                    return true
                }
            }
            return false
        }

        fun regionRole(player: OfflinePlayer, region: RegionManager.RegionData): RegionRoles {
            val playerRegion = PlayerManager.getPlayerRegion(player, UUID.fromString(region.id))
            return playerRegion?.role ?: RegionRoles.NONE
        }
    }
}