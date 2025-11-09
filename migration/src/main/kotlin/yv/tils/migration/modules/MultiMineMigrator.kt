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

package yv.tils.migration.modules

import yv.tils.migration.base.BaseMigrator
import yv.tils.utils.logger.Logger

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
