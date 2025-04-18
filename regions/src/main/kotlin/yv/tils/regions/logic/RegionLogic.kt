package yv.tils.regions.logic

import language.LanguageHandler
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.regions.data.Permissions
import yv.tils.regions.data.RegionManager
import yv.tils.regions.data.RegionRoles
import yv.tils.regions.language.LangStrings
import java.util.*

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

    fun registerRegion(player: Player, name: String, loc1: Location, loc2: Location) {
        if (!player.hasPermission(Permissions.REGION_CREATE.permission)) {
            player.sendMessage(LanguageHandler.getMessage(
                yv.tils.common.language.LangStrings.COMMAND_EXECUTOR_MISSING_PERMISSION.key,
                player.uniqueId
            ))
            return
        }

        // TODO: Add checks for overlapping regions
        // TODO: Add checks for region size
        // TODO: Add checks for region name -> owner can only create one region with the same name

        val region = RegionManager.createRegion(player, name, loc1, loc2)

        if (region == null) {
            player.sendMessage(LanguageHandler.getMessage(
                LangStrings.REGION_CREATE_FAIL_GENERIC.key,
                player.uniqueId,
                mapOf<String, Any>(
                    "region" to name,
                    "world" to loc1.world.name,
                    "x" to loc1.blockX.toString(),
                    "z" to loc1.blockZ.toString(),
                    "x2" to loc2.blockX.toString(),
                    "z2" to loc2.blockZ.toString(),
                )
            ))
            return
        }

        player.sendMessage(LanguageHandler.getMessage(
            LangStrings.REGION_CREATE_SUCCESS.key,
            player.uniqueId,
            mapOf<String, Any>(
                "region" to region.name,
                "world" to region.world,
                "x" to region.x.toString(),
                "z" to region.z.toString(),
                "x2" to region.x2.toString(),
                "z2" to region.z2.toString()
            )
        ))
    }

    fun removeRegion(sender: CommandSender, regionName: String) {
        if (!sender.hasPermission(Permissions.REGION_DELETE.permission)) {
            sender.sendMessage(LanguageHandler.getMessage(
                yv.tils.common.language.LangStrings.COMMAND_EXECUTOR_MISSING_PERMISSION.key,
                sender
            ))
            return
        }

        var finalRegion: RegionManager.RegionData? = null

        if (sender !is Player) {
            val regions = RegionManager.getRegions(regionName)

            when (getRegionListSize(regionName)) {
                1 -> {
                    finalRegion = regions[0]

                    RegionManager.deleteRegion(UUID.fromString(regions[0].id))
                }
                0 -> {
                    sender.sendMessage(LanguageHandler.getMessage(
                        LangStrings.REGION_GENERIC_NONE.key,
                        sender,
                        mapOf<String, Any>(
                            "region" to regionName
                        )
                    ))
                    return
                }
                else -> {
                    val regionList: MutableList<String> = mutableListOf()
                    for (region in regions) {
                        val owner = RegionManager.getRegionOwner(UUID.fromString(region.id))
                        regionList.add("${region.name}: ${region.id} (${owner})")
                    }

                    sender.sendMessage(LanguageHandler.getMessage(
                        LangStrings.REGION_GENERIC_MULTIPLE.key,
                        sender,
                        mapOf<String, Any>(
                            "regions" to regionList
                        )
                    ))

                    return
                }
            }
        } else {
            val region = RegionManager.getRegions(sender, RegionRoles.OWNER, regionName)
            if (region == null) {
                sender.sendMessage(LanguageHandler.getMessage(
                    LangStrings.REGION_GENERIC_NONE.key,
                    sender,
                    mapOf<String, Any>(
                        "region" to regionName
                    )
                ))
                return
            } else {
                finalRegion = region

                RegionManager.deleteRegion(UUID.fromString(region.id))
            }
        }

        val region = finalRegion ?: return

        sender.sendMessage(LanguageHandler.getMessage(
            LangStrings.REGION_DELETE_SUCCESS.key,
            sender,
            mapOf<String, Any>(
                "region" to region.name
            )
        ))
    }

    fun getRegionListSize(regionName: String): Int {
        val regions = RegionManager.getRegions(regionName)

        if (regions.isEmpty()) {
            return 0
        }
        return regions.size
    }
}