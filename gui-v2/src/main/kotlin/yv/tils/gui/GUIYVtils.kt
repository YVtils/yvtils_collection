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

package yv.tils.gui

import yv.tils.gui.core.InvUIBootstrap
import yv.tils.utils.modules.Module

/**
 * GUI module (v2), built on top of the InvUI library.
 *
 * Unlike the legacy `gui` module, this module does not need to register any
 * Bukkit event listeners itself: InvUI handles all inventory click/close/drag
 * events internally for windows created through it.
 *
 * NOTE: this class's lifecycle methods are only guaranteed to be called when
 * `gui-v2` is driven directly by a core's static module list (e.g.
 * `multiMine-core`). When `gui-v2` is only pulled in as a *transitive* Maven
 * dependency of another dynamically-fetched module without being separately
 * enabled in that core's `modules.yml` (e.g. `test-core`), this class is never
 * instantiated at all - see [InvUIBootstrap] for why the actual InvUI
 * initialization must not depend on this class's lifecycle running.
 */
class GUIYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "gui-v2",
            "1.0.0",
            "GUI module for YVtils, powered by InvUI",
            "YVtils",
            "https://docs.yvtils.net/gui/"
        )
    }

    override fun onLoad() {}

    override fun enablePlugin() {
        InvUIBootstrap.ensure()
    }

    override fun enablePlugin() {
        Module.addModule(MODULE)
    }

    override fun onLateEnablePlugin() {}

    override fun disablePlugin() {}
}
