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

package yv.tils.utils.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import yv.tils.utils.modules.Core
import yv.tils.utils.logger.DEBUG_LEVEL
import yv.tils.utils.logger.Logger

class MessageUtils {
    companion object {
        fun convert(text: String?): Component {
            Logger.debug("Converting text to Component: $text", DEBUG_LEVEL.SPAM)
            return text?.let { MiniMessage.miniMessage().deserialize(it) } ?: Component.empty()
        }

        fun convert(text: Component?): String {
            Logger.debug("Converting Component to text: $text", DEBUG_LEVEL.SPAM)
            return text?.let { MiniMessage.miniMessage().serialize(it) } ?: ""
        }

        fun convert(textList: List<String>): List<Component> {
            Logger.debug("Converting list of text to Component: $textList", DEBUG_LEVEL.SPAM)
            return textList.map { convert(it) }
        }

        fun convertChatMessage(text: Component): Component {
            Logger.debug("Converting chat message to Component: $text", DEBUG_LEVEL.SPAM)
            return convert(strip(text))
        }

        fun strip(text: String?): String {
            Logger.debug("Stripping text: $text", DEBUG_LEVEL.SPAM)
            return text?.let { PlainTextComponentSerializer.plainText().serialize(convert(text)) } ?: ""
        }

        fun strip(text: Component?): String {
            Logger.debug("Stripping Component: $text", DEBUG_LEVEL.SPAM)
            return text?.let { PlainTextComponentSerializer.plainText().serialize(text) } ?: ""
        }

        fun stripChatMessage(text: Component): String {
            Logger.debug("Stripping chat message: $text", DEBUG_LEVEL.SPAM)
            return strip(strip(text))
        }

        fun handleLore(text: String): List<Component> {
            Logger.debug("Handling lore: $text", DEBUG_LEVEL.SPAM)
            val lore = mutableListOf<Component>()
            val mm = MiniMessage.miniMessage()

            // Variable to store the last color or formatting tag detected
            var lastFormat: String? = null

            // List of known color and style tags from MiniMessage
            val knownTags = listOf(
                "<red>", "<green>", "<blue>", "<yellow>", "<gold>", "<white>", "<black>", "<gray>",
                "<aqua>", "<dark_red>", "<dark_green>", "<dark_blue>", "<dark_aqua>", "<dark_purple>",
                "<dark_gray>", "<light_purple>", "<bold>", "<italic>", "<underlined>",
                "<strikethrough>", "<obfuscated>"
            )

            val processLines: (String) -> Unit = { text ->
                val lines = text.split(Regex("<newline>|<br>|\\n"))
                for (line in lines) {
                    if (line.trim().isEmpty()) continue

                    val formattedLine = if (lastFormat != null && !knownTags.any { line.contains(it) }) {
                        "$lastFormat$line"
                    } else {
                        line
                    }

                    val component = mm.deserialize(formattedLine)
                    lore.add(component)

                    knownTags.firstOrNull { tag -> formattedLine.contains(tag) }?.let {
                        lastFormat = it
                    }
                }
            }

            processLines(text)
            return lore
        }

        fun handleLore(text: Component): List<Component> {
            Logger.debug("Handling component lore: $text", DEBUG_LEVEL.SPAM)
            return handleLore(convert(text))
        }

        fun joinedConvert(vararg text: String): Component {
            Logger.debug("Joining and converting text: ${text.joinToString(";")}", DEBUG_LEVEL.SPAM)
            val joinedText = text.joinToString("")
            return convert(joinedText)
        }

        fun replacer(inPut: Component, replace: Map<String, Any>): Component {
            Logger.debug("InPut: $inPut", DEBUG_LEVEL.SPAM)
            Logger.debug("Replace: $replace", DEBUG_LEVEL.SPAM)

            val text = convert(inPut)
            val outPut = replacerLogic(text, replace)

            Logger.debug("Output: $outPut", DEBUG_LEVEL.SPAM)

            return outPut
        }

        fun replacer(inPut: String, replace: Map<String, Any>): Component {
            Logger.debug("InPut: $inPut", DEBUG_LEVEL.SPAM)
            Logger.debug("Replace: $replace", DEBUG_LEVEL.SPAM)

            val outPut = replacerLogic(inPut, replace)

            Logger.debug("Output: $outPut", DEBUG_LEVEL.SPAM)

            return outPut
        }

        private fun replacerLogic(text: String, replace: Map<String, Any>): Component {
            var text = text.replace("\\<", "<")

            for (i in replace.keys) {
                val oldString = "<$i>"
                text = text.replace(oldString, replace[i].toString())
            }

            text = text.replace("<prefix>", Core.prefix)

            if (text.startsWith(" ")) {
                text = text.replaceFirst(" ", "")
            }

            val outPut = convert(text)

            return outPut
        }
    }
}
