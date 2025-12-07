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

package yv.tils.moderation.configs.saveFile

import jdk.jfr.internal.event.EventConfiguration.timestamp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import yv.tils.config.files.FileUtils
import yv.tils.config.files.JSONFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger
import java.util.UUID

import yv.tils.moderation.configs.saveFile.MuteSave
import yv.tils.utils.logger.DEBUGLEVEL

//  {
//      saves: [
//          {
//              "uuid": <uuid>,
//              "warningCount": <number>,
//              "warnings": [
//                  {
//                      "id": <number>,
//                      "reason": <reason>,
//                      "modAction": {
//                          "uuid": <uuid>,
//                          "timestamp": <timestamp>
//                      }
//                  }
//              ]
//      ]
//  }

class WarnSaveFile {
    companion object {
        val saves = mutableMapOf<UUID, WarnSave>()
    }

    fun loadConfig() {
        val file = JSONFileUtils.loadJSONFile("/moderation/warnedPlayers.json")
        val jsonFile = file.content
        val saveList = jsonFile["saves"]?.jsonArray ?: return

        if (saveList.isEmpty()) {
            Logger.debug("No saves found in the save file.", DEBUGLEVEL.SPAM)
            return
        }

        for (save in saveList) {
            Logger.debug("Loading save: $save", DEBUGLEVEL.SPAM)

            val uuid = save.jsonObject["uuid"]?.toString()?.replace("\"", "") ?: continue
            val warningCount = save.jsonObject["warningCount"]?.toString()?.toInt() ?: continue

            val warningsJSON = save.jsonObject["warnings"]?.jsonArray ?: continue

            val warnings = mutableListOf<Warning>()

            for (warning in warningsJSON) {
                Logger.debug("Loading warning: $warning", DEBUGLEVEL.SPAM)

                val warnID = warning.jsonObject["id"]?.toString()?.replace("\"", "") ?: continue
                val reason = warning.jsonObject["reason"]?.toString()?.replace("\"", "") ?: continue
                val modActionJSON = warning.jsonObject["modAction"]?.jsonObject ?: continue
                val modUUID = modActionJSON.jsonObject["uuid"]?.toString()?.replace("\"", "") ?: continue
                val timestamp = modActionJSON.jsonObject["timestamp"]?.toString()?.replace("\"", "") ?: continue

                val modAction = ModAction(
                    uuid = modUUID,
                    timestamp = timestamp,
                )

                val warning = Warning(
                    id = warnID,
                    reason = reason,
                    modAction = modAction
                )

                warnings.add(warning)
            }

            saves[UUID.fromString(uuid)] = WarnSave(uuid, warningCount, warnings)
        }
    }

    fun registerStrings(saveList: MutableList<WarnSave> = mutableListOf()) {
        val saveWrapper = mapOf("saves" to saveList)
        val jsonFile = JSONFileUtils.makeJSONFile("/moderation/warnedPlayers.json", saveWrapper)
        FileUtils.updateFile("/moderation/warnedPlayers.json", jsonFile, true)
    }

    fun warnPlayer(uuid: UUID, newWarn: Warning) {
        val warnSave = saves[uuid]

        if (warnSave != null) {
            warnSave.warningCount += 1
            warnSave.warnings.add(newWarn)
        } else {
            saves[uuid] = WarnSave(
                uuid = uuid.toString(),
                warningCount = 1,
                warnings = mutableListOf(newWarn)
            )
        }
    }

    fun getWarnings(uuid: UUID): List<Warning> {
        val warnSave = saves[uuid]
        return warnSave?.warnings ?: emptyList()
    }

    fun getWarningCount(uuid: UUID): Int {
        val warnSave = saves[uuid]
        return warnSave?.warningCount ?: 0
    }

    fun removeWarning(uuid: UUID, warningID: String) {
        val warnSave = saves[uuid] ?: return

        val warningToRemove = warnSave.warnings.find { it.id == warningID } ?: return

        warnSave.warnings.remove(warningToRemove)
        warnSave.warningCount -= 1
    }

    fun clearWarnings(uuid: UUID) {
        saves.remove(uuid)
    }
}