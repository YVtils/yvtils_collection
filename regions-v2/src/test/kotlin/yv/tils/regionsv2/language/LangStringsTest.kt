package yv.tils.regionsv2.language

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LangStringsTest {
    @Test
    fun `language keys are descriptive unique and both languages have matching placeholders`() {
        val entries = LangStrings.entries
        assertEquals(entries.size, entries.map { it.key }.toSet().size)
        val parameter = Regex("<([a-z][A-Za-z0-9_]*)>")
        val formatting = setOf("prefix", "red", "green", "gray", "white", "aqua", "newline")
        for (entry in entries) {
            assertFalse(entry.key.matches(Regex(".*[a-f0-9]{32}$")), entry.key)
            assertTrue(entry.english.isNotBlank(), entry.key)
            assertTrue(entry.german.isNotBlank(), entry.key)
            fun parameters(text: String) =
                parameter.findAll(text).map { it.groupValues[1] }.filterNot { it in formatting }.toSet()
            assertEquals(parameters(entry.english), parameters(entry.german), entry.key)
            assertFalse(entry.english.contains(Regex("\\{\\d+}")), entry.key)
        }
    }
}
