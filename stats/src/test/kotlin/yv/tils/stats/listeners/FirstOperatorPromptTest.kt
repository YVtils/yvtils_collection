/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.stats.listeners

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import yv.tils.stats.StatsYVtils

class FirstOperatorPromptTest {
    @Test
    fun `only the first operator is prompted while undecided`() {
        val prompt = FirstOperatorPrompt()
        assertFalse(prompt.claim(operator = false, undecided = true))
        assertTrue(prompt.claim(operator = true, undecided = true))
        assertFalse(prompt.claim(operator = true, undecided = true))
    }

    @Test
    fun `recorded acceptance and rejection are not nagged on joins`() {
        assertFalse(FirstOperatorPrompt().claim(operator = true, undecided = false))
    }

    @Test
    fun `a new boot can prompt again if no decision was recorded`() {
        assertTrue(FirstOperatorPrompt().claim(operator = true, undecided = true))
        assertTrue(FirstOperatorPrompt().claim(operator = true, undecided = true))
    }

    @Test
    fun `all boot statuses fit a single informational line`() {
        val module = StatsYVtils()
        for (decision in listOf(null, false, true)) {
            val message = module.bootMessage(decision)
            assertEquals(1, message.lines().size)
            assertTrue(message.contains("/yvtilsstats"))
            assertTrue(message.length < 160)
        }
    }
}
