package yv.tils.regions.data

import kotlinx.serialization.Serializable
import logger.Logger
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import yv.tils.regions.configs.PlayerSaveFile
import yv.tils.regions.configs.RegionSaveFile
import yv.tils.regions.data.FlagType.*
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

            val rUUID = data.UUID.generateUUID()

            val defaultGlobalFlags = mutableMapOf(
                PVP to true,
            )

            val defaultRoleBasedFlags = mutableMapOf(
                PLACE to 3,
                DESTROY to 3,
                CONTAINER       to 3,
                INTERACT        to 3,
                USE             to 3,
                TELEPORT        to 3
            )

            val regionData = RegionData(
                id = rUUID.toString(),
                name = name,
                world = world,
                x = x,
                z = z,
                x2 = x2,
                z2 = z2,
                created = System.currentTimeMillis(),
                flags = RegionFlags(
                    global = defaultGlobalFlags,
                    roleBased = defaultRoleBasedFlags
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
                Logger.dev("Removing region: $uuid")
                regions.remove(uuid)
                return
            }
            regions[uuid] = regionData
        }

        fun saveRegion(): MutableMap<UUID, RegionData> {
            return regions
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
        val flags: RegionFlags,
    )



    /**
     * Data class representing the flags for a region.
     *
     * @property global A map of global flags and their values.
     * @property roleBased A map of role-based flags and their values.
     */
    @Serializable
    data class RegionFlags(
        /**
         * MutableMap of global flags and their values.
         * The key is the flag type and the value is a boolean indicating if the flag is set.
         *
         * @property FlagType The type of flag.
         * @property Boolean The value of the flag. If true, the action is allowed, if false, it is not.
         */
        val global: MutableMap<FlagType, Boolean>,
        /**
         * MutableMap of role-based flags and their values.
         * The key is the flag type,
         * and the value is an integer indicating the permission level required to set the flag.
         *
         * @property FlagType The type of flag.
         * @property Int The minimum permission level required setting the flag.
         */
        val roleBased: MutableMap<FlagType, Int>
    )
}

/**
 * Enum class representing the different types of flags that can be set in a region.
 *
 * @property PVP Flag for player vs player combat.
 * @property PLACE Flag for building in the region.
 *
 */
enum class FlagType {
    PVP,
    PLACE,
    DESTROY,
    CONTAINER,
    INTERACT,
    USE,
    TELEPORT;

    companion object {
        /**
         * Get flag type from string.
         * @param role The string representation of the flag type.
         * @return The corresponding FlagType.
         * @throws IllegalArgumentException if the string does not match any flag type.
         */
        fun fromString(role: String): FlagType {
            return FlagType.entries.firstOrNull { it.name.equals(role, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid flag type: $role")
        }
    }
}
