package yv.tils.essentials.inventory

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class InventoryTransfersTest {
    private fun stack(amount: Int, material: Material = Material.DIAMOND): ItemStack {
        val item = mock(ItemStack::class.java)
        var count = amount
        `when`(item.type).thenReturn(material)
        `when`(item.maxStackSize).thenReturn(64)
        `when`(item.amount).thenAnswer { count }
        doAnswer { count = it.getArgument(0); null }.`when`(item).amount = anyInt()
        `when`(item.clone()).thenAnswer { stack(count, material) }
        `when`(item.isSimilar(any(ItemStack::class.java))).thenAnswer { (it.getArgument(0) as ItemStack).type == material }
        return item
    }

    @Test
    fun `right click picks up the larger half without mutating the original stack`() {
        val original = stack(5)
        val change = InventoryTransfers.rightClick(original, null)
        assertEquals(2, change.slot?.amount)
        assertEquals(3, change.cursor?.amount)
        assertEquals(5, original.amount)
        val single = InventoryTransfers.rightClick(stack(1), null)
        assertNull(single.slot)
        assertEquals(1, single.cursor?.amount)
    }

    @Test
    fun `right click places one item and respects full stacks`() {
        val empty = InventoryTransfers.rightClick(null, stack(3))
        assertEquals(1, empty.slot?.amount)
        assertEquals(2, empty.cursor?.amount)
        val merge = InventoryTransfers.rightClick(stack(63), stack(1))
        assertEquals(64, merge.slot?.amount)
        assertNull(merge.cursor)
        val full = InventoryTransfers.rightClick(stack(64), stack(4))
        assertEquals(64, full.slot?.amount)
        assertEquals(4, full.cursor?.amount)
    }

    @Test
    fun `right click swaps incompatible items`() {
        val change = InventoryTransfers.rightClick(stack(8, Material.STONE), stack(3))
        assertEquals(Material.DIAMOND, change.slot?.type)
        assertEquals(3, change.slot?.amount)
        assertEquals(Material.STONE, change.cursor?.type)
        assertEquals(8, change.cursor?.amount)
    }

    @Test
    fun `middle click only copies in creative onto an empty cursor`() {
        val original = stack(3)
        assertNull(InventoryTransfers.middleClick(original, null, false))
        assertNull(InventoryTransfers.middleClick(null, null, true))
        assertEquals(64, InventoryTransfers.middleClick(original, null, true)?.amount)
        assertEquals(3, original.amount)
        val occupied = InventoryTransfers.middleClick(original, stack(7, Material.STONE), true)
        assertEquals(Material.STONE, occupied?.type)
        assertEquals(7, occupied?.amount)
    }

    @Test
    fun `shift merges existing stacks before filling empty slots and leaves overflow`() {
        val original = stack(20)
        val source = arrayOf<ItemStack?>(original)
        val destination = arrayOf<ItemStack?>(null, stack(60))
        InventoryTransfers.move(source, 0, destination, listOf(0, 1))
        assertNull(source[0])
        assertEquals(16, destination[0]?.amount)
        assertEquals(64, destination[1]?.amount)
        assertEquals(20, original.amount)

        val overflow = arrayOf<ItemStack?>(stack(10))
        InventoryTransfers.move(overflow, 0, destination, listOf(1))
        assertEquals(10, overflow[0]?.amount)
    }

    @Test
    fun `drag distributes evenly across compatible slots and preserves remainder`() {
        val cursor = stack(11)
        val target = arrayOf<ItemStack?>(null, stack(63), stack(1, Material.STONE))
        val own = arrayOfNulls<ItemStack>(1)
        val remainder = InventoryTransfers.distribute(cursor, listOf(target to 0, target to 1, target to 2, own to 0), false)
        assertEquals(3, target[0]?.amount)
        assertEquals(64, target[1]?.amount)
        assertEquals(Material.STONE, target[2]?.type)
        assertEquals(3, own[0]?.amount)
        assertEquals(4, remainder?.amount)
        assertEquals(11, cursor.amount)
    }

    @Test
    fun `right drag places one per slot without exceeding cursor amount`() {
        val target = arrayOfNulls<ItemStack>(3)
        val remainder = InventoryTransfers.distribute(stack(2), target.indices.map { target to it }, true)
        assertEquals(listOf(1, 1, null), target.map { it?.amount })
        assertNull(remainder)
    }
}
