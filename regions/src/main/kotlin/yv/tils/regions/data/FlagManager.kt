package yv.tils.regions.data

import kotlinx.serialization.Serializable
import logger.Logger
import org.bukkit.command.CommandSender
import yv.tils.regions.configs.ConfigFile
import yv.tils.regions.configs.RegionSaveFile
import yv.tils.regions.data.Flag.PLACE
import yv.tils.regions.data.Flag.PVP
import java.util.*
import javax.naming.NoPermissionException

class FlagManager {
    companion object {
        var flagEntryList = mutableMapOf<Flag, FlagEntry>()

        fun getFlagList(): Map<Flag, FlagEntry> {
            return flagEntryList
        }

        fun getFlagEntry(flag: Flag): FlagEntry? {
            return flagEntryList[flag]
        }

        /**
         * Set the value of a flag for a region.
         * @param flag The flag to set.
         * @param value The value to set the flag to.
         * @param region The region to set the flag for.
         * @param sender The sender of the command (optional).
         * @return True if the flag was set successfully, false otherwise.
         *
         * @throws NoPermissionException if the sender does not have permission to set the flag.
         */
        fun setFlagEntry(flag: Flag, value: Any, region: RegionManager.RegionData, sender: CommandSender? = null): Boolean {
            val entry = flagEntryList[flag]
            val flagType: FlagType = entry?.type ?: return false

            if (sender != null) {
                if (flagType == FlagType.LOCKED_GLOBAL || flagType == FlagType.LOCKED_ROLE_BASED) {
                    if (sender.hasPermission(Permissions.REGION_FLAGS_LOCKED.permission) || sender.isOp) {
                        Logger.debug("Sender ${sender.name} has permission to set locked flag $flag")
                    } else {
                        throw NoPermissionException("Sender ${sender.name} does not have permission to set locked flag $flag")
                    }
                }
            }

            flagEntryList[flag] = entry.copy(value = value)


            // Update the flag in the config file
            val regionFlags = region.flags

            if (flagType == FlagType.GLOBAL || flagType == FlagType.LOCKED_GLOBAL) {
                if (value !is Boolean) {
                    Logger.debug("Invalid value type for global flag $flag: $value")
                    return false
                }
            } else if (flagType == FlagType.ROLE_BASED || flagType == FlagType.LOCKED_ROLE_BASED) {
                if (value !is RegionRoles) {
                    Logger.debug("Invalid value type for role-based flag $flag: $value")
                    return false
                }
            }

            when (flagType) {
                FlagType.GLOBAL -> {
                    regionFlags.global[flag] = value as Boolean
                }
                FlagType.ROLE_BASED -> {
                    regionFlags.roleBased[flag] = (value as RegionRoles).permLevel
                }
                FlagType.LOCKED_GLOBAL -> {
                    regionFlags.lockedGlobal[flag] = value as Boolean
                }
                FlagType.LOCKED_ROLE_BASED -> {
                    regionFlags.lockedRoleBased[flag] = (value as RegionRoles).permLevel
                }
            }

            region.flags = regionFlags

            Logger.debug("Changed flag $flag to $value for region ${region.name}")

            RegionSaveFile().updateRegionSetting(
                UUID.fromString(region.id),
                region
            )

            return true
        }
    }

    fun initFlagList() {
        val flagList =  ConfigFile.getFlagTypes()
        val configuredFlags = ConfigFile.getFlags()

        for (flag in flagList.keys) {
            val type = flagList[flag] ?: continue
            val value = configuredFlags[flag] ?: flag.defaultValue

            flagEntryList[flag] = FlagEntry(flag, type, value)
        }
    }

    data class FlagEntry(
        val flag: Flag,
        val type: FlagType,
        val value: Any
    )

    /**
     * Data class representing the flags for a region.
     *
     * @property global A map of global flags and their values.
     * @property roleBased A map of role-based flags and their values.
     * @property lockedGlobal A map of locked global flags and their values.
     * @property lockedRoleBased A map of locked role-based flags and their values.
     */
    @Serializable
    data class RegionFlags(
        /**
         * MutableMap of global flags and their values.
         * The key is the flag type and the value is a boolean indicating if the flag is set.
         *
         * @property Flag The type of flag.
         * @property Boolean The value of the flag. If true, the action is allowed, if false, it is not.
         */
        val global: MutableMap<Flag, Boolean>,

        /**
         * MutableMap of role-based flags and their values.
         * The key is the flag type,
         * and the value is an integer indicating the permission level required to set the flag.
         *
         * @property Flag The type of flag.
         * @property Int The minimum permission level required setting the flag.
         */
        val roleBased: MutableMap<Flag, Int>,

        /**
         * MutableMap of locked global flags and their values.
         * The key is the flag type and the value is a boolean indicating if the flag is set.
         *
         * @property Flag The type of flag.
         * @property Boolean The value of the flag. If true, the action is allowed, if false, it is not.
         */
        val lockedGlobal: MutableMap<Flag, Boolean> = mutableMapOf(),

        /**
         * MutableMap of locked role-based flags and their values.
         * The key is the flag type,
         * and the value is an integer indicating the permission level required to set the flag.
         *
         * @property Flag The type of flag.
         * @property Int The minimum permission level required setting the flag.
         */
        val lockedRoleBased: MutableMap<Flag, Int> = mutableMapOf()
    )
}

/**
 * Enum class representing the different types of flags that can be set in a region.
 *
 * @property PVP Flag for player vs player combat.
 * @property PLACE Flag for building in the region.
 *
 */
enum class Flag(val defaultValue: Any, val defaultGroup: FlagType) {
    PVP(true, FlagType.GLOBAL),
    PLACE(3, FlagType.ROLE_BASED),
    DESTROY(3, FlagType.ROLE_BASED),
    CONTAINER(3, FlagType.ROLE_BASED),
    INTERACT(3, FlagType.ROLE_BASED),
    USE(3, FlagType.ROLE_BASED),
    TELEPORT(3, FlagType.ROLE_BASED),;

    companion object {
        /**
         * Get flag from string.
         * @param role The string representation of the flag.
         * @return The corresponding Flag.
         * @throws IllegalArgumentException if the string does not match any flag.
         */
        fun fromString(role: String): Flag {
            return Flag.entries.firstOrNull { it.name.equals(role, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid flag type: $role")
        }
    }
}

enum class FlagType {
    GLOBAL,
    ROLE_BASED,
    LOCKED_GLOBAL,
    LOCKED_ROLE_BASED;

    companion object {
        fun fromString(type: String): FlagType {
            return FlagType.entries.firstOrNull { it.name.equals(type, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid flag type: $type")
        }
    }
}