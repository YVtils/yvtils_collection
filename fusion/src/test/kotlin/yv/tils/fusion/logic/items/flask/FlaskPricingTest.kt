package yv.tils.fusion.logic.items.flask

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FlaskPricingTest {
    @Test
    fun `saturation can be nourished at full hunger and respects its cap`() {
        assertEquals(FlaskPricing.Saturation(4f, 32), FlaskPricing.saturation(20, 10f, 100, 8, 4))
        assertEquals(FlaskPricing.Saturation(0.5f, 4), FlaskPricing.saturation(20, 19.5f, 100, 8, 4))
        assertEquals(FlaskPricing.Saturation(0f, 0), FlaskPricing.saturation(20, 20f, 100, 8, 4))
    }

    @Test
    fun `saturation spending respects remaining XP and can be disabled`() {
        assertEquals(FlaskPricing.Saturation(2f, 16), FlaskPricing.saturation(20, 0f, 20, 8, 4))
        assertEquals(FlaskPricing.Saturation(0f, 0), FlaskPricing.saturation(20, 0f, 0, 8, 4))
        assertEquals(FlaskPricing.Saturation(0f, 0), FlaskPricing.saturation(20, 0f, 100, 8, 0))
    }
    @Test
    fun `charges only for hunger actually restored`() {
        assertEquals(FlaskPricing.Feeding(2, 16), FlaskPricing.quote(18, 100, 8, 4))
        assertEquals(FlaskPricing.Feeding(4, 32), FlaskPricing.quote(10, 100, 8, 4))
    }

    @Test
    fun `partial payment never overspends raw points`() {
        assertEquals(FlaskPricing.Feeding(3, 24), FlaskPricing.quote(10, 31, 8, 4))
        assertEquals(FlaskPricing.Feeding(0, 0), FlaskPricing.quote(10, 7, 8, 4))
    }

    @Test
    fun `full hunger and zero XP cost nothing`() {
        assertEquals(FlaskPricing.Feeding(0, 0), FlaskPricing.quote(20, 100, 8, 4))
        assertEquals(FlaskPricing.Feeding(0, 0), FlaskPricing.quote(0, 0, 8, 4))
    }

    @Test
    fun `every valid quote respects hunger XP and configured limits`() {
        for (hunger in 0..20) for (points in 0..100) for (cost in 1..10) {
            val quote = FlaskPricing.quote(hunger, points, cost, 4)
            assertTrue(quote.food in 0..minOf(4, 20 - hunger))
            assertTrue(quote.cost in 0..points)
            assertEquals(quote.food * cost, quote.cost)
        }
    }

    @Test
    fun `invalid rates rejected`() {
        assertThrows(IllegalArgumentException::class.java) { FlaskPricing.quote(10, 10, 0, 4) }
    }
}
