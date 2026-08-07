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
 * GUI module, built on top of the InvUI library.
 *
 * Unlike the legacy `gui` module, this module does not need to register any
 * Bukkit event listeners itself: InvUI handles all inventory click/close/drag
 * events internally for windows created through it.
 *
 * This source is shared by every `gui-<version>` Gradle module (`gui-26.1`,
 * `gui-26.2`, ...) - see `gui-26.1/build.gradle.kts` for why.
 *
 * NOTE: `core` resolves a matching `gui-<version>` artifact onto the runtime
 * classpath unconditionally (see `DynamicModuleRegistry.GUI_ARTIFACTS`,
 * `DynamicModuleLoader.addGuiModule`), but this class's own lifecycle methods
 * are NOT driven by that - `gui` isn't a `DynamicModuleRegistry.KNOWN_MODULES`
 * entry, so nothing ever instantiates `GUIYVtils` in that path. That's fine:
 * see [InvUIBootstrap] for why the real InvUI setup deliberately does not
 * depend on this class's lifecycle running at all (every GUI-opening call
 * site, e.g. `ConfigGui.open()`, calls [InvUIBootstrap.ensure] itself as a
 * lazy, idempotent fallback).
 */
class GUIYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "gui",
            "1.0.0",
            "GUI module for YVtils, powered by InvUI",
            "YVtils",
            "https://docs.yvtils.net/gui/"
        )
    }

    override fun onLoad() {}

    override fun enablePlugin() {
        InvUIBootstrap.ensure()

        Module.addModule(MODULE)
    }

    override fun onLateEnablePlugin() {}

    override fun disablePlugin() {}
}
