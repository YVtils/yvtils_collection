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

package yv.tils.configv2.files

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.loader.ConfigurationLoader
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import java.io.File
import java.io.FileNotFoundException

/**
 * Format-agnostic load/save/merge logic for YAML and JSON files, backed by
 * Configurate instead of Bukkit's `YamlConfiguration` + hand-rolled
 * `kotlinx.serialization` JSON merging.
 *
 * This single object replaces the original `config` module's `FileUtils` +
 * `YMLFileUtils` + `JSONFileUtils` trio:
 * - Path resolution (plugin-folder-relative vs. absolute) was previously
 *   duplicated across four different functions across those three files, with
 *   `saveFile` even forgetting to apply it - here it's defined exactly once
 *   in [resolvePath] and every function goes through it.
 * - The recursive JSON-merge functions (`mergeJsonObjects`,
 *   `mergeJsonObjectsWithArrayAppend`) and the separate YAML-merge branch in
 *   `updateFile` are replaced by Configurate's built-in
 *   [ConfigurationNode.mergeFrom]/[ConfigurationNode.from], which do the same
 *   job for *any* format Configurate supports, not just these two.
 * - `loadFilesFromFolder` previously returned `List<Any>` with callers doing
 *   an unsafe `mapNotNull { it as? YAMLFile }`; here it's `List<ConfigFile>`
 *   from the start.
 */
