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
import org.bukkit.*
import org.bukkit.entity.Player
import yv.tils.regions.configs.PlayerSaveFile
import yv.tils.regions.configs.RegionSaveFile
import yv.tils.regions.data.PlayerManager.Companion.players
import java.util.*

class RegionManager {
    companion object {
        fun createRegion(
            player: Player,
            name: String,
            loc1: Location,
            loc2: Location,
        ): RegionData {
            val world = loc1.world.name
            val x = loc1.blockX
            val z = loc1.blockZ
            val x2 = loc2.blockX
            val z2 = loc2.blockZ

            val rUUID = yv.tils.utils.data.UUID.generateUUID()

            val defaultGlobalFlags: MutableMap<Flag, Boolean> = mutableMapOf()
            val defaultRoleBasedFlags: MutableMap<Flag, Int> = mutableMapOf()
            val defaultLockedGlobalFlags: MutableMap<Flag, Boolean> = mutableMapOf()
            val defaultLockedRoleBasedFlags: MutableMap<Flag, Int> = mutableMapOf()

            val flags = FlagManager.flagEntryList
            for (flag in flags) {
                val flagType = flag.value.type

                when (flagType) {
                    FlagType.GLOBAL -> {
                        defaultGlobalFlags[flag.key] = flag.value.value as Boolean
                    }
                    FlagType.ROLE_BASED -> {
                        val role = RegionRoles.fromString(flag.value.value as String)

                        defaultRoleBasedFlags[flag.key] = role.permLevel
                    }
                    FlagType.LOCKED_GLOBAL -> {
                        defaultLockedGlobalFlags[flag.key] = flag.value.value as Boolean
                    }
                    FlagType.LOCKED_ROLE_BASED -> {
                        val role = RegionRoles.fromString(flag.value.value as String)

                        defaultLockedRoleBasedFlags[flag.key] = role.permLevel
                    }
                }
            }


            val regionData = RegionData(
                id = rUUID.toString(),
                name = name,
                world = world,
                x = x,
                z = z,
                x2 = x2,
                z2 = z2,
                created = System.currentTimeMillis(),
                flags = FlagManager.RegionFlags(
                    global = defaultGlobalFlags,
                    roleBased = defaultRoleBasedFlags,
                    lockedGlobal = defaultLockedGlobalFlags,
                    lockedRoleBased = defaultLockedRoleBasedFlags
                )
            )

            regions[rUUID] = regionData

            val playerRegion = PlayerManager.PlayerRegion(
                uuid = player.uniqueId.toString(),
                region = regionData.id,
                role = RegionRoles.OWNER
            )

            val playerUUID = player.uniqueId
            if (players.containsKey(playerUUID)) {
                players[playerUUID]!![rUUID] = playerRegion
            } else {
                players[playerUUID] = mutableMapOf(rUUID to playerRegion)
            }

            RegionSaveFile().updateRegionSetting(rUUID, regionData)
            PlayerSaveFile().updatePlayerSetting(playerUUID, rUUID, playerRegion)

            return regionData
        }

        fun deleteRegion(rUUID: UUID): Boolean {
            regions[rUUID] ?: return false
            regions.remove(rUUID)

            // Find players associated with this region
            for ((playerUUID, playerRegions) in players) {
                if (playerRegions.containsKey(rUUID)) {
                    playerRegions.remove(rUUID)
                    PlayerSaveFile().updatePlayerSetting(playerUUID, rUUID, null)
                }
            }

            RegionSaveFile().updateRegionSetting(rUUID, null)

            return true
        }

        fun getRegion(id: UUID): RegionData? {
            return regions[id]
        }

        fun getRegion(id: String): RegionData? {
            return getRegion(UUID.fromString(id))
        }

        /**
         * Gets a region by its name or ID.
         * If the name is a valid UUID, it will be treated as an ID.
         * If the name is not a valid UUID, it will be treated as a name.
         * If there are multiple regions with the same name, null will be returned.
         * @param name The name or ID of the region.
         * @return The region data if found, null otherwise.
         */
        fun getRegionByNameOrID(name: String): RegionData? {
            val uuid = try {
                UUID.fromString(name)
            } catch (_: IllegalArgumentException) {
                null
            }

            if (uuid != null) {
                return getRegion(uuid)
            }

            val regionList = regions.values.filter { it.name.equals(name, ignoreCase = true) }

            return when (regionList.size) {
                1 -> regionList[0]
                else -> null
            }
        }

        fun getRegions(name: String): List<RegionData> {
            val regionList = regions.values.filter { it.name.equals(name, ignoreCase = true) }

            return regionList.ifEmpty {
                emptyList()
            }
        }

        fun getRegions(player: OfflinePlayer): List<RegionData> {
            val playerRegions = players[player.uniqueId]
            return playerRegions?.values?.mapNotNull { getRegion(it.region) } ?: emptyList()
        }

        fun getRegions(player: OfflinePlayer, role: RegionRoles): List<RegionData> {
            val playerRegions = players[player.uniqueId]
            val permLevel = role.permLevel

            if (permLevel == RegionRoles.NONE.permLevel) {
                return getAllRegions()
            }

            return playerRegions?.values?.mapNotNull { region ->
                val regionData = getRegion(region.region)
                if (regionData != null && region.role.permLevel <= permLevel) {
                    regionData
                } else {
                    null
                }
            } ?: emptyList()
        }

        fun getRegions(player: OfflinePlayer, role: RegionRoles, negativeRole: RegionRoles): List<RegionData> {
            val playerRegions = players[player.uniqueId]
            val permLevel = role.permLevel
            val negativePermLevel = negativeRole.permLevel

            if (permLevel == RegionRoles.NONE.permLevel) {
                return getAllRegions()
            }

            return playerRegions?.values?.mapNotNull { region ->
                val regionData = getRegion(region.region)
                if (regionData != null && region.role.permLevel <= permLevel && region.role.permLevel != negativePermLevel) {
                    regionData
                } else {
                    null
                }
            } ?: emptyList()
        }

        fun getRegions(player: OfflinePlayer, role: RegionRoles, name: String): RegionData? {
            val regions = getRegions(player, role)
            val regionList = regions.filter { it.name.equals(name, ignoreCase = true) }

            return regionList.firstOrNull()
        }

        fun getRegions(world: World): List<RegionData> {
            return regions.values.filter { it.world == world.name }
        }

        fun getAllRegions(): List<RegionData> {
            return regions.values.toList()
        }

        fun loadRegion(uuid: UUID, regionData: RegionData?) {
            if (regionData == null) {
                regions.remove(uuid)
                return
            }
            regions[uuid] = regionData
        }

        fun saveRegion(): MutableMap<UUID, RegionData> {
            return regions
        }

        fun hasRegionWithName(name: String, player: OfflinePlayer): Boolean {
            val regions = getRegions(player)
            return regions.any { it.name.equals(name, ignoreCase = true) }
        }

        val regions = mutableMapOf<UUID, RegionData>()
    }

    @Serializable
    data class RegionData (
        val id: String,
        val name: String,
        val world: String,
        val x: Int,
        val z: Int,
        val x2: Int,
        val z2: Int,
        val created: Long,
        var flags: FlagManager.RegionFlags,
    )
}
