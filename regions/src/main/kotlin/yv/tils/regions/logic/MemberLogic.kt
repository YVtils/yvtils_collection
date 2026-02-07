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

import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import yv.tils.config.language.LanguageHandler
import yv.tils.regions.configs.ConfigFile
import yv.tils.regions.data.*
import yv.tils.regions.language.LangStrings
import yv.tils.utils.logger.Logger
import yv.tils.utils.player.PlayerUtils
import java.util.*

class MemberLogic {
    companion object {
        fun addPlayerToRegion(region: String, player: OfflinePlayer, role: String?, sender: CommandSender) {
            val regionData = RegionManager.getRegionByNameOrID(region)
            val userRole = RegionRoles.fromString(role ?: "MEMBER")

            if (regionData == null) {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.REGION_MEMBER_ADD_FAILED.key,
                        sender,
                        mapOf<String, Any>(
                            "region" to region,
                            "yv/tils/player" to (player.name ?: "-"),
                        )
                    )
                )
                return
            }

            val regionMembershipCount = RegionManager.getRegions(player, RegionRoles.MEMBER, RegionRoles.OWNER)
            val maxRegions = ConfigFile.getValueAsInt("settings.region.max.members") ?: -1
            if (regionMembershipCount.size >= maxRegions && maxRegions != -1) {
                sender.sendMessage(LanguageHandler.getMessage(
                    LangStrings.REGION_MEMBER_ADD_FAILED_MAX_MEMBERSHIPS.key,
                    sender,
                    mapOf<String, Any>(
                        "maxRegions" to maxRegions.toString()
                    )
                ))
                return
            }

            val regionMembersCount = PlayerManager.getRegionMembers(UUID.fromString(regionData.id)) ?: emptyList()
            val maxMembers = ConfigFile.getValueAsInt("settings.region.max.members") ?: -1
            if (regionMembersCount.size >= maxMembers && maxMembers != -1) {
                sender.sendMessage(LanguageHandler.getMessage(
                    LangStrings.REGION_MEMBER_ADD_FAILED_MAX_MEMBERS.key,
                    sender,
                    mapOf<String, Any>(
                        "maxRegions" to maxMembers.toString()
                    )
                ))
                return
            }

            val isSuccessful = MemberLogic().addPlayerToRegion(regionData, player, userRole)

            Logger.debug("Added player ${player.name} to region ${regionData.name} with role $role")

            if (isSuccessful) {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.REGION_MEMBER_ADD_SUCCESS.key,
                        sender,
                        mapOf(
                            "region" to regionData.name,
                            "yv/tils/player" to (player.name ?: "-"),
                            "role" to userRole.toString()
                        )
                    )
                )
            } else {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.REGION_MEMBER_ADD_FAILED.key,
                        sender,
                        mapOf(
                            "region" to regionData.name,
                            "yv/tils/player" to (player.name ?: "-"),
                        )
                    )
                )
            }
        }

        fun removePlayerFromRegion(region: String, player: OfflinePlayer, sender: CommandSender) {
            val regionData = RegionManager.getRegionByNameOrID(region)

            if (regionData == null) {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.REGION_MEMBER_REMOVE_FAILED.key,
                        sender,
                        mapOf(
                            "region" to region,
                            "yv/tils/player" to (player.name ?: "-"),
                        )
                    )
                )
                return
            }

            val isSuccessful = MemberLogic().removePlayerFromRegion(regionData, player)

            Logger.debug("Removed player ${player.name} from region ${regionData.name}")

            if (isSuccessful) {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.REGION_MEMBER_REMOVE_SUCCESS.key,
                        sender,
                        mapOf(
                            "region" to regionData.name,
                            "yv/tils/player" to (player.name ?: "-")
                        )
                    )
                )
            } else {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.REGION_MEMBER_REMOVE_FAILED.key,
                        sender,
                        mapOf(
                            "region" to regionData.name,
                            "yv/tils/player" to (player.name ?: "-"),
                        )
                    )
                )
            }
        }

        fun changePlayerRoleInRegion(region: String, player: OfflinePlayer, sender: CommandSender, role: String?) {
            val regionData = RegionManager.getRegionByNameOrID(region)
            val role = RegionRoles.fromString(role ?: "MEMBER")

            if (regionData == null) {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.REGION_MEMBER_ROLE_FAILED.key,
                        sender,
                        mapOf(
                            "region" to region,
                            "yv/tils/player" to (player.name ?: "-"),
                        )
                    )
                )
                return
            }

            val isSuccessful = MemberLogic().changeRoleInRegion(regionData, player, role)

            Logger.debug("Changed player ${player.name} role in region ${regionData.name} to $role")

            if (isSuccessful) {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.REGION_MEMBER_ROLE_SUCCESS.key,
                        sender,
                        mapOf(
                            "region" to regionData.name,
                            "yv/tils/player" to (player.name ?: "-"),
                            "role" to role.toString(),
                        )
                    )
                )
            } else {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.REGION_MEMBER_ROLE_FAILED.key,
                        sender,
                        mapOf(
                            "region" to regionData.name,
                            "yv/tils/player" to (player.name ?: "-"),
                        )
                    )
                )
            }
        }

        fun getRegionMembers(region: RegionManager.RegionData, sender: CommandSender) {
            val members = MemberLogic().getMembersOfRegion(region)
            val owner = MemberLogic().getOwnerOfRegion(region)

            var membersList = members.joinToString(", ") { (PlayerUtils.uuidToName(it) ?: "-") +" (${MemberLogic().getPlayerRole(PlayerUtils.uuidToPlayer(it), region)})" }
            membersList += if (owner != null) {
                ", ${PlayerUtils.uuidToName(owner) ?: "-"} (Owner)"
            } else {
                "- (Owner)"
            }

            sender.sendMessage(
                LanguageHandler.getMessage(
                    LangStrings.REGION_MEMBERS_INFO.key,
                    sender,
                    mapOf(
                        "members" to membersList
                    )
                )
            )
        }
    }

    private fun addPlayerToRegion(region: RegionManager.RegionData, player: OfflinePlayer, role: RegionRoles): Boolean {
        if (isMemberOfRegion(player, region)) return false

        PlayerManager.addPlayerToRegion(player, region, role)
        return true
    }

    private fun removePlayerFromRegion(region: RegionManager.RegionData, player: OfflinePlayer): Boolean {
        if (!isMemberOfRegion(player, region)) return false

        PlayerManager.removePlayerFromRegion(player, region)
        return true
    }

    private fun changeRoleInRegion(region: RegionManager.RegionData, player: OfflinePlayer, role: RegionRoles): Boolean {
        if (!isMemberOfRegion(player, region)) return false

        PlayerManager.changeRoleInRegion(player, region, role)
        return true
    }

    private fun getMembersOfRegion(region: RegionManager.RegionData): List<UUID> {
        return PlayerManager.getRegionMembersAsUUIDs(UUID.fromString(region.id)) ?: emptyList()
    }

    private fun getOwnerOfRegion(region: RegionManager.RegionData): UUID? {
        return PlayerManager.getRegionOwnerAsUUID(UUID.fromString(region.id))
    }

    private fun isMemberOfRegion(player: OfflinePlayer, region: RegionManager.RegionData): Boolean {
        val regions = RegionManager.getRegions(player)
        return regions.any { it.id == region.id }
    }

    private fun getPlayerRole(player: OfflinePlayer, region: RegionManager.RegionData): RegionRoles {
        return PlayerChecks.regionRole(player, region)
    }
}
