/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.gui.logic

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import yv.tils.configv2.language.LanguageHandler

/** Shared left-click toggle, optional right-click reset, and in-place display refresh. */
object ToggleControl {
    fun item(provider: (Player) -> ItemProvider, toggle: () -> Unit, reset: (() -> Unit)? = null): Item =
        Item.builder().setItemProvider { viewer -> provider(viewer) }.addClickHandler { item, click ->
            when (click.clickType()) {
                ClickType.LEFT -> toggle()
                ClickType.RIGHT -> reset?.invoke()
                else -> return@addClickHandler
            }
            item.notifyWindows()
        }.build()

    fun controls(player: Player) = LanguageHandler.getMessage("action.gui.lore.controls.boolean", player)
}
