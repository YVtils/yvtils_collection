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

package yv.tils.core.loader

import yv.tils.utils.logger.DEBUG_LEVEL
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Module
import java.nio.file.Path

/**
 * SPIKE: reflectively discovers and instantiates whichever dynamically-fetched
 * modules actually made it onto the classpath (via [DynamicModuleLoader]).
 *
 * This re-reads the same `modules.txt` config file that the loader used - it
 * CANNOT rely on any state set by [DynamicModuleLoader], since that class runs
 * in a separate classloader per Paper's `PluginLoader` docs. By the time this
 * runs (from `YVtils.onLoad()`), the classpath is already final, so we just
 * need to check whether each expected class is actually present.
 */
object DynamicModuleDriver {
    data class DiscoveryResult(
        val loaded: List<Module.YVtilsModule>,
        val succeeded: List<String>,
        val failed: List<Pair<String, String>>,
    )

    fun discover(dataDirectory: Path): DiscoveryResult {
        val enabledModules = ModuleConfig.readEnabledModules(dataDirectory)

        val loaded = mutableListOf<Module.YVtilsModule>()
        val succeeded = mutableListOf<String>()
        val failed = mutableListOf<Pair<String, String>>()

        for (moduleName in enabledModules) {
            val artifact = DynamicModuleRegistry.KNOWN_MODULES[moduleName]

            if (artifact == null) {
                failed.add(moduleName to "no known artifact/entry-point mapping")
                continue
            }

            try {
                val clazz = Class.forName(artifact.entryPointClass)
                val instance = clazz.getDeclaredConstructor().newInstance()

                if (instance !is Module.YVtilsModule) {
                    failed.add(moduleName to "${artifact.entryPointClass} does not implement Module.YVtilsModule")
                    continue
                }

                loaded.add(instance)
                succeeded.add(moduleName)
                Logger.debug("[DynamicModuleDriver] Loaded module '$moduleName' (${artifact.entryPointClass})", DEBUG_LEVEL.BASIC)
            } catch (e: ClassNotFoundException) {
                failed.add(moduleName to "class not found on classpath (artifact failed to resolve?): ${e.message}")
            } catch (e: Exception) {
                failed.add(moduleName to "failed to instantiate: ${e.message}")
            }
        }

        return DiscoveryResult(loaded, succeeded, failed)
    }
}
