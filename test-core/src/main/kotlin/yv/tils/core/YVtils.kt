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
import yv.tils.moderation.ModerationYVtils
import yv.tils.utils.UtilsYVtils
import yv.tils.utils.data.Data
import yv.tils.utils.logger.DEBUGLEVEL
import yv.tils.utils.logger.Logger

@Suppress("UnstableApiUsage")
class YVtils: JavaPlugin() {
    companion object {
        val yvtilsVersion = YVtils().pluginMeta.version
        lateinit var instance: YVtils

        const val PLUGIN_NAME_FULL = "TEST-YVTILS-CORE"
        const val PLUGIN_NAME = "TEST-YVTILS-CORE"
        const val PLUGIN_NAME_SHORT = "test"
        const val PLUGIN_COLOR = "#66cbe8"
    }

    private val modules: List<Data.YVtilsModule> = listOf(
        ConfigYVtils(),
        UtilsYVtils(),
        ModerationYVtils(),
        GUIYVtils(),
        CommonYVtils()
    )

    override fun onLoad() {
        instance = this

        Logger.logger = componentLogger
        Logger.debug("$PLUGIN_NAME_FULL v$yvtilsVersion is loading...", DEBUGLEVEL.BASIC)

        val core = Data.YVtilsCore(
            description = "",
            url = "",

            dependencies = listOf(
                "common"
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
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is starting...", DEBUGLEVEL.BASIC)

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
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is performing late enable...", DEBUGLEVEL.BASIC)

        Logger.error("------------------------------------------------")
        Logger.error("IF YOU SEE THIS MESSAGE, YOU SOMEHOW GOT ACCESS TO A TEST BUILD OF THE TEST CORE PLUGIN.")
        Logger.error("THIS BUILD IS NOT INTENDED FOR PUBLIC USAGE AND MAY MISS FUNCTIONALITY OR CAUSE ISSUES.")
        Logger.error("PLEASE DOWNLOAD THE OFFICIAL BUILD FROM THE MODRINTH PAGE.")
        Logger.error("------------------------------------------------")

        try {
            modules.forEach { it.onLateEnablePlugin() }
        } catch (e: Exception) {
            Logger.error("Error during YVtils late startup: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onDisable() {
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is stopping...", DEBUGLEVEL.BASIC)

        try {
            modules.forEach { it.disablePlugin() }
        } catch (e: Exception) {
            Logger.error("Error during YVtils shutdown: ${e.message}")
            e.printStackTrace()
        }
    }
}
