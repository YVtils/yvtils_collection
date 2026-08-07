/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.discord.configs

import yv.tils.configv2.files.ObjectMapperFileUtils

class StatsSyncSaveFile {
    companion object {
        var saves = mutableMapOf<String, StatsSyncSave>()
    }

    private val filePath = "/discord/statsSync.json"

    fun loadConfig() {
        val loaded = ObjectMapperFileUtils.loadList<StatsSyncSave>(filePath)

        if (loaded.isEmpty()) {
            registerStrings()
            return
        }

        saves.clear()
        loaded.forEach { saves[it.guildID] = it }
    }

    fun registerStrings(saveList: MutableList<StatsSyncSave> = mutableListOf()) {
        ObjectMapperFileUtils.saveList(filePath, saveList)
    }

    data class StatsSyncSave(
        val guildID: String,
        var status: String,
        var version: String,
        var playerCount: String,
        var lastRefreshed: String,
    )
}
