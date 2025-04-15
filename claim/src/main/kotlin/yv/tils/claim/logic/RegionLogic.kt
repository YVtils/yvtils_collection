package yv.tils.claim.logic

import org.bukkit.Location
import yv.tils.claim.data.RegionManager

class RegionLogic {
    companion object {
        fun getRegion(loc: Location): RegionManager.RegionData? {
            val world = loc.world ?: return null
            val x = loc.blockX
            val z = loc.blockZ

            return RegionManager.getRegions(world).firstOrNull { region ->
                x in region.x..region.x2 && z in region.z..region.z2
            }
        }
    }
}