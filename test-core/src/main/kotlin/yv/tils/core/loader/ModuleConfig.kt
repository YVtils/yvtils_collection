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

import java.nio.file.Files
import java.nio.file.Path

/**
 * Minimal, dependency-free reader/writer for the dynamic-module config file.
 *
 * This intentionally does NOT use the `config` module's YAML/JSON helpers: at
 * the point [DynamicModuleLoader] runs, the plugin's own bundled dependencies
 * are not guaranteed to be available yet (Paper loads `PluginLoader` classes
 * through a separate, isolated classloader - see the class-level docs on
 * `io.papermc.paper.plugin.loader.PluginLoader`). Keeping this stdlib-only
 * means the exact same code can be safely called both from the loader (to
 * decide what to fetch) and later from `onLoad()`/`onEnable()` (to decide
 * what to reflectively instantiate) without relying on any shared mutable
 * state between the two (which would NOT persist, per Paper's docs).
 *
 * File format is a minimal YAML-like `moduleName: true`/`false` per line
 * (blank lines and lines starting with `#` are ignored) - NOT full YAML, just
 * enough hand-rolled parsing to look consistent with the rest of the
 * project's YAML-style configs while staying dependency-free:
 *
 * ```yaml
 * # YVtils dynamic module configuration.
 * # Set each module to true or false, then restart the server.
 * discord: false
 * sit: true
 * ```
 *
 * On first boot, a default file is generated listing every module known to
 * [DynamicModuleRegistry] (so admins can see every available option, not just
 * guess module names), with the given [defaultEnabledModules] set to `true`.
 */
object ModuleConfig {
    private const val FILE_NAME = "modules.yml"

    /**
     * Reads the set of enabled module names from `<dataDirectory>/modules.yml`.
     * If the file does not exist yet (e.g. first ever boot), it is generated
     * listing every module in [knownModules] (defaulting to everything
     * [DynamicModuleRegistry] knows about), with [defaultEnabledModules] set
     * to `true` and everything else `false`, so the admin can see every
     * available module and simply flip the ones they want on.
     */
    fun readEnabledModules(
        dataDirectory: Path,
        knownModules: Collection<String> = DynamicModuleRegistry.KNOWN_MODULES.keys,
        defaultEnabledModules: Set<String> = setOf("sit"),
    ): List<String> {
        val file = dataDirectory.resolve(FILE_NAME)

        if (!Files.exists(file)) {
            Files.createDirectories(dataDirectory)
            Files.writeString(file, buildDefaultFile(knownModules, defaultEnabledModules))
        }

        return parse(Files.readAllLines(file))
            .filter { (_, enabled) -> enabled }
            .map { (name, _) -> name }
    }

    private fun buildDefaultFile(knownModules: Collection<String>, defaultEnabledModules: Set<String>): String {
        val header = """
            |# YVtils dynamic module configuration.
            |# Set each module to true or false, then restart the server for changes to take effect.
            |
            """.trimMargin()

        val entries = knownModules.joinToString("\n") { name ->
            "$name: ${defaultEnabledModules.contains(name)}"
        }

        return "$header$entries\n"
    }

    /**
     * Parses `moduleName: true`/`false` lines. Unknown/malformed values are
     * treated as `false` rather than failing the whole file.
     */
    private fun parse(lines: List<String>): List<Pair<String, Boolean>> =
        lines
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { line ->
                val separatorIndex = line.indexOf(':')
                if (separatorIndex == -1) return@mapNotNull null

                val name = line.substring(0, separatorIndex).trim()
                val value = line.substring(separatorIndex + 1).trim()

                if (name.isEmpty()) null else name to value.equals("true", ignoreCase = true)
            }
}
