package yv.tils.essentials.inventory

import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.ReferencingInventory
import xyz.xenondevs.invui.window.Window
import java.util.UUID

/** One locked target, one viewer; item moves are committed before either inventory changes. */
class InventorySession(
    val target: UUID,
    val viewer: UUID,
    val kind: InventoryKind,
    val offline: Boolean,
    var expected: Array<ItemStack?>,
    var fingerprint: ByteArray? = null,
) {
    lateinit var window: Window
    lateinit var inventory: VirtualInventory
    lateinit var adminInventory: ReferencingInventory
    var dragPending = false
    var lastDrag: Any? = null
    @Volatile
    var locked = false
}
