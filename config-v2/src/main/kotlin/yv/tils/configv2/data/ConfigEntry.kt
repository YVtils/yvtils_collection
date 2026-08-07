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

import org.bukkit.Material

/**
 * A single editable config value, as used by the in-game config GUI
 * (`ConfigGui` in `gui`) and [ConfigEntryFileUtils]/[EntryBackedConfig]
 * for persistence. Unchanged from the original `config` module's
 * `yv.tils.config.data.ConfigEntry` - this is plain data with no
 * Configurate/Bukkit-config dependency of its own.
 */
data class ConfigEntry(
    val key: String,
    val type: EntryType,
    var value: Any? = null,
    val defaultValue: Any?,
    val description: String?,
    val invItem: Material? = null,
    val dynamicInvItem: ((ConfigEntry) -> Material)? = null,
    val requiresRestartOnChange: Boolean = false, // TODO: Implement logic
)
