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
import yv.tils.discord.logic.whitelist.WhitelistEntry
import yv.tils.discord.logic.whitelist.WhitelistLogic

class SaveFile {
    private val filePath = "/discord/save.json"

    fun loadConfig() {
        val loaded = ObjectMapperFileUtils.loadList<WhitelistEntry>(filePath)

        if (loaded.isEmpty()) {
            // Either save.json doesn't exist yet (first run) or it's genuinely empty - either
            // way there's nothing to lose, so (re-)writing an empty list is safe and ensures
            // the file exists going forward.
            registerStrings()
            return
        }

        loaded.forEach {
            WhitelistLogic.whitelistMap[it.discordUserID] = it
        }
    }

    fun registerStrings(saveList: MutableList<WhitelistEntry> = mutableListOf()) {
        ObjectMapperFileUtils.saveList(filePath, saveList)
    }

    fun addSave(discordUserID: String, minecraftName: String, minecraftUUID: String) {
        val newSave = WhitelistEntry(
            discordUserID = discordUserID,
            minecraftName = minecraftName,
            minecraftUUID = minecraftUUID
        )
        WhitelistLogic.whitelistMap[discordUserID] = newSave
        registerStrings(WhitelistLogic.getAllEntries().toMutableList())
    }

    fun removeSave(discordUserID: String) {
        WhitelistLogic.whitelistMap.remove(discordUserID)
        registerStrings(WhitelistLogic.getAllEntries().toMutableList())
    }
}
