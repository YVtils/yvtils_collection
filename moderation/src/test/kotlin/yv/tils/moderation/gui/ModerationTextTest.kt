package yv.tils.moderation.gui

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import net.kyori.adventure.text.Component

class ModerationTextTest {
    @Test
    fun `reason tags are parsed and white survives lore styling`() {
        val reason = ModerationText.reason("<white>No reason was defined!")
        val lore = ModerationText.lore(reason)
        assertEquals("No reason was defined!", PlainTextComponentSerializer.plainText().serialize(lore))
        fun hasWhite(component: Component): Boolean =
            component.color() == NamedTextColor.WHITE || component.children().any(::hasWhite)
        assertTrue(hasWhite(lore))
    }

    @Test
    fun `moderation reason cannot introduce interactive actions`() {
        val component = ModerationText.reason("<click:run_command:'/op someone'><white>Reason</click>")
        fun hasClick(value: Component): Boolean =
            value.clickEvent() != null || value.children().any(::hasClick)
        assertFalse(hasClick(component))
    }
}
