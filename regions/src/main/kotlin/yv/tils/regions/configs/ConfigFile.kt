/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.regions.configs

import yv.tils.config.files.YMLFileUtils
import yv.tils.config.data.ConfigEntry
import yv.tils.config.data.EntryType
import yv.tils.regions.data.*
import yv.tils.utils.logger.Logger

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
    val file = YMLFileUtils.loadYAMLFile(filePath)

        for (key in file.content.getKeys(true)) {
            val value = file.content.get(key)

            Logger.debug("Loading config key: $key -> $value")
            config[key] = value as Any
        }
    }

    fun registerStrings(content: MutableMap<String, Any> = mutableMapOf()) {
        val entries = mutableListOf<ConfigEntry>()

        if (content.isEmpty()) {
            entries.add(ConfigEntry("documentation", EntryType.STRING, null, "https://docs.yvtils.net/modules/regions/config.yml", "Documentation URL"))
        } else {
            for ((k, v) in content) {
                val type = when (v) {
                    is Boolean -> EntryType.BOOLEAN
                    is Int -> EntryType.INT
                    is Double -> EntryType.DOUBLE
                    is List<*> -> EntryType.LIST
                    is Map<*, *> -> EntryType.MAP
                    is String -> EntryType.STRING
                    else -> EntryType.UNKNOWN
                }
                entries.add(ConfigEntry(k, type, null, v, null))
            }
        }

        // flags and settings defaults
        entries.add(ConfigEntry("settings.player.max.own", EntryType.INT, null, 5, "Max owned regions per player"))
        entries.add(ConfigEntry("settings.player.max.member", EntryType.INT, null, -1, "Max member regions per player"))
        entries.add(ConfigEntry("settings.region.max.size", EntryType.INT, null, 1000, "Max region size"))
        entries.add(ConfigEntry("settings.region.min.size", EntryType.INT, null, 1, "Min region size"))
        entries.add(ConfigEntry("settings.region.max.members", EntryType.INT, null, -1, "Max members per region"))

        entries.add(ConfigEntry("flags.locked.global", EntryType.MAP, null, mutableMapOf<String, Boolean>(), "Locked global flags"))
        entries.add(ConfigEntry("flags.locked.role_based", EntryType.MAP, null, mutableMapOf<String, String>(), "Locked role-based flags"))

        entries.add(ConfigEntry("flags.global.${Flag.PVP.name}", EntryType.BOOLEAN, null, Flag.PVP.defaultValue, "Global PVP flag"))

        entries.add(ConfigEntry("flags.role_based.${Flag.PLACE.name}", EntryType.STRING, null, parseIDToName(Flag.PLACE.defaultValue as Int), "Role-based PLACE flag"))
        entries.add(ConfigEntry("flags.role_based.${Flag.DESTROY.name}", EntryType.STRING, null, parseIDToName(Flag.DESTROY.defaultValue as Int), "Role-based DESTROY flag"))
        entries.add(ConfigEntry("flags.role_based.${Flag.CONTAINER.name}", EntryType.STRING, null, parseIDToName(Flag.CONTAINER.defaultValue as Int), "Role-based CONTAINER flag"))
        entries.add(ConfigEntry("flags.role_based.${Flag.INTERACT.name}", EntryType.STRING, null, parseIDToName(Flag.INTERACT.defaultValue as Int), "Role-based INTERACT flag"))
        entries.add(ConfigEntry("flags.role_based.${Flag.TELEPORT.name}", EntryType.STRING, null, parseIDToName(Flag.TELEPORT.defaultValue as Int), "Role-based TELEPORT flag"))

        val ymlFile = YMLFileUtils.makeYAMLFileFromEntries(filePath, entries)
        yv.tils.config.files.FileUtils.saveFile(filePath, ymlFile)
    }

    private fun parseIDToName(id: Int): String {
        return RegionRoles.fromID(id).name
    }
}
