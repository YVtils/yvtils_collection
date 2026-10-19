/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.moderation.configs.saveFile

import yv.tils.config.files.ObjectMapperFileUtils
import yv.tils.utils.player.PlayerUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MuteSaveFile {
    companion object {
        val saves = ConcurrentHashMap<UUID, MuteSave>()
        private val lock = Any()
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
        synchronized(lock) {
            val next = saves.toMutableMap()
            next[uuid] = MuteSave(uuid.toString(), reason, muted, expires, modAction)
            registerStrings(next.values.toMutableList())
            saves[uuid] = next.getValue(uuid)
        }
    }

    fun getMuteInfo(uuid: UUID): MuteSave? {
        return saves[uuid]
    }

    fun unmutePlayer(uuid: UUID) {
        synchronized(lock) {
            val next = saves.toMutableMap().apply { remove(uuid) }
            registerStrings(next.values.toMutableList())
            saves.remove(uuid)
        }
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
