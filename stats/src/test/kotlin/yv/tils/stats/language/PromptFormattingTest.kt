/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.stats.language

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PromptFormattingTest {
    private val plain = PlainTextComponentSerializer.plainText()

    @Test
    fun `older saved translations gain sentence breaks without changing their text or URL`() {
        val message = Component.text("Telemetry goes to https://api.yvtils.net/stats. Nothing is sent before acceptance. Choose below.")
        val formatted = RegisterStrings.formatOptInPrompt(message)
        assertEquals(
            "Telemetry goes to https://api.yvtils.net/stats.\nNothing is sent before acceptance.\nChoose below.",
            plain.serialize(formatted),
        )
    }

    @Test
    fun `existing paragraph breaks and colors are preserved`() {
        val message = Component.text("Privacy.\n\nNo player identities. Settings can be changed in game.", NamedTextColor.WHITE)
        val formatted = RegisterStrings.formatOptInPrompt(message)
        assertEquals("Privacy.\n\nNo player identities.\nSettings can be changed in game.", plain.serialize(formatted))
        assertEquals(NamedTextColor.WHITE, formatted.color())
    }
}
