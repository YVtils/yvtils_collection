/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.configs

import yv.tils.fusion.FusionRecipe
import yv.tils.fusion.Ingredient

/** Bootstrap recipes only; administrator files become authoritative after initialization. */
object DefaultRecipes {
    val glassMaterials: List<String> = listOf("GLASS") + org.bukkit.Material.entries
        .filter { it.name.endsWith("_STAINED_GLASS") }.map { it.name }

    /** Upgrade only the built-in plain-glass selector, preserving exact/tag/custom selectors. */
    fun upgradeGlass(recipe: FusionRecipe): FusionRecipe {
        if (recipe.id !in listOf("invisible-frame", "light-block")) return recipe
        return recipe.copy(ingredients = recipe.ingredients.map {
            if (it.materials == listOf("GLASS") && it.tag.isBlank() && it.fusionId.isBlank() && it.exactItem.isBlank())
                it.copy(materials = glassMaterials) else it
        })
    }
    fun all() = listOf(
        FusionRecipe(
            "invisible-frame", "Invisible item frame", "An invisible frame for displaying items.",
            thumbnail = "ITEM_FRAME", ingredients = listOf(
                Ingredient(materials = listOf("ITEM_FRAME")),
                Ingredient(materials = glassMaterials, amount = 4)
            ), output = "ITEM_FRAME", outputKind = "INVISIBLE_FRAME"
        ),
        FusionRecipe(
            "light-block", "Light block", "Choose a light level from 0 to 15.", thumbnail = "LIGHT",
            ingredients = listOf(
                Ingredient(materials = listOf("GLOWSTONE_DUST"), amount = 4),
                Ingredient(materials = glassMaterials)
            ), output = "LIGHT", outputKind = "LIGHT"
        ),
        FusionRecipe(
            "player-head", "Player head", "Enter a player name and preview their skin.", thumbnail = "PLAYER_HEAD",
            ingredients = listOf(
                Ingredient(materials = listOf("ARMOR_STAND")),
                Ingredient(materials = listOf("LEATHER"), amount = 4), Ingredient(materials = listOf("NAME_TAG"))
            ),
            output = "PLAYER_HEAD", outputKind = "PLAYER_HEAD"
        ),
        nourishingFlask()
    )

    fun nourishingFlask() = FusionRecipe(
        "nourishing-flask", "Nourishing Flask",
        "Right-click to exchange XP points for hunger. Reusable; no automatic spending.", category = "Utility",
        thumbnail = "POTION", ingredients = listOf(
            Ingredient(materials = listOf("GLASS_BOTTLE")),
            Ingredient(amount = 2, materials = listOf("GOLD_INGOT")),
            Ingredient(amount = 4, materials = listOf("WHEAT")),
            Ingredient(materials = listOf("LAPIS_LAZULI"))
        ), output = "POTION", outputKind = "NOURISHING_FLASK",
        settings = mapOf("xpPerFoodPoint" to "8", "foodPerUse" to "4", "cooldownTicks" to "20",
            "saturationPerUse" to "4", "xpPerSaturationPoint" to "8")
    )
}
