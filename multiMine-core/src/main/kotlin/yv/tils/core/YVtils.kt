/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.core

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import yv.tils.common.CommonYVtils
import yv.tils.config.ConfigYVtils
import yv.tils.gui.GUIYVtils
import yv.tils.migration.MigrationYVtils
import yv.tils.multiMine.MultiMineYVtils
import yv.tils.stats.StatsYVtils
import yv.tils.utils.UtilsYVtils
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger

class YVtils: JavaPlugin() {
    companion object {
        val yvtilsVersion = YVtils().pluginMeta.version
        lateinit var instance: YVtils

        const val PLUGIN_NAME_FULL = "YVtils-MultiMine"
        const val PLUGIN_NAME = "MultiMine"
        const val PLUGIN_NAME_SHORT = "mm"
        const val PLUGIN_COLOR = "#66cbe8"
    }

    private val modules: List<Data.YVtilsModule> = listOf(
        ConfigYVtils(),
        UtilsYVtils(),
        MigrationYVtils(), // TODO: Remove with 2.1.0
        MultiMineYVtils(),
        GUIYVtils(),
        StatsYVtils(),
        CommonYVtils()
    )

    override fun onLoad() {
        instance = this

        Logger.logger = componentLogger
        Logger.debug("$PLUGIN_NAME_FULL v$yvtilsVersion is loading...")

        val core = Data.YVtilsCore(
            description = "",
            url = "https://modrinth.com/plugin/yvtils_mm",

            dependencies = listOf(
                "multiMine"
            ),

            name = PLUGIN_NAME,
            colorHex = PLUGIN_COLOR,
            pluginShort = PLUGIN_NAME_SHORT,

            version = yvtilsVersion,
            instance = instance,

            key = NamespacedKey(this, "yvtils"),
        )

        Data.initCore(core)

        CommandAPI.onLoad(
            CommandAPIPaperConfig(instance)
                .setNamespace("yvtils")
                .silentLogs(true)
                .verboseOutput(false)
                .fallbackToLatestNMS(true)
        )

        try {
            modules.forEach { it.onLoad() }
        } catch (e: Exception) {
            Logger.error("Error during YVtils loading: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onEnable() {
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is starting...")

        try {
            modules.forEach { it.enablePlugin() }
        } catch (e: Exception) {
            Logger.error("Error during YVtils startup: ${e.message}")
            e.printStackTrace()
        }

        if (instance.isEnabled) {
            onLateEnablePlugin()
        }
    }

    fun onLateEnablePlugin() {
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is performing late enable...")

        try {
            modules.forEach { it.onLateEnablePlugin() }
        } catch (e: Exception) {
            Logger.error("Error during YVtils late startup: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onDisable() {
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is stopping...")

        try {
            modules.forEach { it.disablePlugin() }
        } catch (e: Exception) {
            Logger.error("Error during YVtils shutdown: ${e.message}")
            e.printStackTrace()
        }
    }
}
