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
import yv.tils.configv2.files.ConfigFile
import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.configv2.files.JsonFileUtils
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
        val oldYaml = ConfigurateFileUtils.load(oldConfigPath, ConfigFormat.YAML, overwriteParentDir = true)

        // Transform structure to new format
        val newStructure = transformConfigFile(oldYaml)

        // Save new config
        val newYamlFile = ConfigurateFileUtils.create(newConfigPath, newStructure, ConfigFormat.YAML)
        ConfigurateFileUtils.save(newYamlFile)

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
        val oldYaml = ConfigurateFileUtils.load(oldSavePath, ConfigFormat.YAML, overwriteParentDir = true)

        // Transform to new structure
        val saveEntries = transformSaveFile(oldYaml)
        val saveWrapper = mapOf("saves" to saveEntries)

        // Save as JSON
        val jsonFile = JsonFileUtils.makeJsonFile(newSavePath, saveWrapper)
        ConfigurateFileUtils.save(jsonFile)

        Logger.info("MultiMine save migrated successfully (${saveEntries.size} entries)")
        return true
    }

    private fun transformConfigFile(
        oldYAML: ConfigFile,
    ): Map<String, Any> {
        val newConfig = mutableMapOf<String, Any>()
        val yaml = oldYAML.node

        newConfig["documentation"] = "https://docs.yvtils.net/user/modules/multimine/configs"
        newConfig["defaultState"] = yaml.node("defaultState").get(Boolean::class.javaObjectType) ?: true
        newConfig["animationTime"] = yaml.node("animationTime").get(Int::class.javaObjectType) ?: 3
        newConfig["cooldownTime"] = yaml.node("cooldownTime").get(Int::class.javaObjectType) ?: 3
        newConfig["breakLimit"] = yaml.node("breakLimit").get(Int::class.javaObjectType) ?: 250

        newConfig["leaveDecay"] = true
        newConfig["matchBlockTypeOnly"] = true

        val oldBlocks = yaml.node("blocks").getList(String::class.java) ?: emptyList()
        val blocks = if (oldBlocks.isEmpty()) {
            createTemplateBlocks()
        } else {
            oldBlocks
        }
        newConfig["blocks"] = blocks

        return newConfig
    }

    private fun transformSaveFile(
        oldYAML: ConfigFile,
    ): List<Map<String, Any>> {
        val entries = mutableListOf<Map<String, Any>>()
        val yaml = oldYAML.node

        for ((key, child) in yaml.childrenMap()) {
            val keyStr = key.toString()
            if (keyStr == "documentation") continue

            val value = child.get(Boolean::class.javaObjectType) ?: false

            val entry = mapOf(
                "uuid" to keyStr,
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
