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

/**
 * Thin, JSON-specific facade over [ConfigurateFileUtils] (mirrors the
 * original module's `JSONFileUtils`).
 *
 * The returned [ConfigFile.node] is a `BasicConfigurationNode` backed by
 * Gson - no separate `mergeJsonObjects`/`mergeJsonObjectsWithArrayAppend`
 * functions are needed here, [ConfigurateFileUtils.update] handles merging
 * for this format the same way it does for YAML.
 */
class JsonFileUtils {
    companion object {
        fun loadJsonFile(path: String, overwriteParentDir: Boolean = false): ConfigFile =
            ConfigurateFileUtils.load(path, ConfigFormat.JSON, overwriteParentDir)

        fun loadJsonFilesFromFolder(folder: String, overwriteParentDir: Boolean = false): List<ConfigFile> =
            ConfigurateFileUtils.loadFilesFromFolder(folder, ConfigFormat.JSON, overwriteParentDir)

        fun makeJsonFile(path: String, content: Map<String, Any?>, overwriteParentDir: Boolean = false): ConfigFile =
            ConfigurateFileUtils.create(path, content, ConfigFormat.JSON, overwriteParentDir)
    }
}
