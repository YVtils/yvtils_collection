package yv.tils.migration.modules

import logger.Logger
import yv.tils.migration.base.BaseMigrator

/**
 * MultiMine module configuration migration
 *
 * Placeholder migrator for MultiMine module configs. Currently performs no migration as MultiMine
 * module structure is stable.
 */
class MultiMineMigrator: BaseMigrator() {

    override val moduleName = "multimine"

    override fun performMigration(): Boolean {
        // Currently no migration needed for MultiMine module
        Logger.info("MultiMine module migration not implemented - no migration needed.")
        return false
    }
}
