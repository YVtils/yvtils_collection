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

package yv.tils.utils.modules

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import yv.tils.utils.colors.Colors
import java.io.File

class Core {
    companion object {
        var yvtilsVersion = ""

        lateinit var instance: JavaPlugin
        lateinit var key: NamespacedKey
        lateinit var core: YVtilsCore

        /**
         * The folder where the plugin is located.
         * This is used to store plugin data and configuration files.
         */
        val pluginFolder: File
            get() = instance.dataFolder

        /**
         * The chat prefix used for messages sent by this core, e.g. "[YVtils <name>]".
         */
        val prefix: String
            get() = "<dark_gray>[<${Colors.MAIN.color}>YVtils ${core.name}<dark_gray>]<white>"

        fun initCore(core: YVtilsCore) {
            this.core = core
            instance = core.instance
            key = core.key
            yvtilsVersion = core.version
        }
    }

    data class YVtilsCore(
        val name: String,
        val version: String,
        val supportedVersions: List<String> = listOf(),
        val description: String = "",
        val colorHex: String = "",
        val instance: JavaPlugin,
        val key: NamespacedKey,
        val pluginShort: String,
        val url: String = "https://modrinth.com/organization/yvtils",
        val dependencies: List<String> = listOf(),
    )
}