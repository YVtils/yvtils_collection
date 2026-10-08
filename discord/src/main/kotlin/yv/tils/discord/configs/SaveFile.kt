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
import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.configv2.files.ConfigFormat
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import yv.tils.utils.logger.Logger

class SaveFile {
    private val filePath = "/discord/save.json"

    companion object {
        @Volatile
        private var writable = false
        private val json = Json { ignoreUnknownKeys = true }

        // Previous Configurate versions could write saves:null for an empty list.
        @Serializable
        private data class SavedAccounts(val saves: List<WhitelistEntry>? = emptyList())
    }

    fun loadConfig() {
        writable = false
        val file = ConfigurateFileUtils.create(filePath, emptyMap(), ConfigFormat.JSON).file
        try {
            val loaded =
                if (file.exists()) json.decodeFromString<SavedAccounts>(file.readText()).saves.orEmpty() else emptyList()
            check(loaded.map { it.discordUserID }
                .distinct().size == loaded.size) { "Duplicate Discord IDs in saved accounts" }
            WhitelistLogic.whitelistMap = loaded.associateBy { it.discordUserID }
            writable = true
        } catch (error: Exception) {
            Logger.error("Account save could not be loaded; account mutations disabled to preserve existing data: ${error.message}")
        }
    }

    /** Persist atomically before publishing an immutable snapshot; preserves the saves schema. */
    fun commit(entries: List<WhitelistEntry>) {
        check(writable) { "Account save is unavailable" }
        val temporary = "$filePath.tmp.json"
        ObjectMapperFileUtils.saveList(temporary, entries)
        val source = ConfigurateFileUtils.create(temporary, emptyMap(), ConfigFormat.JSON).file.toPath()
        val target = ConfigurateFileUtils.create(filePath, emptyMap(), ConfigFormat.JSON).file.toPath()
        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        WhitelistLogic.whitelistMap = entries.associateBy { it.discordUserID }
    }

}
