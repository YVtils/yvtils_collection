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

import jdk.jfr.internal.event.EventConfiguration.timestamp
import kotlinx.serialization.Serializable
import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.configv2.files.JsonFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger
import java.util.UUID

import yv.tils.moderation.configs.saveFile.MuteSave
import yv.tils.utils.logger.DEBUG_LEVEL

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
        val file = runCatching { JsonFileUtils.loadJsonFile("/moderation/warnedPlayers.json") }.getOrNull() ?: return
        val saveListNode = file.node.node("saves")

        if (saveListNode.virtual() || !saveListNode.isList()) {
            Logger.debug("No saves found in the save file.", DEBUG_LEVEL.SPAM)
            return
        }

        for (saveNode in saveListNode.childrenList()) {
            Logger.debug("Loading save: ${saveNode.raw()}", DEBUG_LEVEL.SPAM)

            val uuid = saveNode.node("uuid").string ?: continue
            val warningCount = saveNode.node("warningCount").get(Int::class.javaObjectType) ?: continue

            val warningsNode = saveNode.node("warnings")
            if (warningsNode.virtual() || !warningsNode.isList()) continue

            val warnings = mutableListOf<Warning>()

            for (warningNode in warningsNode.childrenList()) {
                Logger.debug("Loading warning: ${warningNode.raw()}", DEBUG_LEVEL.SPAM)

                val warnID = warningNode.node("id").string ?: continue
                val reason = warningNode.node("reason").string ?: continue
                val modActionNode = warningNode.node("modAction")
                if (modActionNode.virtual()) continue
                val modUUID = modActionNode.node("uuid").string ?: continue
                val timestamp = modActionNode.node("timestamp").string ?: continue

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
        val saveWrapper = mapOf(
            "saves" to saveList.map {
                mapOf(
                    "uuid" to it.uuid,
                    "warningCount" to it.warningCount,
                    "warnings" to it.warnings.map { warning ->
                        mapOf(
                            "id" to warning.id,
                            "reason" to warning.reason,
                            "modAction" to mapOf(
                                "uuid" to warning.modAction.uuid,
                                "timestamp" to warning.modAction.timestamp,
                            ),
                        )
                    },
                )
            }
        )
        val jsonFile = JsonFileUtils.makeJsonFile("/moderation/warnedPlayers.json", saveWrapper)
        ConfigurateFileUtils.update(jsonFile, overwriteExisting = true)
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