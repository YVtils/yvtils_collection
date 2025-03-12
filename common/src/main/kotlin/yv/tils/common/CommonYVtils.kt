package yv.tils.common

import data.Data
import message.MessageUtils
import data.Data.Companion.yvtilsVersion
import language.Language
import language.LanguageHandler
import logger.Logger
import net.kyori.adventure.text.Component
import yv.tils.common.language.RegisterStrings

class CommonYVtils {
    companion object {
        const val MODULENAME = "common"
        const val MODULEVERSION = "1.0.0"
    }

    init {
        RegisterStrings().registerStrings()
    }

    fun enablePlugin() {
        Language().loadLanguageFiles()

        Data.loadedModules.add("$MODULENAME v$MODULEVERSION")

        val data = mutableMapOf(
            1 to listOf(LanguageHandler.getMessage("plugin.action.start", params = mapOf("prefix" to Data.prefix))),
            2 to MessageUtils.convert(Data.loadedModules),
        )
        val log = printLogWithSplits(data)
        log.forEach { Logger.log(Logger.Companion.Level.INFO, it) }
    }

    fun disablePlugin() {
        val data = mutableMapOf(
            1 to listOf(LanguageHandler.getMessage("plugin.action.stop", params = mapOf("prefix" to Data.prefix))),
        )

        val log = printLogWithSplits(data)
        log.forEach { Logger.log(Logger.Companion.Level.INFO, it) }
    }

    private fun printLogWithSplits(data: MutableMap<Int, List<Component>>): MutableList<Component> {
        val lineLength = 40
        val log = mutableListOf<Component>()
        val border = "+".repeat(lineLength)

        log.add(MessageUtils.convert(border))

        val message = "YVtils Collection v$yvtilsVersion"
        val secondMessage = "https://yvtils.net"
        val prefix = "|"
        val suffix = "|"

        fun getPadding(content: String): Pair<String, String> {
            val availableSpace = lineLength - (prefix.length + suffix.length) - content.length
            val leftPadding = availableSpace / 2
            val rightPadding = availableSpace - leftPadding
            return " ".repeat(leftPadding) to " ".repeat(rightPadding)
        }


        val (paddingLeft, paddingRight) = getPadding(message)
        log.add(MessageUtils.convert("$prefix$paddingLeft$message$paddingRight$suffix"))
        val (secondPaddingLeft, secondPaddingRight) = getPadding(secondMessage)
        log.add(MessageUtils.convert("$prefix$secondPaddingLeft$secondMessage$secondPaddingRight$suffix"))

        log.add(MessageUtils.convert(border))

        var lastKey: Int? = null
        data.toSortedMap().forEach { (key, messages) ->
            if (lastKey != null && key != lastKey) {
                log.add(MessageUtils.convert("|--------------------------------------|"))
            }

            messages.forEach { message ->
                val tempMessage = MessageUtils.strip(message)

                if (tempMessage.length > lineLength) {
                    log.add(MessageUtils.joinedConvert(prefix, tempMessage.substring(0, lineLength - 2), suffix))
                    log.add(MessageUtils.joinedConvert(prefix, tempMessage.substring(lineLength - 2), suffix))
                    return@forEach
                }

                val (prefixPadding, suffixPadding) = getPadding(tempMessage)
                log.add(MessageUtils.joinedConvert(prefix, prefixPadding, tempMessage, suffixPadding, suffix))
            }

            lastKey = key
        }

        log.add(MessageUtils.convert(border))

        return log
    }
}