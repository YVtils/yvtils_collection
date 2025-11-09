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

package yv.tils.config

import yv.tils.utils.data.Data

class ConfigYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "config",
            "1.0.0",
            "YVtils Config Module",
            "YVtils",
            "https://docs.yvtils.net/config/"
        )
    }

    override fun onLoad() {}

    override fun enablePlugin() {
        Data.addModule(MODULE)
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {

    }
}
