/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.gui

import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.Material
import xyz.xenondevs.invui.item.ItemWrapper
import yv.tils.gui.utils.GuiStyle
import yv.tils.utils.colors.Colors

/** Keep status colors explicit: generic GuiStyle.lore deliberately removes them. */
internal object FusionStyle {
    fun item(stack: ItemStack, name: Component, lore: List<Component>, color: Colors = Colors.SECONDARY): ItemWrapper {
        val copy = stack.clone()
        copy.editMeta {
            it.displayName(GuiStyle.title(name, color))
            it.lore(lore)
        }
        return ItemWrapper(copy)
    }

    fun item(material: Material, name: Component, lore: List<Component>, color: Colors = Colors.SECONDARY) =
        item(ItemStack(material), name, lore, color)

    fun status(text: Component, good: Boolean) = GuiStyle.title(text, if (good) Colors.GREEN else Colors.RED)
}
