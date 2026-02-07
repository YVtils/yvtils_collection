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

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.regions.data.*
import yv.tils.regions.language.LangStrings
import yv.tils.utils.logger.Logger
import javax.naming.NoPermissionException

class FlagLogic {
    companion object {
        fun getFlagList(sender: CommandSender, region: String?) {

        }

        fun changeFlag(
            sender: CommandSender,
            region: String?,
            flagType: String,
            value: Boolean? = null,
            role: RegionRoles? = null
        ) {
            if ((value == null && role == null) || (value != null && role != null)) {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.REGION_FLAG_CHANGE_FAIL_INVALID.key,
                        sender,
                    )
                )
                return
            }

            val regionData = if (region == null) {
                if (sender is Player) {
                    RegionLogic.getRegion(sender.location) ?: run {
                        sender.sendMessage(
                            LanguageHandler.getMessage(
                                LangStrings.REGION_GENERIC_NONE.key,
                                sender.uniqueId,
                            )
                        )
                        return
                    }
                } else {
                    sender.sendMessage(
                        LanguageHandler.getMessage(
                            LangStrings.REGION_GENERIC_NONE.key,
                            sender,
                        )
                    )
                    return
                }
            } else {
                RegionManager.getRegionByNameOrID(region) ?: run {
                    sender.sendMessage(
                        LanguageHandler.getMessage(
                            LangStrings.REGION_GENERIC_NONE.key,
                            sender,
                        )
                    )
                    return
                }
            }

            val flag: Flag

            try {
                flag = Flag.fromString(flagType.uppercase())
            } catch (_: IllegalArgumentException) {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.REGION_FLAG_CHANGE_FAIL_INVALID.key,
                        sender,
                        mapOf("flag" to flagType)
                    )
                )
                return
            }

            regionData.flags

            try {
                val isSuccessful = FlagManager.setFlagEntry(flag, value ?: role!!, regionData, sender)
                if (!isSuccessful) {
                    sender.sendMessage(
                        LanguageHandler.getMessage(
                            LangStrings.REGION_FLAG_CHANGE_FAIL_INVALID.key,
                            sender,
                            mapOf("flag" to flagType)
                        )
                    )
                    return
                }

                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.REGION_FLAG_CHANGE_SUCCESS.key,
                        sender,
                        mapOf(
                            "flag" to flagType,
                            "value" to (value ?: role.toString()),
                            "region" to regionData.name
                        )
                    )
                )
            } catch (_: NoPermissionException) {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.REGION_FLAG_CHANGE_FAIL_NO_PERMISSION.key,
                        sender,
                    )
                )
                return
            }
        }

        fun flagCheck(region: RegionManager.RegionData, flag: Flag, playerRole: RegionRoles): Boolean {
            val globalFlag = region.flags.global[flag]
            val roleBasedFlag = region.flags.roleBased[flag]

            if (globalFlag != null) {
                Logger.debug("Global flag: $globalFlag")
                return globalFlag
            }

            if (roleBasedFlag != null) {
                Logger.debug("Role based flag: $roleBasedFlag")
                if (roleBasedFlag == RegionRoles.NONE.permLevel) {
                    Logger.debug("Role based flag: $roleBasedFlag is NONE")
                    return true
                }

                if (roleBasedFlag >= playerRole.permLevel && playerRole != RegionRoles.NONE) {
                    Logger.debug("Role based flag: $roleBasedFlag is greater than or equal to player role: ${playerRole.permLevel}")
                    return true
                }

                return false
            }

            return false
        }
    }

    /**
     * Returns if flag is global or role based.
     * @param flag The flag to check.
     * @return The flag type.
     */
    fun getFlagType(flag: Flag): FlagType? {
        return FlagManager.flagEntryList[flag]?.type
    }
}
