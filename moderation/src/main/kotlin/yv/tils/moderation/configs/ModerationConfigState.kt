/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.moderation.configs

import yv.tils.configv2.data.annotations.NotGuiEditable
import yv.tils.configv2.data.annotations.ConfigDescription

/**
 * moderation's `config.yml`, as a plain `data class` persisted via `ObjectMapperFileUtils` -
 * replaces the old `ConfigEntry`-list-built-by-hand version of this file. No GUI is wired up
 * yet (see `DataClassConfigGui`/multiMine's `ManageGUI` for the reference implementation),
 * but any field added here with `@ConfigDescription` is already GUI-ready.
 */
data class ModerationConfigState(
    @NotGuiEditable
    @ConfigDescription("Documentation URL")
    val documentation: String = "https://docs.yvtils.net/moderation/config.yml",
)
