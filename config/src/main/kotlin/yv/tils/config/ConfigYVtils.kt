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

package yv.tils.config

import yv.tils.config.language.Language
import yv.tils.utils.modules.Module

class ConfigYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "config",
            "26.08.01",
            "YVtils Config Module",
            "YVtils",
            "https://docs.yvtils.net/config/"
        )
    }

    override fun onLoad() {}

    override fun enablePlugin() {
        Module.addModule(MODULE)
    }

    override fun onLateEnablePlugin() {
        // Runs after every module's onLoad() (where they register their
        // language strings via RegisterStrings) has completed, so this is
        // the first safe point to (re)build and load the language files.
        Language().loadLanguageFiles()
    }

    override fun disablePlugin() {
        Module.removeModule(MODULE)
    }
}
