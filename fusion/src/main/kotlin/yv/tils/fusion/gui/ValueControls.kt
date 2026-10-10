/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.gui

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import xyz.xenondevs.invui.item.Item
import yv.tils.configv2.language.LanguageHandler
import yv.tils.fusion.language.FusionText
import yv.tils.gui.utils.GuiStyle

object ValueControls {
    fun delta(click: ClickType): Int? = when (click) {
        ClickType.LEFT -> 1
        ClickType.RIGHT -> -1
        ClickType.SHIFT_LEFT -> 10
        ClickType.SHIFT_RIGHT -> -10
        else -> null
    }

    fun number(
        player: Player, material: Material, title: String, value: Int, range: IntRange,
        back: (Player) -> Unit, update: (Player, Int) -> Unit,
        params: Map<String, Any> = emptyMap()
    ): Item = Item.builder().setItemProvider {
        FusionStyle.item(
            material, FusionText.text(player, title, params), listOf(
                GuiStyle.lore(FusionText.text(player, "currentValue", mapOf("value" to value))),
                GuiStyle.lore(LanguageHandler.getMessage("action.gui.lore.controls.number", player)),
                GuiStyle.lore(FusionText.text(player, "numberInputHint"))
            )
        )
    }.addClickHandler { _, event ->
        val viewer = event.player()
        if (!FusionGui.allowed(viewer, true)) return@addClickHandler
        val delta = delta(event.clickType())
        if (delta != null) update(
            viewer,
            (value.toLong() + delta).coerceIn(range.first.toLong(), range.last.toLong()).toInt()
        )
        else if (event.clickType() == ClickType.MIDDLE) FusionGui.input(
            viewer,
            "editor",
            value.toString(),
            true,
            back
        ) { actor, input ->
            val parsed = input.toIntOrNull()
            if (parsed != null && parsed in range) update(actor, parsed)
            else {
                FusionText.send(actor, "error", mapOf("error" to "${range.first}..${range.last}")); back(actor)
            }
        }
    }.build()
}
