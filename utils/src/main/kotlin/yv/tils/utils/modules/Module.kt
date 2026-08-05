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
    )

    interface YVtilsModule {
        fun onLoad() {}
        fun enablePlugin() {}
        fun onLateEnablePlugin() {}
        fun disablePlugin() {}
    }
}