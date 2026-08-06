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

package yv.tils.configv2

import kotlinx.serialization.Serializable

/**
 * Ported as-is from the original `config` module's `GeneralConfigManager`.
 *
 * Note (pre-existing, not introduced by this port): nothing in either the
 * old `config` module or here actually loads `GeneralConfigManager.config`
 * from disk - it's always just the in-memory default [GeneralConfig]. Every
 * call site (`core/CheckVersion.kt`, and its per-launcher copies) only ever
 * observes the default values. This looks like an incomplete integration
 * left over from before `common`'s own `/config.yml` handling
 * (`yv.tils.common.config.ConfigFile`) existed, rather than something safe
 * to silently "fix" as part of a migration - flagging it here instead.
 */
class GeneralConfigManager {
    companion object {
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
