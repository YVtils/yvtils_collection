/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.moderation.configs.saveFile

import yv.tils.configv2.files.ObjectMapperFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.player.PlayerUtils
import java.util.*

class MuteSaveFile {
    companion object {
        val saves = mutableMapOf<UUID, MuteSave>()
    }

    private val filePath = "/moderation/mutedPlayers.json"

    fun loadConfig() {
        val loaded = ObjectMapperFileUtils.loadList<MuteSave>(filePath)

        if (loaded.isEmpty()) {
            registerStrings()
            return
        }

        saves.clear()
        loaded.forEach { saves[UUID.fromString(it.uuid)] = it }
    }

    fun registerStrings(saveList: MutableList<MuteSave> = mutableListOf()) {
        ObjectMapperFileUtils.saveList(filePath, saveList)
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
