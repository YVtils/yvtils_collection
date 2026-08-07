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

package yv.tils.utils.modules

import org.bukkit.entity.Player
import java.util.function.Consumer

class Module {
    companion object {
        private val loadedModules = ArrayList<YVtilsModuleData>()

        fun addModule(module: YVtilsModuleData) {
            loadedModules.add(module)
        }

        fun removeModule(module: YVtilsModuleData) {
            loadedModules.remove(module)
        }

        fun getModule(moduleName: String): YVtilsModuleData? {
            return loadedModules.find { it.name == moduleName }
        }

        fun getModules(sort: Boolean = false): ArrayList<YVtilsModuleData> {
            if (sort) {
                loadedModules.sortBy { it.name }
            }

            return loadedModules
        }

        fun getModuleNames(sort: Boolean = false): List<String> {
            if (sort) {
                loadedModules.sortBy { it.name }
            }

            return loadedModules.map { it.name }
        }

        fun getModulesString(sort: Boolean = false): String {
            if (sort) {
                loadedModules.sortBy { it.name }
            }

            return loadedModules.joinToString(", ") { it.name }
        }
    }

    data class YVtilsModuleData(
        val name: String,
        val version: String,
        val description: String = "",
        val author: String = "YVtils",
        val documentation: String = "https://docs.yvtils.net",
        /**
         * Opens this module's config-editing GUI for [player], if it has one - `null` for
         * modules with nothing GUI-editable (e.g. a module whose only "config" is runtime
         * state rather than user settings).
         *
         * Deliberately a JDK [Consumer] rather than a Kotlin `(Player) -> Unit` lambda type:
         * `core` and each dynamically-loaded feature module (see `DynamicModuleDriver`) run
         * under *separate* classloaders, each potentially resolving its own copy of
         * `kotlin-stdlib` - a Kotlin `Function1` showing up in this class's public API would
         * trip the JVM's loader-constraint check the moment `core` calls a getter/method
         * whose signature mentions it (`LinkageError: loader constraint violation`), since
         * the two sides would disagree on which `Class` object `kotlin.jvm.functions.Function1`
         * even refers to. `java.util.function.Consumer` is a platform type loaded once by the
         * shared bootstrap classloader, so it can safely cross that boundary; Kotlin still
         * SAM-converts a lambda literal into one automatically at each module's own
         * `MODULE` declaration site (typically `{ player -> ManageGUI().openGUI(player) }`,
         * delegating to a `gui-v2`-backed `DataClassConfigGui.open(...)` call).
         *
         * This indirection (a per-module opener registered on [YVtilsModuleData] itself) is
         * what lets `core`'s `/yvtils config` list and open every active module's config GUI
         * without `core` ever needing a compile-time dependency on that module.
         */
        val configGuiOpener: Consumer<Player>? = null,
    )

    interface YVtilsModule {
        fun onLoad() {}
        fun enablePlugin() {}
        fun onLateEnablePlugin() {}
        fun disablePlugin() {}
    }
}