package yv.tils.claim.data

import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*


class RegionManager {
    companion object {
        fun createRegion(): RegionData? {
            return null // TODO("Not yet implemented")
        }

        fun deleteRegion(): Boolean {
            return false // TODO("Not yet implemented")
        }

        fun getRegion(id: UUID): RegionData? {
            return regions[id]
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

            return regions?.filter { it.role.permLevel <= permLevel }?.map { it.region } ?: emptyList()
        }

        fun getRegions(world: World): List<RegionData> {
            return regions.values.filter { it.world == world }
        }

        private val players = mutableMapOf<UUID, List<PlayerRegion>>()
        private val regions = mutableMapOf<UUID, RegionData>()
    }

    data class RegionData (
        val id: UUID,
        val name: String,
        val world: World,
        val x: Int,
        val z: Int,
        val x2: Int,
        val z2: Int,
        val created: Date,
        val flags: RegionFlags,
    )

    data class PlayerRegion (
        val player: UUID,
        val region: RegionData,
        val role: RegionRoles,
    )

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