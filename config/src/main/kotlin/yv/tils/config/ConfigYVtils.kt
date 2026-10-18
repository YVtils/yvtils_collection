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

import yv.tils.config.language.Language
import yv.tils.utils.modules.Module

/**
 * Entry point for the Configurate-backed rewrite of the `config` module.
 *
 * Covers the same three areas as the original `config` module:
 * - `yv.tils.config.files` - YAML/JSON file loading, saving, and merging.
 * - `yv.tils.config.data` - the [yv.tils.config.data.ConfigEntry] schema
 *   used by the in-game config GUI (`ConfigGui` in `gui`), plus
 *   [yv.tils.config.data.EntryBackedConfig], a reusable replacement for
 *   the `entries`/`configIndex`/`get*`/`registerStrings`/`loadConfig`
 *   boilerplate duplicated across feature modules' `configs/ConfigFile.kt`.
 * - `yv.tils.config.language` - the translation-string system
 *   (`BuildLanguage`/`Language`/`LanguageHandler`/`LanguageProvider`/
 *   `LanguageBroadcast`), backed by [yv.tils.config.files.YamlFileUtils]
 *   instead of Bukkit's `YamlConfiguration`.
 *
 * Embedded in common's shared runtime bundle and used by every loaded feature.
 */
class ConfigYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "config",
            Module.readVersion(ConfigYVtils::class.java, "config"),
            "YVtils Configurate configuration and localization runtime",
            "YVtils",
            "https://docs.yvtils.net/config/"
        )
    }

    override fun onLoad() {}

    override fun enablePlugin() {
        Module.addModule(MODULE)
    }

    override fun onLateEnablePlugin() {
        Language().loadLanguageFiles()
    }

    override fun disablePlugin() {
        Module.removeModule(MODULE)
    }
}
