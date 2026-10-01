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

package yv.tils.regions.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import yv.tils.regions.configs.RegionsConfigState

class ClaimPricingTest {
    private val config = RegionsConfigState()

    @Test
    fun `two chunks free then one diamond for each additional size tier`() {
        assertEquals(0L, ClaimPricing.price(1, 1, config))
        assertEquals(0L, ClaimPricing.price(32, 32, config))
        assertEquals(1L, ClaimPricing.price(33, 33, config))
        assertEquals(1L, ClaimPricing.price(48, 48, config))
        assertEquals(2L, ClaimPricing.price(49, 49, config))
        assertEquals(14L, ClaimPricing.price(256, 256, config))
    }

    @Test
    fun `rectangles use longer side and footprint does not depend on chunk alignment`() {
        assertEquals(1L, ClaimPricing.price(48, 1, config))
        assertEquals(1L, ClaimPricing.price(1, 48, config))
        assertEquals(0L, ClaimPricing.price(32, 2, config))
    }

    @Test
    fun `custom tiers support no free area and free currency`() {
        assertEquals(9L, ClaimPricing.price(48, 48, config.copy(freeClaimChunks = 0, diamondsPerChunk = 3)))
        assertEquals(0L, ClaimPricing.price(256, 256, config.copy(diamondsPerChunk = 0)))
        assertThrows(IllegalArgumentException::class.java) { ClaimPricing.price(0, 32, config) }
        assertThrows(IllegalArgumentException::class.java) {
            ClaimPricing.price(
                32,
                32,
                config.copy(freeClaimChunks = -1)
            )
        }
    }

    @Test
    fun `expansions only pay outstanding amount and shrinking never refunds`() {
        val credit = ClaimPricing.price(48, 48, config)
        assertEquals(1L, ClaimPricing.due(ClaimPricing.price(64, 64, config), credit))
        assertEquals(0L, ClaimPricing.due(ClaimPricing.price(16, 16, config), credit))
        assertEquals(0L, ClaimPricing.due(ClaimPricing.price(48, 48, config), credit))
    }

    @Test
    fun `merge credits both source claims and closes free-claim merging loophole`() {
        val freeCredit = ClaimPricing.price(32, 32, config) * 2
        assertEquals(2L, ClaimPricing.due(ClaimPricing.price(64, 32, config), freeCredit))
        val paidCredit = ClaimPricing.price(48, 48, config) * 2
        assertEquals(2L, ClaimPricing.due(ClaimPricing.price(96, 48, config), paidCredit))
    }

    @Test
    fun `large configured rates do not wrap integer totals`() {
        assertEquals(30_064_771_058L, ClaimPricing.price(256, 256, config.copy(diamondsPerChunk = Int.MAX_VALUE)))
    }
}
