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

import yv.tils.configv2.language.Language
import yv.tils.utils.modules.Module

/**
 * Entry point for the Configurate-backed rewrite of the `config` module.
 *
 * Covers the same three areas as the original `config` module:
 * - `yv.tils.configv2.files` - YAML/JSON file loading, saving, and merging.
 * - `yv.tils.configv2.data` - the [yv.tils.configv2.data.ConfigEntry] schema
 *   used by the in-game config GUI (`ConfigGui` in `gui`), plus
 *   [yv.tils.configv2.data.EntryBackedConfig], a reusable replacement for
 *   the `entries`/`configIndex`/`get*`/`registerStrings`/`loadConfig`
 *   boilerplate duplicated across feature modules' `configs/ConfigFile.kt`.
 * - `yv.tils.configv2.language` - the translation-string system
 *   (`BuildLanguage`/`Language`/`LanguageHandler`/`LanguageProvider`/
 *   `LanguageBroadcast`), backed by [yv.tils.configv2.files.YamlFileUtils]
 *   instead of Bukkit's `YamlConfiguration`.
 *
 * `config-v2` is not yet wired into `common`'s shared runtime bundle, and no
 * existing feature module has been migrated to it yet; it can be built and
 * depended on standalone while it coexists with the original `config`
 * module.
 */
class ConfigV2YVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "config-v2",
            "26.08.01",
            "YVtils Config Module (v2, Configurate-backed)",
            "YVtils",
            "https://docs.yvtils.net/config-v2/"
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
