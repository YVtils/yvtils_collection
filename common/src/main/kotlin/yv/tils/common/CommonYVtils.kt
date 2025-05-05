package yv.tils.common

import coroutine.CoroutineHandler
import data.Data
import data.Data.Companion.yvtilsVersion
import language.Language
import language.LanguageHandler
import logger.Logger
import message.MessageUtils
import net.kyori.adventure.text.Component
import yv.tils.common.config.ConfigFile
import yv.tils.common.language.LoadPlayerLanguage
import yv.tils.common.language.RegisterStrings
import yv.tils.common.listeners.PlayerJoin
import yv.tils.common.listeners.PlayerLocaleChange
import yv.tils.common.updateChecker.PluginVersion

class CommonYVtils : Data.YVtilsModule {
    companion object {
        const val MODULE_NAME = "common"
        const val MODULE_VERSION = "1.0.0"
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
    }

    override fun enablePlugin() {
        Language().loadLanguageFiles()

        Data.addModule("$MODULE_NAME v$MODULE_VERSION")

        val modules = Data.getModules(sorted = true)

        val data = mutableMapOf(
            1 to listOf(LanguageHandler.getMessage("plugin.action.start", params = mapOf("prefix" to Data.prefix))),
            2 to listOf(MessageUtils.convert(modules)),
        )
        val log = printLogWithSplits(data)
        log.forEach { Logger.log(Logger.Companion.Level.INFO, it) }

        registerListeners()
        registerCoroutines()

        loadConfigs()

        PluginVersion().launchVersionCheck()
    }

    override fun disablePlugin() {
        val data = mutableMapOf(
            1 to listOf(LanguageHandler.getMessage("plugin.action.stop", params = mapOf("prefix" to Data.prefix))),
        )

        val log = printLogWithSplits(data)
        log.forEach { Logger.log(Logger.Companion.Level.INFO, it) }
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()

        Logger.setDebugMode(
            ConfigFile.getValueAsBoolean("debug.active") ?: false,
            ConfigFile.getValueAsInt("debug.level") ?: 3
        )
    }

    private fun registerListeners() {
        val plugin = Data.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(PlayerLocaleChange(), plugin)
        pm.registerEvents(PlayerJoin(), plugin)
    }

    private fun registerCoroutines() {
        CoroutineHandler.launchTask(
            suspend { LoadPlayerLanguage().asyncCleanup() },
            "yvtils-language-cleanup",
            afterDelay = 5 * 1000 * 60,
        )
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