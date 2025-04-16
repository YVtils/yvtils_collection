package yv.tils.regions.data

import kotlinx.serialization.Serializable
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
        ): RegionData? {
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
                players[playerUUID] = players[playerUUID]!! + playerRegion
            } else {
                players[playerUUID] = listOf(playerRegion)
            }

            RegionSaveFile().updateRegionSetting(rUUID, regionData)
            PlayerSaveFile().updatePlayerSetting(playerUUID, playerRegion)

            return regionData
        }

        fun deleteRegion(rUUID: UUID): Boolean {
            val region = regions[rUUID] ?: return false

            regions.remove(rUUID)

            val playersInRegion = players.values.filter { it -> it.any { it.region == region.id } }

            for (playerRegion in playersInRegion) {
                val player = playerRegion.firstOrNull { it.region == region.id }
                val uuid = UUID.fromString(player?.player ?: return false)
                val playerRegions = players[uuid]
                if (playerRegions != null) {
                    players[uuid] = playerRegions.filter { it.region != region.id }
                }
            }

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
            val regions = players[player.uniqueId]
            val permLevel = role.permLevel

            return regions?.mapNotNull { region ->
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

        fun getRegions(world: World): List<RegionData> {
            return regions.values.filter { it.world == world.name }
        }

        fun getAllRegions(): List<RegionData> {
            return regions.values.toList()
        }

        fun getRegionOwner(id: UUID): String? {
            val region = regions[id] ?: return null
            val playerRegion = players.values.flatten().firstOrNull { it.region == region.id }
            return playerRegion?.player
        }

        private val players = mutableMapOf<UUID, List<PlayerRegion>>()
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

enum class RegionRoles(val permLevel: Int) {
    OWNER(0),
    MODERATOR(1),
    MEMBER(2),
    NONE(-1);
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