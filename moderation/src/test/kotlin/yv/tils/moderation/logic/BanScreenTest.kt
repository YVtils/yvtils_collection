package yv.tils.moderation.logic

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BanScreenTest {
    @Test
    fun `ban screen substitutes formatted reasons and literal issuer without leftover placeholders`() {
        val result = BanScreen.render(
            "<red>Banned<newline><white><reason><newline><issuer><newline><expires>",
            "<white>No reason was defined!", "<red>Admin", "2030-01-01 UTC"
        )
        val plain = PlainTextComponentSerializer.plainText().serialize(result)
        assertEquals("Banned\nNo reason was defined!\n<red>Admin\n2030-01-01 UTC", plain)
        assertFalse(plain.contains("<reason>"))
    }
}
