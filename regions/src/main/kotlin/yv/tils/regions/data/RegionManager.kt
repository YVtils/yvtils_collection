package yv.tils.regions.data

import kotlinx.serialization.Serializable
import logger.Logger
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import yv.tils.regions.configs.PlayerSaveFile
import yv.tils.regions.configs.RegionSaveFile
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
                    global = emptyMap(),
                    roleBased = emptyMap()
                )
            )

            regions[rUUID] = regionData

            val playerRegion = PlayerRegion(
                player = player.uniqueId.toString(),
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

        fun getRegions(name: String): List<RegionData> {
            val regionList = regions.values.filter { it.name.equals(name, ignoreCase = true) }

            return regionList.ifEmpty {
                emptyList()
            }
        }

        fun getRegions(player: Player, role: RegionRoles): List<RegionData> {
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

        fun getRegions(player: Player, role: RegionRoles, name: String): RegionData? {
            val regions = getRegions(player, role)
            val regionList = regions.filter { it.name.equals(name, ignoreCase = true) }

            return regionList.firstOrNull()
        }

        fun getPlayerRegion(player: Player, rUUID: UUID): PlayerRegion? {
            val playerRegions = players[player.uniqueId]
            return playerRegions?.get(rUUID)
        }

        fun getRegions(world: World): List<RegionData> {
            return regions.values.filter { it.world == world.name }
        }

        fun getAllRegions(): List<RegionData> {
            return regions.values.toList()
        }

        fun getRegionOwner(id: UUID): String? {
            val region = regions[id] ?: return null
            
            for (playerRegionMap in players.values) {
                for (playerRegion in playerRegionMap.values) {
                    if (playerRegion.region == region.id && playerRegion.role == RegionRoles.OWNER) {
                        return playerRegion.player
                    }
                }
            }
            
            return null
        }

        fun getRegionMembers(id: UUID): List<String>? {
            val region = regions[id] ?: return null
            val members = mutableListOf<String>()

            for (playerRegionMap in players.values) {
                for (playerRegion in playerRegionMap.values) {
                    if (playerRegion.region == region.id && playerRegion.role != RegionRoles.OWNER) {
                        members.add(playerRegion.player)
                    }
                }
            }

            return if (members.isEmpty()) null else members
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

        fun loadPlayer(uuid: UUID, rUUID: UUID, region: PlayerRegion?) {
            if (region == null) {
                Logger.dev("Removing player region: $uuid -> $rUUID")
                players[uuid]?.remove(rUUID)
                return
            }

            if (players.containsKey(uuid)) {
                players[uuid]!![rUUID] = region
            } else {
                players[uuid] = mutableMapOf(rUUID to region)
            }
        }

        fun savePlayer(): MutableMap<UUID, MutableMap<UUID, PlayerRegion>> {
            return players
        }

        private val players = mutableMapOf<UUID, MutableMap<UUID, PlayerRegion>>()
        private val regions = mutableMapOf<UUID, RegionData>()
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

    @Serializable
    data class PlayerRegion (
        val player: String,
        val region: String,
        val role: RegionRoles,
    )

    @Serializable
    data class RegionFlags(
        val global: Map<FlagType, Boolean>,
        val roleBased: Map<FlagType, Int>
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
    }
}

enum class FlagType {
    PVP,
    BUILD,
    DESTROY,
    CONTAINER,
    INTERACT,
    USE,
    TELEPORT
}
