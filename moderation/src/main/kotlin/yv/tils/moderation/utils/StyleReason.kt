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

package yv.tils.moderation.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import yv.tils.config.language.LanguageHandler

class StyleReason {
    companion object {
        private const val DEFAULT_WIDTH = 46
        private const val MIN_WIDTH = 24
        private val WHITESPACE = "\\s+".toRegex()

        /**
         * Builds a simple ASCII styled box with a highlighted title and wrapped body content.
         */
        fun styleReason(
            reason: String,
            title: String = "Moderation Notice",
            innerWidth: Int = DEFAULT_WIDTH
        ): Component {
            val contentWidth = innerWidth.coerceAtLeast(MIN_WIDTH).coerceAtLeast(title.length)
            val normalized = reason.replace("\r", "")
            val wrappedLines = when {
                normalized.isBlank() -> listOf(LanguageHandler.getRawMessage("moderation.placeholder.reason.none"))
                else -> normalized
                    .split('\n')
                    .flatMap { wrapLine(it, contentWidth) }
                    .ifEmpty { listOf(LanguageHandler.getRawMessage("moderation.placeholder.reason.none")) }
            }

            val horizontalBorder = "+${"-".repeat(contentWidth + 2)}+"
            val separator = "| ${"-".repeat(contentWidth)} |"

            val segments = mutableListOf<Component>()
            segments += Component.text(horizontalBorder, NamedTextColor.DARK_GRAY)
            segments += boxedLine(center(title, contentWidth), contentWidth, NamedTextColor.RED, bold = true)
            segments += Component.text(separator, NamedTextColor.DARK_GRAY)
            wrappedLines.forEach { line ->
                segments += boxedLine(line, contentWidth, NamedTextColor.WHITE)
            }
            segments += Component.text(horizontalBorder, NamedTextColor.DARK_GRAY)

            return Component.join(JoinConfiguration.newlines(), segments)
        }

        private fun boxedLine(
            text: String,
            width: Int,
            color: NamedTextColor,
            bold: Boolean = false
        ): Component {
            val padded = text.padEnd(width, ' ')
            val body = Component.text(padded, color).decoration(TextDecoration.BOLD, bold)
            return Component.text("| ", NamedTextColor.DARK_GRAY)
                .append(body)
                .append(Component.text(" |", NamedTextColor.DARK_GRAY))
        }

        private fun center(text: String, width: Int): String {
            if (text.length >= width) return text
            val padding = width - text.length
            val left = padding / 2
            val right = padding - left
            return buildString {
                repeat(left) { append(' ') }
                append(text)
                repeat(right) { append(' ') }
            }
        }

        private fun wrapLine(line: String, width: Int): List<String> {
            if (line.isBlank()) return listOf("")
            val result = mutableListOf<String>()
            var current = StringBuilder()
            line.trim().split(WHITESPACE).filter { it.isNotEmpty() }.forEach { word ->
                when {
                    word.length > width -> {
                        if (current.isNotEmpty()) {
                            result += current.toString()
                            current = StringBuilder()
                        }
                        val chunks = word.chunked(width)
                        chunks.dropLast(1).forEach { result += it }
                        current.append(chunks.last())
                    }
                    current.isEmpty() -> current.append(word)
                    current.length + 1 + word.length <= width -> current.append(' ').append(word)
                    else -> {
                        result += current.toString()
                        current = StringBuilder(word)
                    }
                }
            }

            if (current.isNotEmpty()) {
                result += current.toString()
            }

            return result.ifEmpty { listOf("") }
        }
    }
}