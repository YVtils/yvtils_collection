/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion

import org.bukkit.Material
import yv.tils.fusion.api.FusionRegistry
import yv.tils.fusion.utils.BukkitTags
import yv.tils.fusion.logic.items.FusionItems

data class FusionRecipe(
    val id: String = "new-recipe",
    val name: String = "New recipe",
    val description: String = "",
    val category: String = "Decoration",
    val thumbnail: String = "CRAFTING_TABLE",
    val enabled: Boolean = true,
    val permission: String = "",
    val ingredients: List<Ingredient> = listOf(Ingredient(materials = listOf("STONE"))),
    val output: String = "STONE",
    val outputAmount: Int = 1,
    val outputKind: String = "NORMAL",
    val exactOutput: String = "",
    val xpLevels: Int = 0,
    val settings: Map<String, String> = emptyMap(),
    val exactThumbnail: String = "",
) {
    fun validate() {
        require(id.matches(Regex("[a-z0-9][a-z0-9_-]{0,63}"))) { "Invalid recipe ID" }
        require(name.isNotBlank() && name.length <= 128 && description.length <= 1024 && category.length <= 64)
        require(runCatching { Material.valueOf(thumbnail).isItem }.getOrDefault(false)) { "Invalid uppercase thumbnail" }
        FusionRegistry.outputType(outputKind).validate(this)
        require(settings.size <= 64 && settings.all { it.key.length <= 64 && it.value.length <= 1024 })
        require(runCatching {
            Material.valueOf(output).let { it.isItem && !it.isAir }
        }.getOrDefault(false)) { "Invalid uppercase output material" }
        require(outputAmount in 1..64 && xpLevels in 0..10000)
        require(ingredients.size in 1..36)
        ingredients.forEach {
            require(it.amount in 1..4096)
            val selectors = listOf(
                it.materials.isNotEmpty() || it.tag.isNotBlank(),
                it.fusionId.isNotBlank(),
                it.exactItem.isNotBlank()
            )
            require(selectors.count { selected -> selected } == 1) { "Ingredient needs one selector type" }
            it.materials.forEach { material ->
                require(
                    Material.matchMaterial(material)
                        ?.let { type -> type.isItem && !type.isAir } == true) { "Invalid material: $material" }
            }
            require(it.tag.isBlank() || BukkitTags.block(it.tag) != null) { "Unknown block tag: ${it.tag}" }
            if (it.exactItem.isNotBlank()) require(!FusionItems.decode(it.exactItem).type.isAir)
        }
        if (exactOutput.isNotBlank()) require(FusionItems.decode(exactOutput).type.name == output)
        if (exactThumbnail.isNotBlank()) require(FusionItems.decode(exactThumbnail).type.name == thumbnail)
        require(permission.isBlank() || permission.matches(Regex("[a-zA-Z0-9_.-]+")))
    }
}
