package yv.tils.migration.base

import yv.tils.migration.config.SaveFile
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.io.File

/**
 * Base class for module-specific migration logic
 *
 * Provides common functionality for config migrations including:
 * - File existence checks
 * - Backup creation
 * - Migration status tracking
 * - Error handling
 */
abstract class BaseMigrator {

    /** Module name for logging and tracking */
    abstract val moduleName: String

    /**
     * Executes the migration for this module
     * @return true if migration was performed, false if skipped
     */
    fun migrate(): Boolean {
        Logger.info("Starting $moduleName migration...")

        try {
            // Check if already migrated
            if (SaveFile.wasMigrated("${moduleName}_config")) {
                Logger.warn("$moduleName config already migrated, skipping.")
                return false
            }

            // Perform module-specific migration
            val result = performMigration()

            if (result) {
                Logger.info("$moduleName migration completed successfully.")
            } else {
                Logger.info("$moduleName migration skipped (no old files found or target exists).")
            }

            return result
        } catch (e: Exception) {
            Logger.error("$moduleName migration failed: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    /**
     * Module-specific migration logic - implemented by subclasses
     * @return true if migration was performed, false if skipped
     */
    abstract fun performMigration(): Boolean

    /** Creates a backup of the given file */
    protected fun createBackup(sourceFile: File, backupName: String): Boolean {
        return try {
            val backupFile = File(sourceFile.parent, backupName)
            sourceFile.copyTo(backupFile, overwrite = true)
            Logger.info("Backup created: ${backupFile.absolutePath}")
            true
        } catch (e: Exception) {
            Logger.error("Failed to create backup: ${e.message}")
            false
        }
    }

    /** Checks if old file exists and new file doesn't exist */
    protected fun shouldMigrate(oldPath: String, newPath: String): Boolean {
        val oldFile = File(oldPath)
        if (! oldFile.exists()) {
            return false
        }

        val newFile = File(Data.pluginFolder, newPath)
        return ! newFile.exists()
    }
}
