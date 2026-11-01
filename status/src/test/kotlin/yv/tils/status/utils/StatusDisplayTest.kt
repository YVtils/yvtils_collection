/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.status.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StatusDisplayTest {
    private val template = "<dark_gray>[<white><status><dark_gray>] |<white> <playerName>"
    private val miniMessage = MiniMessage.miniMessage()
    private val plain = PlainTextComponentSerializer.plainText()

    @Test
    fun `player formatting cannot change the name or fixed separators`() {
        val baseline = glyphs(StatusDisplay.render(template, "", "Alice"))
        val statuses = listOf(
            "<bold>Busy",
            "<italic><underlined><strikethrough><obfuscated>Busy",
            "<red>Busy",
            "<gradient:red:blue>Busy",
            "<rainbow>Busy",
            "</white></dark_gray><reset><bold><red>Busy",
            "<click:run_command:'/say injected'><hover:show_text:'test'><insertion:test>Busy",
            "<font:minecraft:uniform>Busy",
        )

        for (status in statuses) {
            val rendered = StatusDisplay.render(template, status, "Alice")
            val statusLength = plain.serialize(miniMessage.deserialize(status)).length
            val renderedGlyphs = glyphs(rendered)
            assertEquals(baseline, renderedGlyphs.take(1) + renderedGlyphs.drop(1 + statusLength), status)
        }
    }

    @Test
    fun `status retains its own colors and decorations`() {
        val rendered = StatusDisplay.render(template, "<green><bold>Busy", "Alice")
        assertEquals("[Busy] | Alice", plain.serialize(rendered))
        val statusGlyphs = glyphs(rendered).subList(1, 5)
        assertTrue(statusGlyphs.all { it.second.color() == NamedTextColor.GREEN })
        assertTrue(statusGlyphs.all { it.second.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE })
    }

    @Test
    fun `status cannot expand template placeholders or reparse escaped tags`() {
        val status = "<playerName><prefix>\\<red>Busy"
        val rendered = StatusDisplay.render(template, status, "Alice", "<gold>Server")
        assertEquals("[<playerName><prefix><red>Busy] | Alice", plain.serialize(rendered))
        assertTrue(glyphs(rendered).none { it.second.color() == NamedTextColor.RED })
    }

    @Test
    fun `custom template styling remains server controlled`() {
        val custom = "<gold><bold><playerName> [<status>] tail"
        val rendered = StatusDisplay.render(custom, "<reset><red>Busy", "Alice")
        val baseline = glyphs(StatusDisplay.render(custom, "", "Alice"))
        val renderedGlyphs = glyphs(rendered)
        assertEquals(baseline, renderedGlyphs.take(7) + renderedGlyphs.drop(11))
    }

    @Test
    fun `team prefix does not inherit status formatting`() {
        val rendered = StatusDisplay.render(template, "<obfuscated><bold>Busy", "")
        val renderedGlyphs = glyphs(rendered)
        val baseline = glyphs(StatusDisplay.render(template, "", ""))
        assertEquals(baseline, renderedGlyphs.take(1) + renderedGlyphs.drop(5))
    }

    @Test
    fun `confirmation and reconnect messages preserve components without reparsing`() {
        val display = StatusDisplay.render(template, "<bold>\\<red>Busy", "Alice")
        val message = StatusDisplay.message("<prefix> <status><yellow> done", display, "<gold>Server")
        assertEquals("Server [<red>Busy] | Alice done", plain.serialize(message))
        val renderedGlyphs = glyphs(message)
        assertEquals(glyphs(display), renderedGlyphs.subList(7, 7 + plain.serialize(display).length))
        assertTrue(renderedGlyphs.takeLast(5).all {
            it.second.color() == NamedTextColor.YELLOW &&
                it.second.decoration(TextDecoration.BOLD) != TextDecoration.State.TRUE
        })
    }

    /** Compare effective styles, including styles inherited from parent components. */
    private fun glyphs(component: Component, inherited: Style = Style.empty()): List<Pair<Char, Style>> {
        val style = component.style().merge(inherited, Style.Merge.Strategy.IF_ABSENT_ON_TARGET)
        val own = if (component is TextComponent) component.content().map { it to style } else emptyList()
        return own + component.children().flatMap { glyphs(it, style) }
    }
}
