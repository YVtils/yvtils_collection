/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.gui.logic

import org.bukkit.entity.Player
import yv.tils.common.config.ConfigFile

/**
 * Opens the root `/config.yml` ([ConfigFile]) editor.
 *
 * This lives here - in `gui` - rather than in `common` or `core`, for two reasons:
 *
 * 1. `gui` already has a `compileOnly` dependency on `common` (for [DataClassConfigGui]
 *    to reflect over `common`'s data classes); the reverse (`common` depending on `gui`)
 *    would be a circular Gradle project dependency, so `common` cannot host its own
 *    `ManageGUI`-style opener the way every feature module does.
 * 2. More importantly, it must NOT live in `core`: `core`'s own main jar/classloader is a
 *    *different* classloader tier than `common`+`gui` (see `DynamicModuleLoader`'s
 *    architecture note - the "library tier" and the plugin's own jar are siblings, not
 *    parent/child, each with their own resolved `kotlin-stdlib`). `common` is added to
 *    that library tier as an embedded, locally-built `JarLibrary`; `gui-<version>` is
 *    added to that SAME library tier separately, via its own `MavenLibraryResolver`
 *    resolved against the running server's actual Minecraft version (see
 *    `DynamicModuleRegistry.GUI_ARTIFACTS`) - different mechanisms, but both land in the
 *    one shared library-tier classloader, which is what actually matters here.
 *    [DataClassConfigGui.open]'s `saver` parameter is a Kotlin `Function1`, and constructing
 *    that lambda from a class loaded by `core`'s own classloader while calling into
 *    [DataClassConfigGui] (loaded by the library tier) trips the JVM's loader-constraint
 *    check (`LinkageError: loader constraint violation` on `kotlin/jvm/functions/Function1`)
 *    the moment it's invoked - exactly the class of bug
 *    [yv.tils.utils.modules.Module.YVtilsModuleData.configGuiOpener]'s own KDoc warns about.
 *    By keeping the `Function1` construction and the call into [DataClassConfigGui] both
 *    here, in the same classloader tier as `common`+`gui` themselves, `core` only ever
 *    has to cross the boundary through the single-`Player`-argument, JDK-safe
 *    `configGuiOpener: java.util.function.Consumer<Player>` - see `core`'s `YVtils.kt`,
 *    which wraps a call to [open] the exact same way every other module's own
 *    `MODULE = Module.YVtilsModuleData(..., configGuiOpener = { player -> ... })` does.
 */
object CommonConfigGui {
    fun open(player: Player) {
        DataClassConfigGui.open(
            player,
            "YVtils Config",
            ConfigFile.state,
            saver = { updated -> ConfigFile().applyState(updated) }
        )
    }
}
