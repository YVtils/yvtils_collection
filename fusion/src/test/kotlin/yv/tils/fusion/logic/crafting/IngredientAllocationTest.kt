package yv.tils.fusion.logic.crafting

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IngredientAllocationTest {
    @Test
    fun `alternatives cannot consume the same items twice`() {
        val plan = IngredientAllocation.plan(intArrayOf(4), intArrayOf(3, 3)) { _, _ -> true }
        assertFalse(plan.complete)
        assertEquals(4, plan.consumed.sum())
        assertEquals(2, plan.missing.sum())
    }

    @Test
    fun `broad ingredient allocation is rerouted to satisfy a specific ingredient`() {
        val plan =
            IngredientAllocation.plan(intArrayOf(2, 2), intArrayOf(2, 2)) { slot, demand -> demand == 0 || slot == 0 }
        assertTrue(plan.complete)
        assertArrayEquals(intArrayOf(2, 2), plan.consumed)
    }

    @Test
    fun `unmatched items remain untouched`() {
        val plan = IngredientAllocation.plan(intArrayOf(64, 5), intArrayOf(3)) { slot, _ -> slot == 1 }
        assertTrue(plan.complete)
        assertArrayEquals(intArrayOf(0, 3), plan.consumed)
    }

    @Test
    fun `small overlapping inventories agree with exhaustive allocation`() {
        fun possible(available: IntArray, required: IntArray, mask: Int): Boolean {
            val demand = required.indexOfFirst { it > 0 }
            if (demand == -1) return true
            return available.indices.any { slot ->
                if (available[slot] == 0 || mask and (1 shl (slot * required.size + demand)) == 0) false
                else {
                    val remaining = required.clone().apply { this[demand]-- }
                    val inventory = available.clone().apply { this[slot]-- }
                    possible(inventory, remaining, mask)
                }
            }
        }
        for (mask in 0..15) for (a in 0..3) for (b in 0..3) for (x in 0..3) for (y in 0..3) {
            val plan = IngredientAllocation.plan(intArrayOf(a, b), intArrayOf(x, y)) { slot, demand ->
                mask and (1 shl (slot * 2 + demand)) != 0
            }
            assertEquals(
                possible(intArrayOf(a, b), intArrayOf(x, y), mask), plan.complete,
                "mask=$mask inventory=$a,$b demand=$x,$y"
            )
            assertTrue(plan.consumed[0] in 0..a && plan.consumed[1] in 0..b)
            assertEquals(plan.consumed.sum(), x + y - plan.missing.sum())
        }
    }
}
