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

package yv.tils.migration.modules

import org.bukkit.Material
import org.bukkit.Tag
import yv.tils.config.files.FileUtils
import yv.tils.config.files.JSONFileUtils
import yv.tils.config.files.YMLFileUtils
import yv.tils.migration.base.BaseMigrator
import yv.tils.utils.logger.Logger
import java.io.File

/**
 * MultiMine module configuration migration
 *
 * Placeholder migrator for MultiMine module configs. Currently performs no migration as MultiMine
 * module structure is stable.
 */
class MultiMineMigrator: BaseMigrator() {

    override val moduleName = "multimine"

    private val oldConfigPath = "plugins/YVtils-MM/multiMine/config.yml"
    private val newConfigPath = "/multiMine/config.yml"
    private val oldSavePath = "plugins/YVtils-MM/multiMine/save.yml"
    private val newSavePath = "/multiMine/save.json"

    override fun performMigration(): Boolean {
        // Migrate config file
        val configMigrated = migrateConfigFile()

        // Migrate save file
        val saveMigrated = migrateSaveFile()

        // Log results
        Logger.info("MultiMine migration summary:")
        Logger.info("  Config migrated: $configMigrated")
        Logger.info("  Save migrated: $saveMigrated")

        return configMigrated || saveMigrated
    }

    private fun migrateConfigFile(): Boolean {
        if (!shouldMigrate(oldConfigPath, newConfigPath)) {
            return false
        }

        Logger.info("Migrating MultiMine config: $oldConfigPath → $newConfigPath")

        // Create backup
        val oldFile = File(oldConfigPath)
        createBackup(oldFile, "backup_config.yml")

        // Load old config using FileUtils
        val oldYaml = YMLFileUtils.loadYAMLFile(oldConfigPath, true)

        // Transform structure to new format
        val newStructure = transformConfigFile(oldYaml)

        // Save new config
        val newYamlFile = YMLFileUtils.makeYAMLFile(newConfigPath, newStructure)
        FileUtils.saveFile(newConfigPath, newYamlFile)

        Logger.info("MultiMine config migrated successfully")
        return true
    }

    private fun migrateSaveFile(): Boolean {
        if (!shouldMigrate(oldSavePath, newSavePath)) {
            return false
        }

        Logger.info("Migrating MultiMine save: $oldSavePath → $newSavePath")

        // Create backup
        val oldFile = File(oldSavePath)
        createBackup(oldFile, "backup_save.yml")

        // Load old save
        val oldYaml = YMLFileUtils.loadYAMLFile(oldSavePath, true)

        // Transform to new structure
        val saveEntries = transformSaveFile(oldYaml)
        val saveWrapper = mapOf("saves" to saveEntries)

        // Save as JSON
        val jsonFile = JSONFileUtils.makeJSONFile(newSavePath, saveWrapper)
        FileUtils.saveFile(newSavePath, jsonFile)

        Logger.info("MultiMine save migrated successfully (${saveEntries.size} entries)")
        return true
    }

    private fun transformConfigFile(
        oldYAML: YMLFileUtils.Companion.YAMLFile,
    ): Map<String, Any> {
        val newConfig = mutableMapOf<String, Any>()
        val yaml = oldYAML.content

        newConfig["documentation"] = "https://docs.yvtils.net/user/modules/multimine/configs"
        newConfig["defaultState"] = yaml.getBoolean("defaultState", true)
        newConfig["animationTime"] = yaml.getInt("animationTime", 3)
        newConfig["cooldownTime"] = yaml.getInt("cooldownTime", 3)
        newConfig["breakLimit"] = yaml.getInt("breakLimit", 250)

        newConfig["leaveDecay"] = true
        newConfig["matchBlockTypeOnly"] = true

        val oldBlocks = yaml.getStringList("blocks")
        val blocks = if (oldBlocks.isEmpty()) {
            createTemplateBlocks()
        } else {
            oldBlocks
        }
        newConfig["blocks"] = blocks

        return newConfig
    }

    private fun transformSaveFile(
        oldYAML: YMLFileUtils.Companion.YAMLFile,
    ): List<Map<String, Any>> {
        val entries = mutableListOf<Map<String, Any>>()
        val yaml = oldYAML.content

        for (key in yaml.getKeys(false)) {
            if (key == "documentation") continue

            val value = yaml.getBoolean(key)

            val entry = mapOf(
                "uuid" to key,
                "toggled" to value,
            )
            entries.add(entry)
        }

        return entries
    }

    private fun createTemplateBlocks(): List<String> {
        val blocks = Tag.LOGS.values.toMutableList()

        val ores = listOf(
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE,
            Material.COPPER_ORE,
            Material.DEEPSLATE_COAL_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.DEEPSLATE_EMERALD_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,
            Material.DEEPSLATE_COPPER_ORE,
            Material.NETHER_QUARTZ_ORE,
            Material.NETHER_GOLD_ORE,
            Material.ANCIENT_DEBRIS,
            Material.GLOWSTONE,
        )
        blocks.addAll(ores)

        return blocks.map { it.name }
    }
}
