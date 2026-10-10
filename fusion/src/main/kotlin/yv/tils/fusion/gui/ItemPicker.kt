/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.gui

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import yv.tils.fusion.language.FusionText
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.GuiStyle

/** Clickable copies of inventory items: selecting never consumes or moves the originals. */
object ItemPicker {
    fun open(player: Player, back: (Player) -> Unit, select: (Player, ItemStack) -> Unit) {
        if (!FusionGui.allowed(player, true)) return
        val items = player.inventory.contents.filterNotNull().filterNot { it.type.isAir }.map { stack ->
            val snapshot = stack.clone()
            Item.builder().setItemProvider {
                val display = snapshot.clone()
                display.editMeta { meta ->
                    meta.lore(
                        meta.lore().orEmpty() + listOf(
                            GuiStyle.lore(FusionText.text(player, "pickHint"))
                        )
                    )
                }
                xyz.xenondevs.invui.item.ItemWrapper(display)
            }.addClickHandler { _, event ->
                val viewer = event.player()
                if (FusionGui.allowed(viewer, true)) select(viewer, snapshot.clone())
            }.build()
        }.ifEmpty {
            listOf(
                Item.simple(
                    GuiStyle.field(
                        Material.BARRIER,
                        FusionText.text(player, "inventoryEmpty"),
                        emptyList()
                    )
                )
            )
        }
        val gui = PagedGui.itemsBuilder().setStructure(
            "# x x x x x x x #", "# x x x x x x x #", "# x x x x x x x #", "b # # < r > # # #"
        )
            .addIngredient('#', Filler.item()).addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', FusionGui.page(player, false, true))
            .addIngredient('>', FusionGui.page(player, true, true))
            .addIngredient('b', FusionGui.button(player, Material.ARROW, "back", click = back))
            .addIngredient('r', FusionGui.button(player, Material.SUNFLOWER, "refresh") { open(it, back, select) })
            .setContent(items).build()
        FusionGui.show(player, "pickItem", gui)
    }
}
