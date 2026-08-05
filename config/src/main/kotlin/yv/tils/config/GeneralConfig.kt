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

package yv.tils.config

import kotlinx.serialization.Serializable

class GeneralConfigManager {
    companion object {
        private const val CONFIG_FILE_NAME = "config.json"

        var config: GeneralConfig = GeneralConfig()
    }
}

@Serializable
data class GeneralConfig(
    var updateCheck: UpdateCheckConfig = UpdateCheckConfig()
)

@Serializable
data class UpdateCheckConfig(
    var enabled: Boolean = true,
    var notifyOnJoin: Boolean = true
)