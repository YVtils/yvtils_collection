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

package yv.tils.core

import yv.tils.utils.logger.DEBUG_LEVEL
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module

class CheckRequirements {
    /**
     * Checks if all required modules are present and enabled.
     * @return `true` if all required modules are present and enabled, `false` otherwise. Also returns an error message if any module is missing or if the core is not initialized.
     */
    fun checkModules(): Pair<Boolean, String?> {
        try {
            val requirements = Core.core.dependencies
            val modules = Module.getModules()

            for (requirement in requirements) {
                val module = modules.find { it.name == requirement }
                if (module == null) {
                    Logger.debug("Required module '$requirement' is not loaded.", DEBUG_LEVEL.BASIC)
                    return Pair(false, requirement)
                }
            }

            Logger.debug("All dependencies are loaded: ${requirements.joinToString(", ")}", DEBUG_LEVEL.BASIC)

            return Pair(true, null)
        } catch (_: Exception) {
            Logger.error("YVtils core is not initialized. Please ensure the core is loaded before using YVtils features.")
            return Pair(
                false,
                "YVtils core is not initialized. Please ensure the core is loaded before using YVtils features."
            )
        }
    }
}