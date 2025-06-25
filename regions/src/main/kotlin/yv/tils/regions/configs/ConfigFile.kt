package yv.tils.regions.configs

import files.FileUtils
import logger.Logger
import yv.tils.regions.data.*

class ConfigFile {
    companion object {
        val config: MutableMap<String, Any> = mutableMapOf()

        fun getValue(key: String): Any? {
            return config[key]
        }

        fun getValueAsString(key: String): String? {
            return config[key]?.toString()
        }

        fun getValueAsInt(key: String): Int? {
            return config[key]?.toString()?.toIntOrNull()
        }

        fun getValueAsBoolean(key: String): Boolean? {
            return config[key]?.toString()?.toBoolean()
        }

        fun getFlags(): MutableMap<Flag, Any> {
            val flags: MutableMap<Flag, Any> = mutableMapOf()

            val globalFlagsBaseKey = "flags.global"
            val roleFlagsBaseKey = "flags.role_based"
            val lockedGlobalFlagsKey = "flags.locked.global"
            val lockedRoleFlagsKey = "flags.locked.role_based"

            for (flag in Flag.entries) {
                // Try getting the flag from global flags
                val globalFlag = config["$globalFlagsBaseKey.${flag.name}"] as? Boolean
                if (globalFlag != null) {
                    flags[flag] = globalFlag
                    continue
                }

                // Try getting the flag from role-based flags
                val roleFlag = config["$roleFlagsBaseKey.${flag.name}"] as? String
                if (roleFlag != null) {
                    flags[flag] = roleFlag
                    continue
                }

                // Try getting the flag from locked global flags
                val lockedGlobalFlag = config["$lockedGlobalFlagsKey.${flag.name}"] as? Boolean
                if (lockedGlobalFlag != null) {
                    flags[flag] = lockedGlobalFlag
                    continue
                }

                // Try getting the flag from locked role-based flags
                val lockedRoleFlag = config["$lockedRoleFlagsKey.${flag.name}"] as? String
                if (lockedRoleFlag != null) {
                    flags[flag] = lockedRoleFlag
                    continue
                }

                // If the flag is not found in any of the above, set it to default and locked
                flags[flag] = flag.defaultValue
            }

            return flags
        }

        fun getFlagTypes(): MutableMap<Flag, FlagType> {
            val flags: MutableMap<Flag, FlagType> = mutableMapOf()

            val globalFlagsBaseKey = "flags.global"
            val roleFlagsBaseKey = "flags.role_based"
            val lockedGlobalFlagsKey = "flags.locked.global"
            val lockedRoleFlagsKey = "flags.locked.role_based"

            for (flag in Flag.entries) {
                // Try getting the flag from global flags
                val globalFlag = config["$globalFlagsBaseKey.${flag.name}"] as? Boolean
                if (globalFlag != null) {
                    flags[flag] = FlagType.GLOBAL
                    continue
                }

                // Try getting the flag from role-based flags
                val roleFlag = config["$roleFlagsBaseKey.${flag.name}"] as? String
                if (roleFlag != null) {
                    flags[flag] = FlagType.ROLE_BASED
                    continue
                }

                // Try getting the flag from locked global flags
                val lockedGlobalFlag = config["$lockedGlobalFlagsKey.${flag.name}"] as? Boolean
                if (lockedGlobalFlag != null) {
                    flags[flag] = FlagType.LOCKED_GLOBAL
                    continue
                }

                // Try getting the flag from locked role-based flags
                val lockedRoleFlag = config["$lockedRoleFlagsKey.${flag.name}"] as? String
                if (lockedRoleFlag != null) {
                    flags[flag] = FlagType.LOCKED_ROLE_BASED
                    continue
                }

                // If the flag is not found in any of the above, set it to default and locked
                flags[flag] = flag.defaultGroup
            }

            return flags

        }
    }

    private val filePath = "/regions/config.yml"

    fun loadConfig() {
        val file = FileUtils.loadYAMLFile(filePath)

        for (key in file.content.getKeys(true)) {
            val value = file.content.get(key)

            Logger.debug("Loading config key: $key -> $value")
            config[key] = value as Any
        }
    }

    fun registerStrings(content: MutableMap<String, Any> = mutableMapOf()) {
        if (content.isEmpty()) {
            content["documentation"] = "https://docs.yvtils.net/modules/regions/config.yml"
        }

        content["settings.player.max.own"] = 5
        content["settings.player.max.member"] = -1
        content["settings.region.max.size"] = 1000
        content["settings.region.min.size"] = 1
        content["settings.region.max.members"] = -1

        content["flags.locked.global"] = mutableMapOf<String, Boolean>()
        content["flags.locked.role_based"] = mutableMapOf<String, String>()

        content["flags.global"] = mutableMapOf(
            Flag.PVP.name to Flag.PVP.defaultValue,
        )
        content["flags.role_based"] = mutableMapOf(
            Flag.PLACE.name to parseIDToName(Flag.PLACE.defaultValue as Int),
            Flag.DESTROY.name to parseIDToName(Flag.DESTROY.defaultValue as Int),
            Flag.CONTAINER.name to parseIDToName(Flag.CONTAINER.defaultValue as Int),
            Flag.INTERACT.name to parseIDToName(Flag.INTERACT.defaultValue as Int),
            Flag.TELEPORT.name to parseIDToName(Flag.TELEPORT.defaultValue as Int),
        )

        val ymlFile = FileUtils.makeYAMLFile(filePath, content)
        FileUtils.saveFile(filePath, ymlFile)
    }

    private fun parseIDToName(id: Int): String {
        return RegionRoles.fromID(id).name
    }
}
