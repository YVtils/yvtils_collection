package yv.tils.essentials.inventory

enum class InventoryKind(val command: String, val slots: Int) {
    PLAYER("invsee", 41),
    ENDER_CHEST("ecsee", 27);

    val permission get() = "yvtils.essentials.command.$command"
    val editPermission get() = "$permission.edit"

    val viewSize get() = if (this == PLAYER) 54 else 36

    /** GUI decorations never resolve to a writable target slot. */
    fun targetSlot(viewSlot: Int): Int? = when (this) {
        PLAYER -> when (viewSlot) {
            in 2..5 -> 41 - viewSlot // Helmet, chestplate, leggings, boots.
            7 -> 40 // Offhand.
            in 9..35 -> viewSlot // Main storage, in native inventory order.
            in 45..53 -> viewSlot - 45 // Hotbar.
            else -> null
        }
        ENDER_CHEST -> if (viewSlot in 9..35) viewSlot - 9 else null
    }
}
