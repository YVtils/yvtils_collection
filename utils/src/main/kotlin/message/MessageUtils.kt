package message

import data.Data
import logger.Logger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

class MessageUtils {
    companion object {
        fun convert(text: String?): Component {
            Logger.debug("Converting text to Component: $text")
            return text?.let { MiniMessage.miniMessage().deserialize(it) } ?: Component.empty()
        }

        fun convert(text: Component?): String {
            Logger.debug("Converting Component to text: $text")
            return text?.let { MiniMessage.miniMessage().serialize(it) } ?: ""
        }

        fun convert(textList: List<String>): List<Component> {
            Logger.debug("Converting list of text to Component: $textList")
            return textList.map { convert(it) }
        }

        fun convertChatMessage(text: Component): Component {
            Logger.debug("Converting chat message to Component: $text")
            return convert(strip(text))
        }

        fun strip(text: String): String {
            Logger.debug("Stripping text: $text")
            return PlainTextComponentSerializer.plainText().serialize(convert(text))
        }

        fun strip(text: Component): String {
            Logger.debug("Stripping Component: $text")
            return PlainTextComponentSerializer.plainText().serialize(text)
        }

        fun stripChatMessage(text: Component): String {
            Logger.debug("Stripping chat message: $text")
            return strip(strip(text))
        }

        fun handleLore(text: String): List<Component> {
            Logger.debug("Handling lore: $text")
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
            Logger.debug("Handling component lore: $text")
            return handleLore(convert(text))
        }

        fun joinedConvert(vararg text: String): Component {
            Logger.debug("Joining and converting text: ${text.joinToString(";")}")
            val joinedText = text.joinToString("")
            return convert(joinedText)
        }

        fun replacer(inPut: Component, replace: Map<String, Any>): Component {
            Logger.debug("InPut: $inPut")
            Logger.debug("Replace: $replace")

            val text = convert(inPut)
            val outPut = replacerLogic(text, replace)

            Logger.debug("Output: $outPut")

            return outPut
        }

        fun replacer(inPut: String, replace: Map<String, Any>): Component {
            Logger.debug("InPut: $inPut")
            Logger.debug("Replace: $replace")

            val outPut = replacerLogic(inPut, replace)

            Logger.debug("Output: $outPut")

            return outPut
        }

        private fun replacerLogic(text: String, replace: Map<String, Any>): Component {
            var text = text.replace("\\<", "<")

            for (i in replace.keys) {
                val oldString = "<$i>"
                text = text.replace(oldString, replace[i].toString())
            }

            text = text.replace("<prefix>", Data.prefix)

            if (text.startsWith(" ")) {
                text = text.replaceFirst(" ", "")
            }

            val outPut = convert(text)

            return outPut
        }
    }
}
