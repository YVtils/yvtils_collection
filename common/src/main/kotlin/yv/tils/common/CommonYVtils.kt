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

package yv.tils.common

import net.kyori.adventure.text.Component
import yv.tils.common.config.ConfigFile
import yv.tils.common.data.PermissionsData
import yv.tils.common.language.*
import yv.tils.common.listeners.PlayerJoin
import yv.tils.common.listeners.PlayerLocaleChange
import yv.tils.common.permissions.PermissionManager
import yv.tils.common.updateChecker.PluginVersion
import yv.tils.config.language.Language
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.data.Data
import yv.tils.utils.data.Data.Companion.yvtilsVersion
import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils
import yv.tils.utils.server.ServerUtils
import yv.tils.utils.time.TimeUtils

class CommonYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "common",
            "1.0.0",
            "Common module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/common/"
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
    }

    override fun enablePlugin() {
        Language().loadLanguageFiles()

        Data.addModule(MODULE)

        val modules = Data.getModulesAsString(sorted = true)

        val data = mutableMapOf(
            1 to listOf(LanguageHandler.getMessage("plugin.action.start", params = mapOf("prefix" to Data.prefix))),
            2 to listOf(MessageUtils.convert(modules)),
        )
        val log = printLogWithSplits(data)
        log.forEach { Logger.info(it) }

        if (! checkDependencies()) {
            Data.instance.server.pluginManager.disablePlugin(Data.instance)
            return
        } else {
            Logger.debug("All dependencies are loaded successfully.", 1)
        }

        registerListeners()
        registerCoroutines()
        registerPermissions()

        loadConfigs()

        PluginVersion().launchVersionCheck()
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {
        val data = mutableMapOf(
            1 to listOf(LanguageHandler.getMessage("plugin.action.stop", params = mapOf("prefix" to Data.prefix))),
        )

        val log = printLogWithSplits(data)
        log.forEach { Logger.info(it) }
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()

        Logger.setDebugMode(
            ConfigFile.getValueAsBoolean("debug.active") ?: false,
            ConfigFile.getValueAsInt("debug.level") ?: 3
        )

        LanguageHandler().setServerDefaultLanguage(ConfigFile.getValueAsString("language") ?: "en")

        TimeUtils.timeZone = ConfigFile.getValueAsString("timezone") ?: "default"
        ServerUtils.serverIP  = ConfigFile.getValueAsString("serverIP") ?: "smp.net"
        ServerUtils.serverPort = ConfigFile.getValueAsInt("serverPort") ?: -1

        TimeUtils.CONFIG_ERROR_INVALID_TIMEZONE = LanguageHandler.getRawMessage(LangStrings.CONFIG_ERROR_INVALID_TIMEZONE.key)
    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(PlayerLocaleChange(), plugin)
        pm.registerEvents(PlayerJoin(), plugin)
    }

    private fun registerPermissions() {
        PermissionManager.registerPermissions(PermissionsData().getPermissionList(true))

        val modules = Data.getModules()
        val moduleWildcards: Map<String, Boolean> = modules.map { "yvtils.${it.name}.*" }.associateWith { true }

        PermissionManager.registerPermissions(
            listOf(
                PermissionManager.YVtilsPermission(
                    "yvtils.*",
                    "Allows access to all YVtils features",
                    false,
                    moduleWildcards
                )
            )
        )
    }

    private fun registerCoroutines() {
        CoroutineHandler.launchTask(
            suspend { LoadPlayerLanguage().asyncCleanup() },
            "yvtils-language-cleanup",
            afterDelay = 5 * 1000 * 60,
        )
    }

    private fun checkDependencies(): Boolean {
        try {
            val core = Data.core
            val dependencies = core.dependencies

            if (dependencies.isEmpty()) {
                return true
            }

            val modules = Data.getModuleNames()
            for (dependency in dependencies) {
                if (! modules.contains(dependency)) {
                    Logger.error("----------")
                    Logger.error("Missing dependency: $dependency")
                    Logger.error("The YVtils Core, of the plugin you are using, requires this dependency to function properly.")
                    Logger.error("Please check if you filled in required values into the config files.")
                    Logger.error("If you are still having issues, please contact the YVtils support team.")
                    Logger.error("You can find the support team on our Discord server: https://yvtils.net/yvtils/support")
                    Logger.error("----------")
                    Logger.error("The plugin will now disable to prevent further issues.")
                    return false
                }
            }

            Logger.debug("All dependencies are loaded: ${dependencies.joinToString(", ")}")
            return true
        } catch (_: Exception) {
            Logger.error("YVtils core is not initialized. Please ensure the core is loaded before using YVtils features.")
            return false
        }
    }

    private fun printLogWithSplits(data: MutableMap<Int, List<Component>>): MutableList<Component> {
        val lineLength = 40
        val log = mutableListOf<Component>()
        val border = "+".repeat(lineLength)

        log.add(MessageUtils.convert(border))

        val hardMessages = listOf(
            "YVtils Collection v$yvtilsVersion",
            ">>> ${Data.pluginName} <<<",
            "https://yvtils.net",
        )

        val prefix = "|"
        val suffix = "|"

        fun getPadding(content: String): Pair<String, String> {
            val availableSpace = lineLength - (prefix.length + suffix.length) - content.length
            val leftPadding = availableSpace / 2
            val rightPadding = availableSpace - leftPadding
            return " ".repeat(leftPadding) to " ".repeat(rightPadding)
        }

        hardMessages.forEach { hardMessage ->
            val (paddingLeft, paddingRight) = getPadding(hardMessage)
            log.add(MessageUtils.convert("$prefix$paddingLeft$hardMessage$paddingRight$suffix"))
        }

        log.add(MessageUtils.convert(border))

        var lastKey: Int? = null
        data.toSortedMap().forEach { (key, messages) ->
            if (lastKey != null && key != lastKey) {
                log.add(MessageUtils.convert("|--------------------------------------|"))
            }

            messages.forEach { message ->
                val tempMessage = MessageUtils.strip(message)
                val words = tempMessage.split(" ")
                var currentLine = ""

                for (word in words) {
                    val potentialLine = if (currentLine.isEmpty()) word else "$currentLine $word"

                    if (potentialLine.length <= lineLength - 4) {
                        currentLine = potentialLine
                    } else {
                        val (leftPad, rightPad) = getPadding(currentLine)
                        log.add(MessageUtils.joinedConvert(prefix, leftPad, currentLine, rightPad, suffix))
                        currentLine = word
                    }
                }

                if (currentLine.isNotEmpty()) {
                    val (leftPad, rightPad) = getPadding(currentLine)
                    log.add(MessageUtils.joinedConvert(prefix, leftPad, currentLine, rightPad, suffix))
                }
            }

            lastKey = key
        }

        log.add(MessageUtils.convert(border))

        return log
    }
}
