/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 */

package yv.tils.moderation.configs

import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ObjectMapperFileUtils

class ConfigFile {
    companion object {
        var state: ModerationConfigState = ModerationConfigState()
    }

    private val filePath = "/moderation/config.yml"

    fun loadConfig() {
        state = ObjectMapperFileUtils.load(filePath, ModerationConfigState(), format = ConfigFormat.YAML)
        registerStrings()
    }

    fun registerStrings() {
        ObjectMapperFileUtils.save(filePath, state, format = ConfigFormat.YAML)
    }

    /** Called by [yv.tils.gui.logic.DataClassConfigGui]'s saver after an in-game edit. */
    fun applyState(newState: ModerationConfigState) {
        state = newState
        registerStrings()
    }
}
