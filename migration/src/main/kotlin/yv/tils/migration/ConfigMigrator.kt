/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.migration

import yv.tils.migration.modules.DiscordMigrator
import yv.tils.utils.logger.Logger

/**
 * ConfigMigrator - Universal Config Migration Tool
 *
 * Orchestrates migration for all supported modules by delegating to module-specific migrators.
 *
 * Features:
 * - Modular architecture with separate migrators for each module
 * - Comprehensive logging and error handling
 * - Silent operation when no migration is needed
 *
 * @author YVtils Migration Tool
 * @version 2.0.0
 */
class ConfigMigrator {

    /** Main migration entry point - processes all configured modules */
    fun migrateAllConfigs() {
        Logger.info("Starting Config Migration Tool...")

        try {
            var anyMigrated = false

            // Process Discord module migration
            val discordMigrated = DiscordMigrator().migrate()
            anyMigrated = anyMigrated || discordMigrated

            // Add other module migrators here in the future:
            // val smpMigrated = SmpMigrator().migrate()
            // val multiMineMigrated = MultiMineMigrator().migrate()
            // anyMigrated = anyMigrated || smpMigrated || multiMineMigrated

            if (anyMigrated) {
                Logger.info("Config Migration Tool completed with migrations performed.")
            } else {
                Logger.info("Config Migration Tool completed - no migrations needed.")
            }
        } catch (e: Exception) {
            Logger.error("Config Migration Tool failed: ${e.message}")
            e.printStackTrace()
        }
    }
}
