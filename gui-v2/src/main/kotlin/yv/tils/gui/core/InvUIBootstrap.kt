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

package yv.tils.gui.core

import xyz.xenondevs.invui.InvUI
import yv.tils.gui.language.RegisterStrings
import yv.tils.utils.modules.Core

/**
 * One-time setup required before any `gui-v2` window is opened: registering
 * InvUI's plugin instance, and registering this module's language strings.
 *
 * InvUI's `AbstractWindow` static initializer eagerly calls
 * `InvUI.getInstance().getPlugin()` and throws `IllegalStateException` unless a
 * plugin was registered. InvUI's own auto-detection (via the classloader that
 * loaded InvUI's classes) only works when InvUI is loaded through Paper's
 * normal `ConfiguredPluginClassLoader` - it does NOT work when `gui-v2`/InvUI
 * are loaded as a *library* by `MavenLibraryResolver` (a separate classloader),
 * which is exactly what happens for a dynamically-fetched core.
 *
 * This deliberately does NOT rely on [yv.tils.gui.GUIYVtils]'s module lifecycle
 * (`onLoad`/`enablePlugin`) ever being invoked: when `gui-v2` is pulled in only
 * as a *transitive* Maven dependency of another dynamically-fetched module
 * (e.g. `multiMine` depending on `gui-v2`) without being separately listed as
 * `true` in that core's own `modules.yml`, `GUIYVtils` itself is never "driven"
 * by `DynamicModuleDriver` - only modules explicitly enabled there get their
 * lifecycle methods called. `gui-v2` is a library other modules depend on, not
 * an independently togglable feature, so its setup must not depend on being
 * separately enabled. Call [ensure] from every gui-v2 entry point that creates
 * a `Window` instead (e.g. at the top of `ConfigGui.open()`).
 */
object InvUIBootstrap {
    @Volatile
    private var initialized = false

    fun ensure() {
        if (initialized) return
        synchronized(this) {
            if (initialized) return

            try {
                InvUI.getInstance().setPlugin(Core.instance)
            } catch (_: IllegalStateException) {
                // Already set (e.g. GUIYVtils.onLoad() also ran, or a plugin
                // reload without a full JVM restart) - not an error, no-op.
            }

            RegisterStrings.registerStrings()

            initialized = true
        }
    }
}
