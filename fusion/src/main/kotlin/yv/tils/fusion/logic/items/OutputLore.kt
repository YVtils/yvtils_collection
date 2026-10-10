/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.logic.items

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemStack
import yv.tils.fusion.FusionRecipe

/** Shared output styling; use literal text so recipe descriptions cannot inject formatting. */
object OutputLore {
    private fun line(text: String, color: NamedTextColor) = Component.text(text, color)
        .decoration(TextDecoration.ITALIC, false)

    fun apply(item: ItemStack, recipe: FusionRecipe, instructions: List<String> = emptyList(),
              stats: List<Pair<String, String>> = emptyList()) {
        val lore = buildList {
            add(line("✦ Fusion • ${recipe.category}", NamedTextColor.DARK_AQUA))
            if (recipe.description.isNotBlank()) {
                add(Component.empty())
                recipe.description.split(Regex("\\s+")).fold(mutableListOf<String>()) { lines, word ->
                    if (lines.isEmpty() || lines.last().length + word.length + 1 > 42) lines.add(word)
                    else lines[lines.lastIndex] += " $word"
                    lines
                }.forEach { add(line(it, NamedTextColor.GRAY)) }
            }
            if (stats.isNotEmpty()) {
                add(Component.empty())
                stats.forEach { (label, value) -> add(line("$label: ", NamedTextColor.GRAY)
                    .append(line(value, NamedTextColor.AQUA))) }
            }
            if (instructions.isNotEmpty()) {
                add(Component.empty())
                instructions.forEach { add(line("› $it", NamedTextColor.YELLOW)) }
            }
        }
        item.editMeta {
            it.displayName(line(recipe.name, NamedTextColor.AQUA))
            it.lore(lore)
        }
    }
}
