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

import kotlinx.serialization.json.*
import yv.tils.config.files.JSONFileUtils
import yv.tils.regions.data.*
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger
import java.util.*

class RegionSaveFile {
    private val filePath = "/regions/region_save.json"

    fun loadConfig() {
    val file = JSONFileUtils.loadJSONFile(filePath)
    val jsonFile = file.content
        val saveList = jsonFile["regions"]?.jsonArray ?: return

        if (saveList.isEmpty()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (save in saveList) {
            Logger.debug("Loading save: $save")

            try {
                val id = save.jsonObject["id"]?.jsonPrimitive?.content ?: continue
                val name = save.jsonObject["name"]?.jsonPrimitive?.content ?: continue
                val world = save.jsonObject["world"]?.jsonPrimitive?.content ?: continue
                val x = save.jsonObject["x"]?.jsonPrimitive?.content?.toInt() ?: continue
                val z = save.jsonObject["z"]?.jsonPrimitive?.content?.toInt() ?: continue
                val x2 = save.jsonObject["x2"]?.jsonPrimitive?.content?.toInt() ?: continue
                val z2 = save.jsonObject["z2"]?.jsonPrimitive?.content?.toInt() ?: continue
                val created = save.jsonObject["created"]?.jsonPrimitive?.content?.toLong() ?: continue
                val flags = save.jsonObject["flags"]?.jsonObject ?: continue
                val globalFlags = flags["global"]?.jsonObject ?: continue
                val roleBasedFlags = flags["roleBased"]?.jsonObject ?: continue

                val global = mutableMapOf<Flag, Boolean>()
                val roleBased = mutableMapOf<Flag, Int>()
                for (flag in globalFlags) {
                    val flagKey = try {
                        Flag.valueOf(flag.key)
                    } catch (e: IllegalArgumentException) {
                        Logger.warn("Invalid flag key ${flag.key} for region $id: ${e.message}")
                        continue
                    }
                    global[flagKey] = flag.value.jsonPrimitive.content.toBoolean()
                }
                for (flag in roleBasedFlags) {
                    val flagKey = try {
                        Flag.valueOf(flag.key)
                    } catch (e: IllegalArgumentException) {
                        Logger.warn("Invalid flag key ${flag.key} for region $id: ${e.message}")
                        continue
                    }
                    roleBased[flagKey] = flag.value.jsonPrimitive.content.toInt()
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
        val saveWrapper = mapOf("regions" to saveList)
    val jsonFile = JSONFileUtils.makeJSONFile(filePath, saveWrapper)
    yv.tils.config.files.FileUtils.updateFile(filePath, jsonFile)
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
        val saveWrapper = mapOf("regions" to saveList)
    val jsonFile = JSONFileUtils.makeJSONFile(filePath, saveWrapper)
    yv.tils.config.files.FileUtils.updateFile(filePath, jsonFile, true)
    }
}
