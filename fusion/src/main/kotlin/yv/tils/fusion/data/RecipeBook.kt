/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
// Legacy combined-file model retains its package for compatibility.
package yv.tils.fusion

import yv.tils.fusion.configs.DefaultRecipes

data class RecipeBook(val schemaVersion: Int = 2, val recipes: List<FusionRecipe> = DefaultRecipes.all())
