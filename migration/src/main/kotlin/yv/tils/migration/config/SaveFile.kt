/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.migration.config

import yv.tils.configv2.files.ObjectMapperFileUtils

class SaveFile {
    companion object {
        val saveFile = mutableListOf<MigrationEntry>()

        fun wasMigrated(configFileName: String): Boolean {
            return saveFile.any { it.configFileName == configFileName && it.migrated }
        }
    }

    private val filePath = "/migration/save.json"

    fun loadConfig() {
        val loaded = ObjectMapperFileUtils.loadList<MigrationEntry>(filePath)

        if (loaded.isEmpty()) {
            registerStrings()
            return
        }

        saveFile.clear()
        saveFile.addAll(loaded)
    }

    fun registerStrings(saveList: MutableList<MigrationEntry> = mutableListOf()) {
        ObjectMapperFileUtils.saveList(filePath, saveList)
    }
}

data class MigrationEntry(
    val configFileName: String,
    val migrated: Boolean,
)
