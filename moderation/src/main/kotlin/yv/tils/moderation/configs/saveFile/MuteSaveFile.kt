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

import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.configv2.files.JsonFileUtils
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
        val file = runCatching { JsonFileUtils.loadJsonFile("/moderation/mutedPlayers.json") }.getOrNull() ?: return
        val saveListNode = file.node.node("saves")

        if (saveListNode.virtual() || !saveListNode.isList()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (saveNode in saveListNode.childrenList()) {
            Logger.debug("Loading save: ${saveNode.raw()}")

            val uuid = saveNode.node("uuid").string ?: continue
            val reason = saveNode.node("reason").string ?: continue
            val muted = saveNode.node("muted").get(Boolean::class.javaObjectType) ?: continue
            val expires = saveNode.node("expires").string ?: continue

            val modActionNode = saveNode.node("modAction")
            if (modActionNode.virtual()) continue
            val modUUID = modActionNode.node("uuid").string ?: continue
            val timestamp = modActionNode.node("timestamp").string ?: continue

            val modAction = ModAction(
                modUUID,
                timestamp
            )

            saves[UUID.fromString(uuid)] = MuteSave(uuid, reason, muted, expires, modAction)
        }
    }

    fun registerStrings(saveList: MutableList<MuteSave> = mutableListOf()) {
        val saveWrapper = mapOf(
            "saves" to saveList.map {
                mapOf(
                    "uuid" to it.uuid,
                    "reason" to it.reason,
                    "muted" to it.muted,
                    "expires" to it.expires,
                    "modAction" to mapOf(
                        "uuid" to it.modAction.uuid,
                        "timestamp" to it.modAction.timestamp,
                    ),
                )
            }
        )
        val jsonFile = JsonFileUtils.makeJsonFile("/moderation/mutedPlayers.json", saveWrapper)
        ConfigurateFileUtils.update(jsonFile, overwriteExisting = true)
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