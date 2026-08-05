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

package yv.tils.utils

import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.modules.Module

class UtilsYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "utils",
            "26.08.01",
            "Utils module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/utils/"
        )
    }

    override fun onLoad() {}

    override fun enablePlugin() {
        Module.addModule(MODULE)
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {
        CoroutineHandler.cancelAllTasks()
        Module.removeModule(MODULE)
    }
}
