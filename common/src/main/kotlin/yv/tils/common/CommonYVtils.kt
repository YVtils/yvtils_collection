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

package yv.tils.common

import yv.tils.common.language.RegisterStrings
import yv.tils.utils.modules.Module

class CommonYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "common",
            "26.08.01",
            "Common module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/common/"
        )
    }

    override fun onLoad() {

    }

    override fun enablePlugin() {
        RegisterStrings().registerStrings()

        Module.addModule(MODULE)
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {
        Module.removeModule(MODULE)
    }
}
