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

import yv.tils.configv2.files.ConfigFormat

/**
 * A reusable, Configurate-backed version of the `entries`/`configIndex`/
 * `get`/`getString`/`getInt`/`getBoolean`/`registerStrings`/`loadConfig`
 * boilerplate that's currently hand-duplicated in nearly every feature
 * module's `configs/ConfigFile.kt` (`multiMine`, `discord`, `essentials`,
 * `moderation`, `regions`, `server`, `stats`, `status`, `yv_smp`, `common`,
 * ...).
 *
 * Typical usage, replacing that per-module pattern:
 *
 * ```kotlin
 * object MyModuleConfig {
 *     val config = EntryBackedConfig("/my-module/config.yml", ConfigFormat.YAML)
 *
 *     fun onLoad() {
 *         config.register(
 *             ConfigEntry("enabled", EntryType.BOOLEAN, null, true, "Whether the feature is enabled"),
 *         )
 *         config.persist() // seed the file with defaults if it doesn't exist yet
 *     }
 *
 *     fun enablePlugin() {
 *         config.load() // pull in whatever's actually on disk
 *     }
 * }
 * ```
 *
 * A GUI `saver` callback (see `ConfigGui.open` in `gui-v2`) can call
 * [persist] directly after mutating entries returned by [entries] in place
 * - it always fully overwrites the on-disk value for every registered
 * entry by default, so in-game edits win over whatever was previously on
 * disk (matching the original `ManageGUI`/
 * `registerStrings(overwriteExisting = true)` flow).
 */
class EntryBackedConfig(
    private val path: String,
    private val format: ConfigFormat,
    private val overwriteParentDir: Boolean = false,
) {
    private val entryList = mutableListOf<ConfigEntry>()
    private val index = mutableMapOf<String, ConfigEntry>()

    /**
     * All currently registered entries, in registration order. Entries are
     * mutable ([ConfigEntry.value] is a `var`) - a GUI can edit them in
     * place and then call [persist].
     */
    val entries: List<ConfigEntry> get() = entryList

    /**
     * Registers new entries. Safe to call more than once - entries whose
     * key is already registered are skipped rather than duplicated.
     */
    fun register(vararg newEntries: ConfigEntry) {
        for (entry in newEntries) {
            if (index.containsKey(entry.key)) continue
            entryList.add(entry)
            index[entry.key] = entry
        }
    }

    fun getEntry(key: String): ConfigEntry? = index[key]

    fun get(key: String): Any? = index[key]?.let { it.value ?: it.defaultValue }

    fun getString(key: String): String? = get(key)?.toString()

    fun getInt(key: String): Int? = (get(key) as? Number)?.toInt()

    fun getDouble(key: String): Double? = (get(key) as? Number)?.toDouble()

    fun getBoolean(key: String): Boolean? = when (val value = get(key)) {
        is Boolean -> value
        is String -> value.toBoolean()
        else -> null
    }

    @Suppress("UNCHECKED_CAST")
    fun getList(key: String): List<Any?>? = get(key) as? List<Any?>

    fun getMap(key: String): Map<*, *>? = get(key) as? Map<*, *>

    /**
     * Reads whatever's on disk at [path] into the registered entries'
     * [ConfigEntry.value]. No-ops (leaving every entry at its default) if
     * the file doesn't exist yet.
     */
    fun load() {
        ConfigEntryFileUtils.load(path, entryList, format, overwriteParentDir)
    }

    /**
     * Persists every registered entry to [path], creating it if missing.
     * See [ConfigEntryFileUtils.persist] for [overwriteExisting] semantics.
     */
    fun persist(overwriteExisting: Boolean = true) {
        ConfigEntryFileUtils.persist(path, entryList, format, overwriteExisting, overwriteParentDir)
    }
}
