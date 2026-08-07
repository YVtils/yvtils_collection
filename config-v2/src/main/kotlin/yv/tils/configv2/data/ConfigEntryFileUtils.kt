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

package yv.tils.configv2.data

import org.spongepowered.configurate.ConfigurationNode
import yv.tils.configv2.files.ConfigFile
import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.utils.logger.Logger

/**
 * Bridges [ConfigEntry] (the in-game GUI-editable config schema, ported
 * as-is from the original `config` module) with [ConfigurateFileUtils].
 *
 * [ConfigEntry.key] may contain dots to address a nested value (e.g.
 * `"updateCheck.enabled"`) - every existing feature module relies on this
 * via Bukkit's `ConfigurationSection` path syntax (`YamlConfiguration`
 * treats a dotted string as a path, not a literal key). Configurate's
 * [ConfigurationNode.node] instead treats each *argument* as one path
 * segment rather than splitting a single dotted string - every function
 * here explicitly splits the key on `.` before navigating, so a
 * `"updateCheck.enabled"` entry still ends up as a nested
 * `updateCheck: { enabled: ... }` structure on disk instead of a literal
 * top-level `"updateCheck.enabled"` key.
 */
class ConfigEntryFileUtils {
    companion object {
        private fun ConfigEntry.pathSegments(): Array<Any> = key.split(".").toTypedArray()

        /**
         * Writes every entry's current value (falling back to its default)
         * into [node] at its (dot-nested) path. Entries with neither a
         * value nor a default are skipped, same as the original
         * `makeYAMLFileFromEntries`.
         */
        fun applyToNode(node: ConfigurationNode, entries: List<ConfigEntry>) {
            for (entry in entries) {
                val value = entry.value ?: entry.defaultValue ?: continue
                node.node(*entry.pathSegments()).raw(value)
            }
        }

        /**
         * Reads each entry's value back from [node] into [ConfigEntry.value].
         * Entries with no corresponding (non-virtual) node are left alone,
         * so callers can still fall back to [ConfigEntry.defaultValue].
         */
        fun loadFromNode(node: ConfigurationNode, entries: List<ConfigEntry>) {
            for (entry in entries) {
                val childNode = node.node(*entry.pathSegments())
                if (!childNode.virtual()) {
                    entry.value = childNode.raw()
                }
            }
        }

        /**
         * Builds a new, in-memory (not yet saved) [ConfigFile] of the given
         * [format] from [entries] - the Configurate-backed replacement for
         * `YMLFileUtils.makeYAMLFileFromEntries`. Call [ConfigurateFileUtils.save]
         * or [persist]/[ConfigurateFileUtils.update] to actually write it.
         */
        fun buildConfigFile(
            path: String,
            entries: List<ConfigEntry>,
            format: ConfigFormat,
            overwriteParentDir: Boolean = false,
        ): ConfigFile {
            Logger.debug("Building ${format.name} file from ${entries.size} ConfigEntry values: $path")

            val file = ConfigurateFileUtils.create(path, emptyMap(), format, overwriteParentDir)
            applyToNode(file.node, entries)
            return file
        }

        /**
         * Persists [entries] to [path], creating the file if it doesn't
         * exist yet.
         *
         * Mirrors the original module's `ConfigFile.registerStrings()`
         * pattern: called once from `onLoad()` (before the file exists) it
         * seeds the file with defaults; called again later (e.g. from a GUI
         * `saver` callback, with [overwriteExisting] left at its default
         * `true`) it fully overwrites the on-disk value for every
         * registered entry - matching the original
         * `FileUtils.updateFile(overwriteExisting = true)` used by
         * `ManageGUI`'s save flow, so in-game edits always win over
         * whatever was previously on disk.
         */
        fun persist(
            path: String,
            entries: List<ConfigEntry>,
            format: ConfigFormat,
            overwriteExisting: Boolean = true,
            overwriteParentDir: Boolean = false,
        ) {
            val configFile = buildConfigFile(path, entries, format, overwriteParentDir)
            ConfigurateFileUtils.update(configFile, overwriteExisting)
        }

        /**
         * Loads [path] (if it exists) and populates [entries] with whatever
         * values are already on disk - the Configurate-backed replacement
         * for each feature module's `loadConfig()` "read the file, copy
         * matching keys into `ConfigEntry.value`" boilerplate.
         *
         * No-ops (leaving every entry to fall back to its default) if the
         * file doesn't exist yet.
         */
        fun load(path: String, entries: List<ConfigEntry>, format: ConfigFormat, overwriteParentDir: Boolean = false) {
            val configFile = runCatching { ConfigurateFileUtils.load(path, format, overwriteParentDir) }.getOrNull()
                ?: return

            loadFromNode(configFile.node, entries)
        }
    }
}
