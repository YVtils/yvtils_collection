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
 * Thin, YAML-specific facade over [ConfigurateFileUtils], for call sites that
 * know they're dealing with `.yml` files and don't want to pass
 * [ConfigFormat.YAML] around explicitly (mirrors the original module's
 * `YMLFileUtils`).
 *
 * The returned [ConfigFile.node] is a `CommentedConfigurationNode` under the
 * hood. Note this only preserves the document *header* - a comment block at
 * the very top of the file followed by a blank line - across load/save;
 * per-key/inline comments are not read or re-emitted (Configurate's YAML
 * loader disables SnakeYAML's own comment processing). This is still an
 * improvement over Bukkit's `YamlConfiguration`, which preserves neither.
 */
class YamlFileUtils {
    companion object {
        fun loadYamlFile(path: String, overwriteParentDir: Boolean = false): ConfigFile =
            ConfigurateFileUtils.load(path, ConfigFormat.YAML, overwriteParentDir)

        fun loadYamlFilesFromFolder(folder: String, overwriteParentDir: Boolean = false): List<ConfigFile> =
            ConfigurateFileUtils.loadFilesFromFolder(folder, ConfigFormat.YAML, overwriteParentDir)

        fun makeYamlFile(path: String, content: Map<String, Any?>, overwriteParentDir: Boolean = false): ConfigFile =
            ConfigurateFileUtils.create(path, content, ConfigFormat.YAML, overwriteParentDir)
    }
}
