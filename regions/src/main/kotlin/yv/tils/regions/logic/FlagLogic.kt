package yv.tils.regions.logic

import logger.Logger
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.regions.configs.RegionSaveFile
import yv.tils.regions.data.*
import java.util.*
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
                // TODO
                Logger.dev("Invalid flag change request: $value, $role")
                return
            }

            val regionData = if (region == null) {
                if (sender is Player) {
                    RegionLogic.getRegion(sender.location) ?: run {
                        // TODO
                        Logger.dev("Region not found for player ${sender.name}")
                        return
                    }
                } else {
                    // TODO
                    Logger.dev("Region not found for console sender")
                    return
                }
            } else {
                RegionManager.getRegionByNameOrID(region) ?: run {
                    // TODO
                    Logger.dev("Region not found for region $region")
                    return
                }
            }

            val flag: Flag

            try {
                flag = Flag.fromString(flagType.uppercase())
            } catch (_: IllegalArgumentException) {
                // TODO
                Logger.dev("Invalid flag type: $flagType")
                return
            }

            val regionFlags = regionData.flags

            try {
                val isSuccessful = FlagManager.setFlagEntry(flag, value ?: role!!, regionData, sender)
                if (!isSuccessful) {
                    // TODO
                    Logger.dev("Failed to set flag $flag for region ${regionData.name}")
                    return
                }
            } catch (_: NoPermissionException) {
                // TODO
                Logger.dev("Sender ${sender.name} does not have permission to set flag $flag")
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