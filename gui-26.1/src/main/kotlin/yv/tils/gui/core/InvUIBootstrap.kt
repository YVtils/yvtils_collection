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
 * One-time setup required before any `gui`/InvUI window is opened:
 * registering InvUI's plugin instance, and registering this module's language
 * strings.
 *
 * InvUI's `AbstractWindow` static initializer eagerly calls
 * `InvUI.getInstance().getPlugin()` and throws `IllegalStateException` unless a
 * plugin was registered. InvUI's own auto-detection (via the classloader that
 * loaded InvUI's classes) only works when InvUI is loaded through Paper's
 * normal `ConfiguredPluginClassLoader` - it does NOT work when `gui`/InvUI
 * are loaded as a *library* by `MavenLibraryResolver` (a separate classloader),
 * which is exactly what happens for a dynamically-fetched core.
 *
 * IMPORTANT - once a class's static initializer throws, the JVM marks that
 * class as permanently erroneous for the rest of that classloader's lifetime:
 * every subsequent use of it (from ANY code, even code that calls [ensure]
 * correctly) immediately throws `NoClassDefFoundError`, without even
 * re-attempting initialization. In practice this means if even ONE consumer of
 * InvUI anywhere in the server touches a `Window`/`AnvilWindow`/etc. before
 * [ensure] has run, InvUI is broken for the entire remaining server session -
 * regardless of how many *other* call sites correctly call [ensure] first.
 *
 * Because of that, **do not** call [ensure] individually at every place that
 * builds a `Window` - a single missed call site anywhere breaks everything
 * else, permanently, for that run. Instead, call it exactly **once**, in your
 * module's own `enablePlugin()` (NOT `onLoad()` - see below for why):
 *
 * ```kotlin
 * override fun enablePlugin() {
 *     InvUIBootstrap.ensure() // <- add this once, alongside the below
 *     Module.addModule(MODULE)
 *     // ... registerCommands(), etc.
 * }
 * ```
 *
 * This must be `enablePlugin()`, not `onLoad()`: [ensure] calls
 * `InvUI.getInstance().setPlugin(...)`, which internally registers InvUI
 * itself as a Bukkit `Listener` on that plugin
 * (`Bukkit.getPluginManager().registerEvents(...)`). Bukkit throws
 * `IllegalPluginAccessException: Plugin attempted to register ... while not
 * enabled` if you try to register events before the plugin has been marked
 * enabled - which happens right before `onEnable()`/`enablePlugin()`, but
 * *after* `onLoad()`. So calling [ensure] from `onLoad()` (as an earlier
 * version of this fix did) fails outright.
 *
 * This is safe and sufficient because a module's `enablePlugin()` is
 * guaranteed to run before any player could possibly issue a command that
 * reaches your GUI code (the server doesn't finish starting - and therefore
 * can't accept commands - until every plugin's `onEnable()` has completed) -
 * see `MultiMineYVtils`/`ModerationYVtils`/`StatusYVtils` for the existing
 * examples. [ensure] is idempotent (safe to call multiple times, from
 * multiple modules), so there's no harm in it also being called elsewhere
 * (e.g. `ConfigGui.open()` also calls it, as a self-sufficiency fallback for
 * anyone integrating with `gui` who might otherwise forget the
 * `enablePlugin()` step - this is safe there specifically because
 * `ConfigGui.open()` only ever runs lazily, in response to a player command,
 * which can only happen after every plugin has already been enabled) - but
 * the `enablePlugin()` call is what actually guarantees correct ordering for
 * code that might run earlier than that, not those extra call sites.
 *
 * This deliberately does NOT rely on [yv.tils.gui.GUIYVtils]'s own module
 * lifecycle (`onLoad`/`enablePlugin`) ever being invoked: when `gui` is
 * pulled in only as a *transitive* Maven dependency of another dynamically-
 * fetched module (e.g. `multiMine` depending on `gui`) without being
 * separately listed as `true` in that core's own `modules.yml`, `GUIYVtils`
 * itself is never "driven" by `DynamicModuleDriver` - only modules explicitly
 * enabled there get their lifecycle methods called. `gui` is a library
 * other modules depend on, not an independently togglable feature, so its
 * setup must not depend on being separately enabled.
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
                // Already set (e.g. GUIYVtils.enablePlugin() also ran, or a
                // plugin reload without a full JVM restart) - not an error.
            }

            RegisterStrings.registerStrings()

            initialized = true
        }
    }
}
