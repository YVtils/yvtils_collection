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

package yv.tils.regions.data

import kotlinx.serialization.Serializable
import org.bukkit.OfflinePlayer
import yv.tils.regions.configs.PlayerSaveFile
import yv.tils.regions.data.RegionManager.Companion.regions
import yv.tils.utils.player.PlayerUtils
import java.util.*

class PlayerManager {
    companion object {
        fun savePlayer(): MutableMap<UUID, MutableMap<UUID, PlayerRegion>> {
            return players
        }

        fun loadPlayer(uuid: UUID, rUUID: UUID, region: PlayerRegion?) {
            if (region == null) {
                players[uuid]?.remove(rUUID)
                return
            }

            if (players.containsKey(uuid)) {
                players[uuid]!![rUUID] = region
            } else {
                players[uuid] = mutableMapOf(rUUID to region)
            }
        }

        fun getRegionMembers(id: UUID): List<String>? {
            val region = regions[id] ?: return null
            val members = mutableListOf<String>()

            for (playerRegionMap in players.values) {
                for (playerRegion in playerRegionMap.values) {
                    if (playerRegion.region == region.id && playerRegion.role != RegionRoles.OWNER) {
                        members.add(playerRegion.uuid)
                    }
                }
            }

            return if (members.isEmpty()) null else members
        }

        fun getRegionMembersAsUUIDs(id: UUID): List<UUID>? {
            val region = regions[id] ?: return null
            val members = mutableListOf<UUID>()

            for (playerRegionMap in players.values) {
                for (playerRegion in playerRegionMap.values) {
                    if (playerRegion.region == region.id && playerRegion.role != RegionRoles.OWNER) {
                        members.add(UUID.fromString(playerRegion.uuid))
                    }
                }
            }

            return if (members.isEmpty()) null else members
        }

        fun getRegionMembersAsPlayers(id: UUID): List<OfflinePlayer>? {
            val region = regions[id] ?: return null
            val members = mutableListOf<OfflinePlayer>()

            for (playerRegionMap in players.values) {
                for (playerRegion in playerRegionMap.values) {
                    if (playerRegion.region == region.id && playerRegion.role != RegionRoles.OWNER) {
                        members.add(PlayerUtils.uuidToPlayer(UUID.fromString(playerRegion.uuid)))
                    }
                }
            }

            return if (members.isEmpty()) null else members
        }

        fun getRegionOwner(id: UUID): String? {
            val region = regions[id] ?: return null

            for (playerRegionMap in players.values) {
                for (playerRegion in playerRegionMap.values) {
                    if (playerRegion.region == region.id && playerRegion.role == RegionRoles.OWNER) {
                        return playerRegion.uuid
                    }
                }
            }

            return null
        }

        fun getRegionOwnerAsUUID(id: UUID): UUID? {
            val region = regions[id] ?: return null

            for (playerRegionMap in players.values) {
                for (playerRegion in playerRegionMap.values) {
                    if (playerRegion.region == region.id && playerRegion.role == RegionRoles.OWNER) {
                        return UUID.fromString(playerRegion.uuid)
                    }
                }
            }

            return null
        }

        fun getRegionOwnerAsPlayer(id: UUID): OfflinePlayer? {
            val region = regions[id] ?: return null

            for (playerRegionMap in players.values) {
                for (playerRegion in playerRegionMap.values) {
                    if (playerRegion.region == region.id && playerRegion.role == RegionRoles.OWNER) {
                        return PlayerUtils.uuidToPlayer(UUID.fromString(playerRegion.uuid))
                    }
                }
            }

            return null
        }

        fun getPlayerRegion(player: OfflinePlayer, rUUID: UUID): PlayerRegion? {
            val playerRegions = players[player.uniqueId]
            return playerRegions?.get(rUUID)
        }

        fun addPlayerToRegion(player: OfflinePlayer, region: RegionManager.RegionData, role: RegionRoles) {
            val playerRegion = PlayerRegion(
                uuid = player.uniqueId.toString(),
                region = region.id,
                role = role
            )

            PlayerSaveFile().updatePlayerSetting(
                player.uniqueId,
                UUID.fromString(region.id),
                playerRegion
            )
        }

        fun removePlayerFromRegion(player: OfflinePlayer, region: RegionManager.RegionData) {
            PlayerSaveFile().updatePlayerSetting(
                player.uniqueId,
                UUID.fromString(region.id),
                null
            )
        }

        fun changeRoleInRegion(player: OfflinePlayer, region: RegionManager.RegionData, role: RegionRoles) {
            val playerRegion = PlayerRegion(
                uuid = player.uniqueId.toString(),
                region = region.id,
                role = role
            )
            PlayerSaveFile().updatePlayerSetting(
                player.uniqueId,
                UUID.fromString(region.id),
                playerRegion
            )
        }

        val players = mutableMapOf<UUID, MutableMap<UUID, PlayerRegion>>()
    }

    @Serializable
    data class PlayerRegion (
        val uuid: String,
        val region: String,
        val role: RegionRoles,
    )
}

@Serializable
enum class RegionRoles(val permLevel: Int) {
    OWNER(0),
    MODERATOR(1),
    MEMBER(2),
    NONE(-1);

    companion object {
        fun fromString(role: String): RegionRoles {
            return entries.firstOrNull { it.name.equals(role, ignoreCase = true) } ?: NONE
        }

        fun fromID(role: Int): RegionRoles {
            return entries.firstOrNull { it.permLevel == role } ?: NONE
        }
    }
}
