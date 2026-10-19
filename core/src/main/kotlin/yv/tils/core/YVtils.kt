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
import yv.tils.config.ConfigYVtils
import yv.tils.core.commands.register.YVtilsCommand
import yv.tils.core.loader.DynamicModuleDriver
import yv.tils.core.loader.ModuleConfig
import yv.tils.gui.logic.CommonConfigGui
import yv.tils.utils.UtilsYVtils
import yv.tils.utils.logger.DEBUG_LEVEL
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module
import java.util.function.Consumer

class YVtils : JavaPlugin() {
    companion object {
        val yvtilsVersion: String get() = instance.pluginMeta.version
        lateinit var instance: YVtils

        const val PLUGIN_NAME_FULL = "YVtils Collection"
        const val PLUGIN_NAME = "Collection"
        const val PLUGIN_NAME_SHORT = "col"
        const val PLUGIN_COLOR = "#D6E0C6"
    }

    // Every module `core` can statically or dynamically load (the always-
    // embedded `common`/`utils` trio, plus everything in
    // `DynamicModuleRegistry.KNOWN_MODULES`) has been migrated to
    // `config` - only `ConfigYVtils` needs to run now.
    private val modules: List<Module.YVtilsModule> = listOf(
        ConfigYVtils(),
        UtilsYVtils(),
        CommonYVtils()
    )

    private var dynamicModules: List<Module.YVtilsModule> = listOf()
    private var dynamicModuleDiscovery: DynamicModuleDriver.DiscoveryResult? = null
    private val initializedModules = mutableListOf<Module.YVtilsModule>()
    private var startupFailed = false

    private fun phase(name: String, targets: List<Module.YVtilsModule>, action: (Module.YVtilsModule) -> Unit): Boolean {
        var succeeded = true
        targets.forEach { module ->
            try {
                action(module)
            } catch (error: Throwable) {
                if (error is VirtualMachineError) throw error
                succeeded = false
                Logger.error("Module ${module.javaClass.name} failed during $name", error)
            }
        }
        if (!succeeded) startupFailed = true
        return succeeded
    }

    override fun onLoad() {
        instance = this

        Logger.logger = componentLogger
        Logger.debug("$PLUGIN_NAME_FULL v$yvtilsVersion is loading...", DEBUG_LEVEL.BASIC)

        val core = Core.YVtilsCore(
            description = "A collection of useful plugins for Minecraft servers.",
            url = "https://modrinth.com/plugin/yvtils",

            dependencies = listOf(
                "common",
            ),

            supportedVersions = listOf(
                "26.1.2",
                "26.2",
                "26.3"
            ),

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
                .verboseOutput(true)
                .fallbackToLatestNMS(true)
        )

        phase("load", modules) {
            initializedModules.add(it)
            it.onLoad()
        }

        val discovery = DynamicModuleDriver.discover(ModuleConfig.sharedDataDirectory(dataFolder.toPath()))

        discovery.succeeded.forEach {
            Logger.info("[DynamicModuleDriver] Module '$it' resolved and loaded successfully.")
        }
        discovery.failed.forEach { (name, reason) ->
            Logger.warn("[DynamicModuleDriver] Module '$name' failed to load: $reason")
        }

        dynamicModules = discovery.loaded
        dynamicModuleDiscovery = discovery

        if (discovery.failed.isNotEmpty()) startupFailed = true
        phase("load", dynamicModules) {
            initializedModules.add(it)
            it.onLoad()
        }
    }

    override fun onEnable() {
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is starting...", DEBUG_LEVEL.BASIC)

        if (startupFailed || !CheckVersion().serverVersion()) {
            Logger.error("YVtils startup aborted: module loading failed or the server version is unsupported.")
            server.pluginManager.disablePlugin(this)
            return
        }
        if (!phase("enable", modules + dynamicModules) { it.enablePlugin() }) {
            server.pluginManager.disablePlugin(this)
            return
        }

        // `common` can't register its own `configGuiOpener` (doing so would need a
        // `common` -> `gui` dependency, but `gui` already depends on `common` - see
        // `CommonConfigGui`'s KDoc), so `core` - which already embeds/resolves both - patches
        // it in here instead, right after `common` has registered itself via `Module.addModule`.
        Module.getModule("common")?.let { commonModule ->
            Module.removeModule(commonModule)
            Module.addModule(commonModule.copy(configGuiOpener = Consumer { player -> CommonConfigGui.open(player) }))
        }

        YVtilsCommand()
        CommandAPI.onEnable()

        if (instance.isEnabled) {
            onLateEnablePlugin()
        }
    }

    fun onLateEnablePlugin() {
        Logger.debug("$PLUGIN_NAME v$yvtilsVersion is performing late enable...", DEBUG_LEVEL.BASIC)

        if (!phase("late enable", modules + dynamicModules) { it.onLateEnablePlugin() }) {
            server.pluginManager.disablePlugin(this)
            return
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

        val discovery = dynamicModuleDiscovery
        if (discovery != null && discovery.failed.isNotEmpty()) {
            Logger.warn("----------")
            Logger.warn("${discovery.failed.size} dynamic module(s) failed to load:")
            discovery.failed.forEach { (name, reason) -> Logger.warn(" - $name: $reason") }
            Logger.warn("----------")

            if (discovery.succeeded.isEmpty() && discovery.failed.isNotEmpty()) {
                Logger.error("----------")
                Logger.error("None of the configured dynamic modules could be loaded.")
                Logger.error("The plugin will now disable to prevent further issues.")
                Logger.error("----------")

                instance.server.pluginManager.disablePlugin(instance)
                return
            }
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

        phase("disable", initializedModules.asReversed()) { it.disablePlugin() }
        initializedModules.clear()
        if (CommandAPI.isLoaded()) CommandAPI.onDisable()

        Logger.info("----------")
        Logger.info("YVtils Collection by YVtils")
        Logger.info("$PLUGIN_NAME v$yvtilsVersion has been disabled successfully!")
        Logger.info("----------")
    }
}
