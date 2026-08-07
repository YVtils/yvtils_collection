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
import java.io.File

/**
 * A loaded (or in-memory, not-yet-saved) configuration file.
 *
 * Unlike the original `config` module's `YAMLFile`/`JSONFile` (two unrelated
 * data classes with duplicated load/save/merge logic behind them), a single
 * [ConfigFile] represents either format: [ConfigurationNode] is Configurate's
 * common abstraction over YAML/JSON/HOCON/XML trees, so [ConfigurateFileUtils]
 * only needs to special-case the format at the loader-selection boundary
 * (see [ConfigFormat]), not throughout every read/write/merge operation.
 *
 * [file] is already fully resolved (plugin-folder-relative or absolute,
 * depending on how it was loaded/created) - operations that take a
 * [ConfigFile] directly never need to re-resolve or re-trim the path.
 */
data class ConfigFile(
    val file: File,
    val node: ConfigurationNode,
    val format: ConfigFormat,
)
