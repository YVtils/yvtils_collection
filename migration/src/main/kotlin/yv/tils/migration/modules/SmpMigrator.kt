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
