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
     * listing every *configurable* module in [knownModules] (defaulting to
     * everything [DynamicModuleRegistry] knows about, minus any
     * [DynamicModuleRegistry.ModuleArtifact.hidden] ones - see below), with
     * [defaultEnabledModules] set to `true` and everything else `false`, so
     * the admin can see every available module and simply flip the ones they
     * want on.
     *
     * Modules marked [DynamicModuleRegistry.ModuleArtifact.hidden] (e.g.
     * `migration`) are always EXCLUDED from the returned set,
     * regardless of what's in the file - even if an admin hand-edits the
     * file to add e.g. `migration: true`, that line is ignored. This is
     * deliberately "always excluded", not "always force-enabled": this
     * result feeds [DynamicModuleLoader]/[DynamicModuleDriver], which
     * actually attempt to *resolve the artifact over the network* for every
     * enabled module - forcing a hidden module "on" here would mean it's
     * unconditionally Maven-resolved on every boot even in environments
     * where that artifact was never published (e.g. a throwaway local dev
     * registry), which previously hard-crashed plugin loading entirely for
     * `migration`. Hidden modules are internal/not meant to be toggled by an
     * admin through this generic mechanism at all - not "always on".
     */
    fun readEnabledModules(
        dataDirectory: Path,
        knownModules: Collection<String> = DynamicModuleRegistry.KNOWN_MODULES.keys,
        defaultEnabledModules: Set<String> = emptySet(),
    ): List<String> {
        val file = dataDirectory.resolve(FILE_NAME)
        val configurableModules = knownModules.filterNot(::isHidden)

        if (!Files.exists(file)) {
            Files.createDirectories(dataDirectory)
            Files.writeString(file, buildDefaultFile(configurableModules, defaultEnabledModules))
        }

        return parse(Files.readAllLines(file))
            .filter { (name, enabled) -> enabled && !isHidden(name) }
            .map { (name, _) -> name }
    }

    /**
     * Writes [enabledModules] out to `<dataDirectory>/modules.yml`, listing
     * every *configurable* module in [knownModules] (defaulting to
     * everything [DynamicModuleRegistry] knows about, minus any
     * [DynamicModuleRegistry.ModuleArtifact.hidden] ones) with `true`/`false`
     * depending on whether it's contained in [enabledModules].
     *
     * Like [readEnabledModules], this always regenerates the full file from
     * [knownModules] - any entries not in [knownModules] are dropped, exactly
     * like the file that gets generated on first boot. Hidden modules are
     * intentionally never written here either - see [readEnabledModules].
     *
     * Note: since dynamic modules are only resolved/instantiated once, at
     * [DynamicModuleDriver.discover] time (called from `onLoad()`), toggling
     * a module here only takes effect after the server is restarted.
     */
    fun writeEnabledModules(
        dataDirectory: Path,
        enabledModules: Set<String>,
        knownModules: Collection<String> = DynamicModuleRegistry.KNOWN_MODULES.keys,
    ) {
        Files.createDirectories(dataDirectory)

        Files.writeString(
            dataDirectory.resolve(FILE_NAME),
            buildDefaultFile(knownModules.filterNot(::isHidden), enabledModules)
        )
    }

    /**
     * Convenience wrapper around [readEnabledModules]/[writeEnabledModules]
     * that flips a single module's enabled state, leaving every other
     * module's state untouched.
     *
     * A no-op for modules marked [DynamicModuleRegistry.ModuleArtifact.hidden]
     * - they can't be toggled through this API at all (this mirrors
     * [YVtilsModulesGui][yv.tils.core.commands.gui.YVtilsModulesGui] already
     * excluding them from the toggle GUI entirely, but guards against any
     * other/future caller too).
     */
    fun setModuleEnabled(dataDirectory: Path, moduleName: String, enabled: Boolean) {
        if (isHidden(moduleName)) return

        val current = readEnabledModules(dataDirectory).toMutableSet()

        if (enabled) {
            current.add(moduleName)
        } else {
            current.remove(moduleName)
        }

        writeEnabledModules(dataDirectory, current)
    }

    /**
     * Whether [moduleName] is marked
     * [DynamicModuleRegistry.ModuleArtifact.hidden] in
     * [DynamicModuleRegistry.KNOWN_MODULES] - i.e. excluded from
     * `modules.yml`/the in-game GUI entirely (see [readEnabledModules]).
     * Unknown module names are never considered hidden.
     */
    private fun isHidden(moduleName: String): Boolean =
        DynamicModuleRegistry.KNOWN_MODULES[moduleName]?.hidden == true

    private fun buildDefaultFile(knownModules: Collection<String>, defaultEnabledModules: Set<String>): String {
        val header = """
            |# YVtils dynamic module configuration.
            |# Set each module to true or false, then restart the server for changes to take effect.
            |# Note: some internal modules (e.g. migration) are intentionally not
            |# listed here and cannot be enabled through this file. The `gui` module isn't
            |# listed either, but for a different reason - it isn't a togglable feature at
            |# all; `core` always resolves exactly one `gui-<version>` build matching the
            |# server's Minecraft version (see DynamicModuleRegistry.GUI_ARTIFACTS).
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
