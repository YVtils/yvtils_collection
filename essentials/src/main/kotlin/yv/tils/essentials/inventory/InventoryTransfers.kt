package yv.tils.essentials.inventory

import org.bukkit.inventory.ItemStack

/** Plans against detached arrays; callers persist target changes before applying admin changes. */
internal object InventoryTransfers {
    data class CursorChange(val slot: ItemStack?, val cursor: ItemStack?)

    private fun amount(item: ItemStack?, count: Int): ItemStack? =
        item?.clone()?.apply { amount = count }?.takeIf { count > 0 }

    fun rightClick(slot: ItemStack?, cursor: ItemStack?): CursorChange = when {
        cursor == null -> {
            val take = ((slot?.amount ?: 0) + 1) / 2
            CursorChange(amount(slot, (slot?.amount ?: 0) - take), amount(slot, take))
        }
        slot == null -> CursorChange(amount(cursor, 1), amount(cursor, cursor.amount - 1))
        slot.isSimilar(cursor) -> {
            val place = if (slot.amount < slot.maxStackSize) 1 else 0
            CursorChange(amount(slot, slot.amount + place), amount(cursor, cursor.amount - place))
        }
        cursor.amount <= cursor.maxStackSize -> CursorChange(cursor.clone(), slot.clone())
        else -> CursorChange(slot.clone(), cursor.clone())
    }

    fun middleClick(slot: ItemStack?, cursor: ItemStack?, creative: Boolean): ItemStack? =
        if (creative && cursor == null) amount(slot, slot?.maxStackSize ?: 0) else cursor?.clone()

    fun move(source: Array<ItemStack?>, slot: Int, destination: Array<ItemStack?>, order: List<Int>) {
        val stack = source[slot] ?: return
        var remaining = stack.amount
        transfer@ for (empty in listOf(false, true)) for (index in order) {
            val current = destination[index]
            if ((current == null) != empty || current != null && !current.isSimilar(stack)) continue
            val amount = minOf(remaining, stack.maxStackSize - (current?.amount ?: 0)).coerceAtLeast(0)
            if (amount == 0) continue
            destination[index] = (current ?: stack).clone().apply { this.amount = (current?.amount ?: 0) + amount }
            remaining -= amount
            if (remaining == 0) break@transfer
        }
        source[slot] = stack.clone().apply { amount = remaining }.takeIf { remaining > 0 }
    }

    fun distribute(cursor: ItemStack, slots: List<Pair<Array<ItemStack?>, Int>>, single: Boolean): ItemStack? {
        val eligible = slots.filter { (items, slot) ->
            val item = items[slot]
            (item == null || item.isSimilar(cursor)) && (item?.amount ?: 0) < cursor.maxStackSize
        }
        if (eligible.isEmpty()) return cursor.clone()
        val perSlot = if (single) 1 else cursor.amount / eligible.size
        var remaining = cursor.amount
        for ((items, slot) in eligible) {
            val item = items[slot]
            val amount = minOf(perSlot, remaining, cursor.maxStackSize - (item?.amount ?: 0))
            if (amount == 0) continue
            items[slot] = (item ?: cursor).clone().apply { this.amount = (item?.amount ?: 0) + amount }
            remaining -= amount
        }
        return cursor.clone().apply { amount = remaining }.takeIf { remaining > 0 }
    }
}
