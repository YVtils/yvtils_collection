/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
// Public API model retains its original package for addon compatibility.
package yv.tils.fusion

import org.bukkit.inventory.ItemStack
import yv.tils.fusion.logic.items.FusionItems
import yv.tils.fusion.utils.BukkitTags

data class Ingredient(
    val amount: Int = 1,
    val materials: List<String> = emptyList(),
    val tag: String = "",
    val fusionId: String = "",
    val exactItem: String = "",
) {
    fun matches(item: ItemStack): Boolean {
        if (item.type.isAir) return false
        if (exactItem.isNotBlank()) return item.isSimilar(FusionItems.decode(exactItem))
        if (fusionId.isNotBlank()) return FusionItems.id(item) == fusionId
        if (FusionItems.id(item) != null) return false
        return item.type.name in materials || tag.isNotBlank() && BukkitTags.block(tag)?.isTagged(item.type) == true
    }
}
