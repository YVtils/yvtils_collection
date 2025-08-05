package yv.tils.migration.modules

import yv.tils.utils.logger.Logger
import yv.tils.migration.base.BaseMigrator

/**
 * SMP module configuration migration
 *
 * Placeholder migrator for SMP module configs. Currently performs no migration as SMP module
 * structure is stable.
 */
class SmpMigrator: BaseMigrator() {

    override val moduleName = "smp"

    override fun performMigration(): Boolean {
        // Currently no migration needed for SMP module
        Logger.info("SMP module migration not implemented - no migration needed.")
        return false
    }
}
