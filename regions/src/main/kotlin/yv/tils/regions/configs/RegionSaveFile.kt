package yv.tils.regions.configs

import coroutine.CoroutineHandler
import files.FileUtils
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import logger.Logger
import yv.tils.regions.data.FlagType
import yv.tils.regions.data.RegionManager
import java.util.*

class RegionSaveFile {
    companion object {
        val saves = mutableMapOf<UUID, RegionManager.RegionData>()
    }

    private val filePath = "/regions/region_save.json"

    fun loadConfig() {
        val file = FileUtils.loadJSONFile(filePath)
        val jsonFile = file.content
        val saveList = jsonFile["regions"]?.jsonArray ?: return

        if (saveList.isEmpty()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (save in saveList) {
            Logger.debug("Loading save: $save")

            val id = save.jsonObject["id"]?.toString() ?: continue
            val name = save.jsonObject["name"]?.toString() ?: continue
            val world = save.jsonObject["world"]?.toString() ?: continue
            val x = save.jsonObject["x"]?.toString()?.toInt() ?: continue
            val z = save.jsonObject["z"]?.toString()?.toInt() ?: continue
            val x2 = save.jsonObject["x2"]?.toString()?.toInt() ?: continue
            val z2 = save.jsonObject["z2"]?.toString()?.toInt() ?: continue
            val created = save.jsonObject["created"]?.toString()?.toLong() ?: continue
            val flags = save.jsonObject["flags"]?.jsonObject ?: continue
            val globalFlags = flags["global"]?.jsonObject ?: continue
            val roleBasedFlags = flags["roleBased"]?.jsonObject ?: continue

            val global = mutableMapOf<FlagType, Boolean>()
            val roleBased = mutableMapOf<FlagType, Int>()
            for (flag in globalFlags) {
                val flagKey = FlagType.valueOf(flag.key)
                global[flagKey] = flag.value.toString().toBoolean()
            }
            for (flag in roleBasedFlags) {
                val flagKey = FlagType.valueOf(flag.key)
                roleBased[flagKey] = flag.value.toString().toInt()
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
                flags = RegionManager.RegionFlags(
                    global = global,
                    roleBased = roleBased
                )
            )

            val regionUUID = UUID.fromString(id)
            data.UUID.registerUUID(regionUUID)

            if (saves.containsKey(regionUUID)) {
                saves[regionUUID] = region
            } else {
                saves[regionUUID] = region
            }
        }
    }

    fun registerStrings(saveList: MutableList<RegionManager.RegionData> = mutableListOf()) {
        val saveWrapper = mapOf("regions" to saveList)
        val jsonFile = FileUtils.makeJSONFile(filePath, saveWrapper)
        FileUtils.updateFile(filePath, jsonFile)
    }

    fun updateRegionSetting(uuid: UUID, content: RegionManager.RegionData) {
        saves[uuid] = content

        Logger.debug("Updating region setting: $uuid -> $content")

        CoroutineHandler.launchTask(
            suspend { registerStrings(saves.values.toMutableList()) },
            null,
            isOnce = true,
        )
    }
}