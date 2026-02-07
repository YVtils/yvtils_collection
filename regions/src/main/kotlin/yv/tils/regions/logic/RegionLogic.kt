/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.regions.logic

import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.regions.configs.ConfigFile
import yv.tils.regions.data.*
import yv.tils.regions.language.LangStrings
import java.util.*
import kotlin.math.abs

class RegionLogic {
    companion object {
        fun getRegion(loc: Location): RegionManager.RegionData? {
            val world = loc.world ?: return null
            val x = loc.blockX
            val z = loc.blockZ

            val regions = RegionManager.getAllRegions()
            for (region in regions) {
                val cX1 = region.x
                val cZ1 = region.z
                val cX2 = region.x2
                val cZ2 = region.z2
                val worldName = region.world

                if (worldName == world.name) {
                    if (x in minOf(cX1, cX2)..maxOf(cX1, cX2) && z in minOf(cZ1, cZ2)..maxOf(cZ1, cZ2)) {
                        return region
                    }
                }
            }

            return null
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

        if (isOverlappingWithExistingRegions(loc1, loc2)) {
            player.sendMessage(LanguageHandler.getMessage(
                LangStrings.REGION_CREATE_FAIL_OVERLAP.key,
                player.uniqueId,
            ))
            return
        }

        val regionSize = calcNewRegionSize(loc1, loc2)
        val maxSize = ConfigFile.getValueAsInt("settings.region.max.size") ?: -1
        val minSize = ConfigFile.getValueAsInt("settings.region.min.size") ?: -1
        when {
            (regionSize > maxSize && maxSize != -1) -> {
                player.sendMessage(LanguageHandler.getMessage(
                    LangStrings.REGION_CREATE_FAIL_SIZE_MAX.key,
                    player.uniqueId,
                    mapOf<String, Any>(
                        "maxSize" to maxSize.toString()
                    )
                ))
                return
            }
            (regionSize < minSize && minSize != -1) -> {
                player.sendMessage(LanguageHandler.getMessage(
                    LangStrings.REGION_CREATE_FAIL_SIZE_MIN.key,
                    player.uniqueId,
                    mapOf<String, Any>(
                        "minSize" to minSize.toString()
                    )
                ))
                return
            }
        }

        val ownedRegions = RegionManager.getRegions(player, RegionRoles.OWNER)
        val maxRegions = ConfigFile.getValueAsInt("settings.region.max.owned") ?: -1
        if (ownedRegions.size >= maxRegions && maxRegions != -1) {
            player.sendMessage(LanguageHandler.getMessage(
                LangStrings.REGION_CREATE_FAIL_OWNED_MAX.key,
                player.uniqueId,
                mapOf<String, Any>(
                    "maxRegions" to maxRegions.toString()
                )
            ))
            return
        }

        if (RegionManager.hasRegionWithName(name, player)) {
            player.sendMessage(LanguageHandler.getMessage(
                LangStrings.REGION_CREATE_FAIL_ALREADY_EXISTS.key,
                player.uniqueId,
                mapOf<String, Any>(
                    "region" to name
                )
            ))
            return
        }

        RegionManager.createRegion(player, name, loc1, loc2)

        player.sendMessage(LanguageHandler.getMessage(
            LangStrings.REGION_CREATE_SUCCESS.key,
            player.uniqueId,
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

        val finalRegion: RegionManager.RegionData?

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
                        val owner = PlayerManager.getRegionOwner(UUID.fromString(region.id))
                        regionList.add("<click:suggest_command:/rg remove ${region.id}>${region.name}: ${region.id} (${owner})</click>")
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

        val region = finalRegion

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

    private fun calcNewRegionSize(loc1: Location, loc2: Location): Int {
        val x = abs(loc1.blockX - loc2.blockX)
        val z = abs(loc1.blockZ - loc2.blockZ)

        return x * z
    }

    /**
     * Check if the new region overlaps with any existing regions.
     * @param loc1 The first location of the new region.
     * @param loc2 The second location of the new region.
     * @return True if the new region overlaps with any existing regions, false otherwise.
     */
    private fun isOverlappingWithExistingRegions(loc1: Location, loc2: Location): Boolean {
        val world = loc1.world ?: return false

        val newMinX = minOf(loc1.blockX, loc2.blockX)
        val newMaxX = maxOf(loc1.blockX, loc2.blockX)
        val newMinZ = minOf(loc1.blockZ, loc2.blockZ)
        val newMaxZ = maxOf(loc1.blockZ, loc2.blockZ)

        val existingRegions = RegionManager.getRegions(world)

        for (region in existingRegions) {
            val existingMinX = minOf(region.x, region.x2)
            val existingMaxX = maxOf(region.x, region.x2)
            val existingMinZ = minOf(region.z, region.z2)
            val existingMaxZ = maxOf(region.z, region.z2)

            if (!(newMaxX < existingMinX || newMinX > existingMaxX ||
                  newMaxZ < existingMinZ || newMinZ > existingMaxZ)) {
                return true
            }
        }

        return false
    }
}
