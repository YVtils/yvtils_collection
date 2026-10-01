/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.gui.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import yv.tils.utils.colors.Colors
import yv.tils.utils.message.MessageUtils

/** Consistent palette for shared editors and module menus, independent of translation styling. */
object GuiStyle {
    fun title(text: Component, color: Colors = Colors.SECONDARY): Component =
        Component.text(MessageUtils.strip(text), TextColor.fromHexString(color.color))
            .decoration(TextDecoration.ITALIC, false)

    fun lore(text: Component): Component = title(text, Colors.TERTIARY)

    fun field(material: Material, name: Component, lines: List<Component>): ItemBuilder {
        val stack = ItemStack(material)
        stack.editMeta {
            it.displayName(title(name))
            it.lore(lines.map(::lore))
        }
        return ItemBuilder(stack)
    }

    /** First anvil slot is a renameable paper carrying the literal existing value. */
    fun inputPaper(value: String): Item = Item.simple(field(Material.PAPER, Component.text(value), emptyList()))
}
