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

package yv.tils.migration.config

import kotlinx.serialization.Serializable
import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.configv2.files.JsonFileUtils
import yv.tils.utils.logger.Logger

class SaveFile {
    companion object {
        val saveFile = mutableListOf<MigrationEntry>()

        fun wasMigrated(configFileName: String): Boolean {
            return saveFile.any { it.configFileName == configFileName && it.migrated }
        }
    }

    private val filePath = "/migration/save.json"

    fun loadConfig() {
        val file = JsonFileUtils.loadJsonFile(filePath)
        val saveListNode = file.node.node("saves")

        if (saveListNode.virtual() || !saveListNode.isList()) {
            Logger.debug("No saves found in the save file.")
            return
        }

        for (saveNode in saveListNode.childrenList()) {
            Logger.debug("Loading save: ${saveNode.raw()}")

            val configFileName = saveNode.node("configFileName").string ?: continue
            val migrated = saveNode.node("migrated").get(Boolean::class.javaObjectType) ?: false

            val migrationEntry = MigrationEntry(configFileName, migrated)
            saveFile.add(migrationEntry)
            Logger.debug("Migration entry loaded: $migrationEntry")
        }
    }

    fun registerStrings(saveList: MutableList<MigrationEntry> = mutableListOf()) {
        val saveWrapper = mapOf(
            "saves" to saveList.map { mapOf("configFileName" to it.configFileName, "migrated" to it.migrated) }
        )
        val jsonFile = JsonFileUtils.makeJsonFile(filePath, saveWrapper)
        ConfigurateFileUtils.update(jsonFile, overwriteExisting = false)
    }

    private fun upgradeStrings(saveList: MutableList<MigrationEntry> = mutableListOf()) {
        val saveWrapper = mapOf(
            "saves" to saveList.map { mapOf("configFileName" to it.configFileName, "migrated" to it.migrated) }
        )
        val jsonFile = JsonFileUtils.makeJsonFile(filePath, saveWrapper)
        ConfigurateFileUtils.update(jsonFile, overwriteExisting = true)
    }
}

@Serializable
data class MigrationEntry(
    val configFileName: String,
    val migrated: Boolean,
)
