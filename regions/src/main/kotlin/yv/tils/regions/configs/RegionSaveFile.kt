/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.regions.configs

import yv.tils.configv2.files.ObjectMapperFileUtils
import yv.tils.regions.data.RegionManager
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.data.UUID as UUIDRegistry
import yv.tils.utils.logger.Logger
import java.util.*

class RegionSaveFile {
    private val filePath = "/regions/region_save.json"

    fun loadConfig() {
        val loaded = ObjectMapperFileUtils.loadList<RegionManager.RegionData>(filePath, key = "regions")

        if (loaded.isEmpty()) {
            registerStrings()
            return
        }

        loaded.forEach {
            try {
                val regionUUID = UUID.fromString(it.id)
                UUIDRegistry.registerUUID(regionUUID)
                RegionManager.loadRegion(regionUUID, it)
            } catch (e: Exception) {
                Logger.error("Failed to load region: ${e.message}")
            }
        }
    }

    fun registerStrings(saveList: MutableList<RegionManager.RegionData> = mutableListOf()) {
        ObjectMapperFileUtils.saveList(filePath, saveList, key = "regions")
    }

    fun updateRegionSetting(uuid: UUID, content: RegionManager.RegionData?) {
        RegionManager.loadRegion(uuid, content)

        Logger.debug("Updating region setting: $uuid -> $content")

        CoroutineHandler.launchTask(
            suspend { registerStrings(RegionManager.saveRegion().values.toMutableList()) },
            null,
            isOnce = true,
        )
    }
}
