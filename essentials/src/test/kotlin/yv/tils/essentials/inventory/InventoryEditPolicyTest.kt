package yv.tils.essentials.inventory

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class InventoryEditPolicyTest {
    @Test
    fun `stale occupied slot is not equal to an empty displayed slot`() {
        val item = mock(ItemStack::class.java)
        `when`(item.type).thenReturn(Material.DIAMOND)
        `when`(item.amount).thenReturn(1)
        assertFalse(InventoryEditPolicy.same(null, item))
        assertFalse(InventoryEditPolicy.same(item, null))
        assertTrue(InventoryEditPolicy.same(item, item))
    }

    @Test
    fun `air and null are interchangeable empty slots`() {
        val air = mock(ItemStack::class.java)
        `when`(air.type).thenReturn(Material.AIR)
        assertTrue(InventoryEditPolicy.same(null, air))
        assertTrue(InventoryEditPolicy.same(air, null))
    }
}
