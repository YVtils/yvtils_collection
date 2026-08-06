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

package yv.tils.core

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import yv.tils.common.CommonYVtils
import yv.tils.configv2.ConfigV2YVtils
import yv.tils.regions.RegionsYVtils
import yv.tils.utils.UtilsYVtils
import yv.tils.utils.logger.DEBUG_LEVEL
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module

@Suppress("UnstableApiUsage")
class YVtils : JavaPlugin() {
    companion object {
        val yvtilsVersion = YVtils().pluginMeta.version
        lateinit var instance: YVtils

        const val PLUGIN_NAME_FULL = "YVtils-Regions"
        const val PLUGIN_NAME = "Regions"
        const val PLUGIN_NAME_SHORT = "rg"
        const val PLUGIN_COLOR = "#4CAF50"
    }

    private val modules: List<Module.YVtilsModule> = listOf(
        ConfigV2YVtils(),
        UtilsYVtils(),
        RegionsYVtils(),
        CommonYVtils()
    )

    override fun onLoad() {
        instance = this

        Logger.logger = componentLogger
        Logger.debug("$PLUGIN_NAME_FULL v$yvtilsVersion is loading...", DEBUG_LEVEL.BASIC)

        val core = Core.YVtilsCore(
            description = "YVtils Regions plugin",
            url = "https://modrinth.com/plugin/yvtils_rg",

            dependencies = listOf(
                "regions"
            ),

            supportedVersions = listOf(),

            name = PLUGIN_NAME,
            colorHex = PLUGIN_COLOR,
            pluginShort = PLUGIN_NAME_SHORT,

            version = yvtilsVersion,
            instance = instance,

            key = NamespacedKey(this, "yvtils"),
        )

        Core.initCore(core)

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
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is starting...", DEBUG_LEVEL.BASIC)

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
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is performing late enable...", DEBUG_LEVEL.BASIC)

        try {
            modules.forEach { it.onLateEnablePlugin() }
        } catch (e: Exception) {
            Logger.error("Error during YVtils late startup: ${e.message}")
            e.printStackTrace()
        }

        if (!CheckVersion().serverVersion()) {
            Logger.error("----------")
            Logger.error("YVtils does not support the current server version (${Core.instance.server.version}).")
            Logger.error("Please use a supported server version: ${Core.core.supportedVersions.joinToString(", ")}")
            Logger.error("If you are still having issues, please contact the YVtils support team.")
            Logger.error("You can find the support team on our Discord server: https://yvtils.net/yvtils/support")
            Logger.error("----------")
            Logger.error("The plugin will now disable to prevent further issues.")

            instance.server.pluginManager.disablePlugin(instance)
            return
        }

        val dependencyCheck = CheckRequirements().checkModules()
        if (!dependencyCheck.first) {
            Logger.error("----------")
            Logger.error("Missing dependency: ${dependencyCheck.second}")
            Logger.error("The YVtils Core, of the plugin you are using, requires this dependency to function properly.")
            Logger.error("Please check if you filled in required values into the config files.")
            Logger.error("If you are still having issues, please contact the YVtils support team.")
            Logger.error("You can find the support team on our Discord server: https://yvtils.net/yvtils/support")
            Logger.error("----------")
            Logger.error("The plugin will now disable to prevent further issues.")

            instance.server.pluginManager.disablePlugin(instance)
            return
        }

        val loadedModules = Module.getModulesString(true)

        Logger.info("----------")
        Logger.info("YVtils Collection by YVtils")
        Logger.info("$PLUGIN_NAME v$yvtilsVersion has been enabled successfully!")
        Logger.info("The following modules have been enabled:")
        Logger.info(loadedModules)
        Logger.info("If you are having issues, please contact the YVtils support team.")
        Logger.info("You can find the support team on our Discord server: https://yvtils.net/yvtils/support")
        Logger.info("----------")
    }

    override fun onDisable() {
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is stopping...", DEBUG_LEVEL.BASIC)

        try {
            modules.forEach { it.disablePlugin() }
        } catch (e: Exception) {
            Logger.error("Error during YVtils shutdown: ${e.message}")
            e.printStackTrace()
        }

        Logger.info("----------")
        Logger.info("YVtils Collection by YVtils")
        Logger.info("$PLUGIN_NAME v$yvtilsVersion has been disabled successfully!")
        Logger.info("----------")
    }
}
