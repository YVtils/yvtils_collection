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

package yv.tils.regions.configs

import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.configv2.files.JsonFileUtils
import yv.tils.regions.data.*
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger
import java.util.*

class RegionSaveFile {
    private val filePath = "/regions/region_save.json"

    fun loadConfig() {
        val file = runCatching { JsonFileUtils.loadJsonFile(filePath) }.getOrNull() ?: return
        val saveListNode = file.node.node("regions")

        if (saveListNode.virtual() || !saveListNode.isList()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (saveNode in saveListNode.childrenList()) {
            Logger.debug("Loading save: ${saveNode.raw()}")

            try {
                val id = saveNode.node("id").string ?: continue
                val name = saveNode.node("name").string ?: continue
                val world = saveNode.node("world").string ?: continue
                val x = saveNode.node("x").get(Int::class.javaObjectType) ?: continue
                val z = saveNode.node("z").get(Int::class.javaObjectType) ?: continue
                val x2 = saveNode.node("x2").get(Int::class.javaObjectType) ?: continue
                val z2 = saveNode.node("z2").get(Int::class.javaObjectType) ?: continue
                val created = saveNode.node("created").get(Long::class.javaObjectType) ?: continue
                val flagsNode = saveNode.node("flags")
                val globalFlagsNode = flagsNode.node("global")
                val roleBasedFlagsNode = flagsNode.node("roleBased")

                val global = mutableMapOf<Flag, Boolean>()
                val roleBased = mutableMapOf<Flag, Int>()
                for ((flagKeyRaw, flagNode) in globalFlagsNode.childrenMap()) {
                    val flagKey = try {
                        Flag.valueOf(flagKeyRaw.toString())
                    } catch (e: IllegalArgumentException) {
                        Logger.warn("Invalid flag key $flagKeyRaw for region $id: ${e.message}")
                        continue
                    }
                    global[flagKey] = flagNode.get(Boolean::class.javaObjectType) ?: continue
                }
                for ((flagKeyRaw, flagNode) in roleBasedFlagsNode.childrenMap()) {
                    val flagKey = try {
                        Flag.valueOf(flagKeyRaw.toString())
                    } catch (e: IllegalArgumentException) {
                        Logger.warn("Invalid flag key $flagKeyRaw for region $id: ${e.message}")
                        continue
                    }
                    roleBased[flagKey] = flagNode.get(Int::class.javaObjectType) ?: continue
                }

                val region = RegionManager.RegionData(
                    id = id,
                    name = name,
                    world = world,
                    x = x,
                    z = z,
                    x2 = x2,
                    z2 = z2,
                    created = created,
                    flags = FlagManager.RegionFlags(
                        global = global,
                        roleBased = roleBased
                    )
                )

                val regionUUID = UUID.fromString(id)
                yv.tils.utils.data.UUID.registerUUID(regionUUID)

                RegionManager.loadRegion(regionUUID, region)
            } catch (e: Exception) {
                Logger.error("Failed to load region: ${e.message}")
            }
        }
    }

    fun registerStrings(saveList: MutableList<RegionManager.RegionData> = mutableListOf()) {
        val saveWrapper = mapOf("regions" to saveList.map { regionToMap(it) })
        val jsonFile = JsonFileUtils.makeJsonFile(filePath, saveWrapper)
        ConfigurateFileUtils.update(jsonFile, overwriteExisting = false)
    }

    fun updateRegionSetting(uuid: UUID, content: RegionManager.RegionData?) {
        RegionManager.loadRegion(uuid, content)

        Logger.debug("Updating region setting: $uuid -> $content")

        CoroutineHandler.launchTask(
            suspend { upgradeStrings(RegionManager.saveRegion().values.toMutableList()) },
            null,
            isOnce = true,
        )
    }

    private fun upgradeStrings(saveList: MutableList<RegionManager.RegionData> = mutableListOf()) {
        val saveWrapper = mapOf("regions" to saveList.map { regionToMap(it) })
        val jsonFile = JsonFileUtils.makeJsonFile(filePath, saveWrapper)
        ConfigurateFileUtils.update(jsonFile, overwriteExisting = true)
    }

    /**
     * Converts a [RegionManager.RegionData] into a plain nested
     * `Map<String, Any?>`/`List` structure that [JsonFileUtils.makeJsonFile]
     * (via `ConfigurationNode.raw()`) can persist directly - the
     * Configurate-backed replacement for what `kotlinx.serialization`'s
     * `Json.encodeToString` used to do for `@Serializable` classes.
     *
     * Enum-keyed flag maps (`Map<Flag, Boolean>`/`Map<Flag, Int>`) are
     * converted to `Map<String, ...>` via [Flag.name], matching how
     * [loadConfig] reads them back with `Flag.valueOf(key)` and how the
     * original `kotlinx.serialization` output represented enum map keys.
     */
    private fun regionToMap(region: RegionManager.RegionData): Map<String, Any?> = mapOf(
        "id" to region.id,
        "name" to region.name,
        "world" to region.world,
        "x" to region.x,
        "z" to region.z,
        "x2" to region.x2,
        "z2" to region.z2,
        "created" to region.created,
        "flags" to mapOf(
            "global" to region.flags.global.mapKeys { it.key.name },
            "roleBased" to region.flags.roleBased.mapKeys { it.key.name },
            "lockedGlobal" to region.flags.lockedGlobal.mapKeys { it.key.name },
            "lockedRoleBased" to region.flags.lockedRoleBased.mapKeys { it.key.name },
        ),
    )
}