class ConfigurateFileUtils {
    companion object {
        /**
         * Overrides the base directory every load/save/update/create/
         * loadFilesFromFolder call resolves its (non-absolute) `path`
         * against, instead of the [defaultBaseDirectory].
         *
         * `null` (the default) means "keep using [defaultBaseDirectory]",
         * computed fresh on every call. Set this once (e.g. from your
         * plugin's `onLoad()`, before any config-v2 file I/O happens, and
         * *after* `Core.initCore(...)`) to redirect all of it elsewhere -
         * useful for things like a dedicated data directory, or pointing a
         * test harness at a scratch directory instead of a real plugin
         * folder.
         *
         * This only affects calls made with the default
         * `overwriteParentDir = false`; passing `overwriteParentDir = true`
         * (or an already-absolute path) still bypasses both this and
         * [defaultBaseDirectory] entirely, same as before.
         *
         * IMPORTANT: keep this default as `null` (i.e. "lazily compute
         * [defaultBaseDirectory] inside [resolvePath], read fresh on every
         * call"). Do NOT initialize it eagerly with an expression that reads
         * [Core.instance] here (e.g. `File(Core.instance.dataPath...)`) -
         * a companion object property initializer runs exactly once, at
         * whatever moment the JVM first touches this class, which is not
         * guaranteed to be after `Core.initCore()` has run. If you need a
         * custom directory, assign it explicitly and unconditionally from
         * your own plugin's `onLoad()`, *after* `Core.initCore(...)`.
         */
        var baseDirectory: File? = null

        /**
         * The default base directory used when [baseDirectory] hasn't been
         * explicitly overridden: a `yvtils` folder as a *sibling* of the
         * current plugin's own data folder (i.e. `plugins/yvtils/`, next to
         * `plugins/<PluginName>/`) rather than inside it.
         *
         * This is shared across every YVtils launcher jar (`core`,
         * `discord-core`, `multiMine-core`, `regions-core`, `test-core`,
         * ...) - each of those is a *separate* Bukkit plugin with its own
         * `dataFolder`, but they're all part of the same ecosystem, so
         * config-v2-backed data (a module's `/multiMine/config.yml`,
         * `/languages/en.yml`, etc.) defaults to one common location instead
         * of being duplicated/scattered per-launcher.
         *
         * Computed lazily, on every call (same as [Core.pluginFolder]
         * itself) - never cache this in a stored property, since
         * [Core.instance] may not be initialized yet at class-load time
         * (see the warning on [baseDirectory] above for why that matters).
         */
        fun defaultBaseDirectory(): File {
            val pluginFolder = Core.pluginFolder
            val pluginsDirectory = pluginFolder.parentFile ?: pluginFolder
            return File(pluginsDirectory, "yvtils")
        }

        /**
         * Resolves [path] relative to [baseDirectory] (or
         * [defaultBaseDirectory] if that's `null`, the default), unless
         * [overwriteParentDir] is set (in which case [path] is used as-is).
         *
         * Defined exactly once - every load/save/update function below goes
         * through this, so there's only one place that can get the
         * relative-vs-absolute or leading-slash handling wrong.
         */
        private fun resolvePath(path: String, overwriteParentDir: Boolean): File =
            if (overwriteParentDir) {
                File(path)
            } else {
                File(baseDirectory ?: defaultBaseDirectory(), path.trimStart('/', '\\'))
            }

        private fun formatOf(file: File): ConfigFormat =
            ConfigFormat.fromExtension(file.extension)
                ?: throw IllegalArgumentException("Unsupported file extension: ${file.extension} (for ${file.path})")

        private fun loaderFor(file: File, format: ConfigFormat): ConfigurationLoader<*> =
            when (format) {
                ConfigFormat.YAML -> YamlConfigurationLoader.builder()
                    .file(file)
                    .indent(2)
                    .nodeStyle(NodeStyle.BLOCK)
                    .build()

                ConfigFormat.JSON -> GsonConfigurationLoader.builder()
                    .file(file)
                    .indent(2)
                    .lenient(true)
                    .build()
            }

        /**
         * Loads a single config file of the given [format].
         *
         * @throws FileNotFoundException if the file doesn't exist.
         */
        fun load(path: String, format: ConfigFormat, overwriteParentDir: Boolean = false): ConfigFile {
            val file = resolvePath(path, overwriteParentDir)
            if (!file.exists()) throw FileNotFoundException("File not found: $path")

            Logger.debug("Loading ${format.name} file: $path")

            val node = loaderFor(file, format).load()
            return ConfigFile(file, node, format)
        }

        /**
         * Loads a single config file, auto-detecting the format from its
         * extension (`.yml`/`.yaml` or `.json`).
         *
         * @throws FileNotFoundException if the file doesn't exist.
         * @throws IllegalArgumentException if the extension isn't supported.
         */
        fun load(path: String, overwriteParentDir: Boolean = false): ConfigFile =
            load(path, formatOf(resolvePath(path, overwriteParentDir)), overwriteParentDir)

        /**
         * Loads every file of the given [format] directly inside [folder].
         * Returns an empty list if the folder doesn't exist. Files that fail
         * to parse are skipped (and logged) rather than failing the whole
         * batch.
         */
        fun loadFilesFromFolder(
            folder: String,
            format: ConfigFormat,
            overwriteParentDir: Boolean = false
        ): List<ConfigFile> {
            Logger.debug("Loading ${format.name} files from folder: $folder")

            val directory = resolvePath(folder, overwriteParentDir)
            if (!directory.exists() || !directory.isDirectory) return emptyList()

            return directory.listFiles { _, name -> name.endsWith(".${format.extension}", ignoreCase = true) }
                ?.sortedBy { it.name }
                ?.mapNotNull { file ->
                    runCatching { load("$folder/${file.name}", format, overwriteParentDir) }
                        .onFailure { Logger.debug("Failed to load ${file.name}: ${it.message}") }
                        .getOrNull()
                }
                ?: emptyList()
        }

        /**
         * Builds a new, in-memory (not yet saved) [ConfigFile] of the given
         * [format] from a plain map. Call [save] to actually persist it.
         *
         * [content] must only contain types Configurate can store "raw"
         * (maps, lists, strings, numbers, booleans, null) - for anything
         * else (e.g. a Bukkit `Material`), set it on [ConfigFile.node]
         * directly via `node.node("key").set(MyType::class.java, value)`
         * after creating the file.
         */
        fun create(
            path: String,
            content: Map<String, Any?>,
            format: ConfigFormat,
            overwriteParentDir: Boolean = false
        ): ConfigFile {
            Logger.debug("Creating ${format.name} file: $path")

            val file = resolvePath(path, overwriteParentDir)
            val node = loaderFor(file, format).createNode()
            node.raw(content)

            Logger.debug("Node contents: ${node.raw()}", 3)

            return ConfigFile(file, node, format)
        }

        /**
         * Persists [configFile] to disk, creating parent directories and
         * overwriting any existing file at that path.
         */
        fun save(configFile: ConfigFile) {
            Logger.debug("Saving ${configFile.format.name} file: ${configFile.file.path}")

            configFile.file.parentFile?.mkdirs()
            loaderFor(configFile.file, configFile.format).save(configFile.node)
        }

        /**
         * Merges [configFile] into whatever is already on disk at its path,
         * creating the file if it doesn't exist yet.
         *
         * When [overwriteExisting] is `false` (the default), existing values
         * on disk win and only *missing* keys are filled in from
         * [configFile] - equivalent to the original module's
         * `FileUtils.updateFile(overwriteExisting = false)`, but implemented
         * once via [ConfigurationNode.mergeFrom] instead of two separate
         * hand-rolled recursive merge functions (one per format).
         *
         * When `true`, [configFile] completely replaces the file's contents
         * via [ConfigurationNode.from].
         */
        fun update(configFile: ConfigFile, overwriteExisting: Boolean = false) {
            Logger.debug("Updating ${configFile.format.name} file: ${configFile.file.path} | overwriteExisting: $overwriteExisting")

            if (!configFile.file.exists()) {
                Logger.debug("File doesn't exist, creating new file: ${configFile.file.path}")
                save(configFile)
                return
            }

            val loader = loaderFor(configFile.file, configFile.format)
            val existing = loader.load()

            if (overwriteExisting) {
                existing.from(configFile.node)
            } else {
                existing.mergeFrom(configFile.node)
            }

            loader.save(existing)
        }

        /**
         * Flattens a loaded map-of-maps [node] into a `"a.b.c" -> value`
         * map for every leaf (non-map) node, preserving each value's raw
         * type (`String`/`Number`/`Boolean`/`List<*>`/...).
         *
         * This is the Configurate-based replacement for the
         * `file.content.getKeys(true)` + `file.content.get(key)` pattern
         * every module's `configs/ConfigFile.kt` used against a Bukkit
         * `YamlConfiguration` to populate its own flat `config: MutableMap<String, Any>`
         * cache - Bukkit's `getKeys(true)` returns dotted paths for every
         * depth, which [ConfigurationNode] doesn't do on its own since it
         * models nesting structurally rather than through dotted keys.
         *
         * Unlike [yv.tils.configv2.language.Language]'s private `flatten` (which only
         * keeps `String` leaves, since language files are always string-valued),
         * this keeps whatever [ConfigurationNode.raw] returns for each leaf.
         */
        fun flattenToMap(node: ConfigurationNode, prefix: String = ""): Map<String, Any> {
            if (!node.isMap()) {
                val value = node.raw() ?: return emptyMap()
                return mapOf(prefix to value)
            }

            val result = mutableMapOf<String, Any>()
            node.childrenMap().forEach { (key, child) ->
                val path = if (prefix.isEmpty()) key.toString() else "$prefix.$key"
                result.putAll(flattenToMap(child, path))
            }
            return result
        }
    }
}
