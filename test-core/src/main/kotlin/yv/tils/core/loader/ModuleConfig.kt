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
    private const val SHARED_DIRECTORY_NAME = "yvtils"

    /**
     * Computes the shared `plugins/yvtils` data directory used by every core,
     * instead of each core's own product-specific data folder (e.g.
     * `plugins/TEST-YVTILS-CORE`, `plugins/YVtils-Discord`, ...).
     *
     * This is always resolved as a sibling of the given per-plugin data
     * directory (`<perPluginDataDirectory>/../yvtils`), so it works
     * regardless of the server's actual directory layout and without needing
     * `Bukkit`/server access (which isn't safely available yet at the point
     * [DynamicModuleLoader] runs).
     *
     * Sharing one directory across every installed core means:
     * - The embedded runtime bundle only needs to be extracted once, not
     *   once per installed core.
     * - `modules.yml` becomes a single, shared enablement list - a core
     *   simply ignores any entry it doesn't have in its own
     *   [DynamicModuleRegistry.KNOWN_MODULES].
     *
     * Note: since every core (currently just `test-core`) is built from this
     * same monorepo in lockstep, the embedded runtime bundle version is
     * always identical across cores today. If that ever changes (cores
     * released independently, out of sync), the first core to boot on a
     * server "wins" and provides its runtime bundle version to every other
     * core sharing this directory - keep that in mind before decoupling
     * core release versioning.
     */
    fun sharedDataDirectory(perPluginDataDirectory: Path): Path =
        (perPluginDataDirectory.parent ?: perPluginDataDirectory).resolve(SHARED_DIRECTORY_NAME)

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
