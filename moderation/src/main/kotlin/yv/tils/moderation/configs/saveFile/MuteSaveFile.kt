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

package yv.tils.moderation.configs.saveFile

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import yv.tils.config.files.FileUtils
import yv.tils.config.files.JSONFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger
import yv.tils.utils.player.PlayerUtils
import java.util.*

class MuteSaveFile {
    companion object {
        val saves = mutableMapOf<UUID, MuteSave>()
    }

    //  {
    //      saves: [
    //          {
    //              "uuid": <uuid>,
    //              "reason": <reason>,
    //              "muted": <muted>,
    //              "expires": <timestamp>,
    //              "modAction": {
    //                  "uuid": <modUUID>,
    //                  "timestamp": <timestamp>
    //              }
    //      ]
    //  }

    fun loadConfig() {
        val file = JSONFileUtils.loadJSONFile("/moderation/mutedPlayers.json")
        val jsonFile = file.content
        val saveList = jsonFile["saves"]?.jsonArray ?: return

        if (saveList.isEmpty()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (save in saveList) {
            Logger.debug("Loading save: $save")

            val uuid = save.jsonObject["uuid"]?.toString()?.replace("\"", "") ?: continue
            val reason = save.jsonObject["reason"]?.toString()?.replace("\"", "") ?: continue
            val muted = save.jsonObject["muted"]?.toString()?.toBoolean() ?: continue
            val expires = save.jsonObject["expires"]?.toString()?.replace("\"", "") ?: continue

            val modActionJSON = save.jsonObject["modAction"]?.jsonObject ?: continue
            val modUUID = modActionJSON.jsonObject["uuid"]?.toString() ?: continue
            val timestamp = modActionJSON.jsonObject["timestamp"]?.toString()?.replace("\"", "") ?: continue

            val modAction = ModAction(
                modUUID,
                timestamp
            )

            saves[UUID.fromString(uuid)] = MuteSave(uuid, reason, muted, expires, modAction)
        }
    }

    fun registerStrings(saveList: MutableList<MuteSave> = mutableListOf()) {
        val saveWrapper = mapOf("saves" to saveList)
        val jsonFile = JSONFileUtils.makeJSONFile("/moderation/mutedPlayers.json", saveWrapper)
        FileUtils.updateFile("/moderation/mutedPlayers.json", jsonFile, true)
    }

    fun mutePlayer(uuid: UUID, reason: String, muted: Boolean, modAction: ModAction, expires: String = "null") {
        saves[uuid] = MuteSave(uuid.toString(), reason, muted, expires, modAction)

        CoroutineHandler.launchTask(
            suspend { registerStrings(saves.values.toMutableList()) },
            null,
            isOnce = true,
        )
    }

    fun getMuteInfo(uuid: UUID): MuteSave? {
        return saves[uuid]
    }

    fun unmutePlayer(uuid: UUID) {
        saves.remove(uuid)

        CoroutineHandler.launchTask(
            suspend { registerStrings(saves.values.toMutableList()) },
            null,
            isOnce = true,
        )
    }

    fun getAllMutes(): Array<String> {
        val mutedPlayers: MutableList<String> = mutableListOf()
        for (uuid in saves.keys) {
            val player = PlayerUtils.uuidToName(uuid)
            if (player != null) {
                mutedPlayers.add(player)
            } else {
                mutedPlayers.add(uuid.toString())
            }
        }
        return mutedPlayers.toTypedArray()
    }
}