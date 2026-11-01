package yv.tils.essentials.logic

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ChatFormattingTest {
    @Test
    fun `dark gray status name does not color or decorate the chat body`() {
        val name = MiniMessage.miniMessage().deserialize("<dark_gray>[<green>Online<dark_gray>] <white>Alice")
        val rendered = ChatFormatting.render(name, Component.text("Hello"))
        assertEquals("[Online] Alice: Hello", PlainTextComponentSerializer.plainText().serialize(rendered))
        assertTrue(glyphs(rendered).takeLast(7).all { it.color() == NamedTextColor.WHITE })

        val decorated = ChatFormatting.render(name.decorate(TextDecoration.BOLD), Component.text("Hello"))
        assertTrue(glyphs(decorated).takeLast(7).none {
            it.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE
        })
    }

    @Test
    fun `explicit chat colors are retained and name components are not reparsed`() {
        val name = Component.text("<dark_gray>Alice", NamedTextColor.DARK_GRAY)
        val body = Component.text("Hello", NamedTextColor.RED).decorate(TextDecoration.ITALIC)
        val rendered = ChatFormatting.render(name, body)
        assertEquals("<dark_gray>Alice: Hello", PlainTextComponentSerializer.plainText().serialize(rendered))
        assertEquals(name, rendered.children().first())
        assertTrue(glyphs(rendered).takeLast(5).all {
            it.color() == NamedTextColor.RED && it.decoration(TextDecoration.ITALIC) == TextDecoration.State.TRUE
        })
    }

    private fun glyphs(component: Component, inherited: Style = Style.empty()): List<Style> {
        val style = component.style().merge(inherited, Style.Merge.Strategy.IF_ABSENT_ON_TARGET)
        val own = if (component is TextComponent) component.content().map { style } else emptyList()
        return own + component.children().flatMap { glyphs(it, style) }
    }
}
