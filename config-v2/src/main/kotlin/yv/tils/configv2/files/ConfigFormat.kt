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
 * The file formats [ConfigurateFileUtils] knows how to load/save, each backed
 * by a dedicated Configurate loader module (`configurate-yaml`/
 * `configurate-gson`) rather than a hand-rolled parser.
 */
enum class ConfigFormat(val extension: String) {
    YAML("yml"),
    JSON("json");

    companion object {
        /**
         * Resolves a [ConfigFormat] from a file extension (without the
         * leading dot, case-insensitive), or `null` if unsupported.
         */
        fun fromExtension(extension: String): ConfigFormat? =
            entries.find { it.extension.equals(extension, ignoreCase = true) }
    }
}
