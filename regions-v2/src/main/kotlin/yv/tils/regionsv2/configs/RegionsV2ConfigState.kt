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

package yv.tils.regionsv2.configs

import yv.tils.configv2.data.annotations.ConfigDescription
import yv.tils.configv2.data.annotations.NotGuiEditable

/**
 * regions-v2's `config.yml`, persisted as a plain data class via
 * `ObjectMapperFileUtils` and editable in-game through [yv.tils.gui.logic.DataClassConfigGui]
 * (see [yv.tils.regionsv2.configs.ManageGUI]) - add more fields here as the module grows,
 * annotated with [ConfigDescription] (GUI lore text) and, where relevant, [NotGuiEditable].
 */
data class RegionsV2ConfigState(
    @NotGuiEditable
    @ConfigDescription("Documentation URL")
    val documentation: String = "https://docs.yvtils.net/regions-v2/config.yml",

    @ConfigDescription("Whether regions-v2 is enabled")
    var enabled: Boolean = true,
)
