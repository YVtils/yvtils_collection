/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.api

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import yv.tils.fusion.*
import yv.tils.fusion.configs.RecipeStore
import yv.tils.fusion.logic.crafting.Crafting
import yv.tils.fusion.logic.items.FusionItems
import yv.tils.utils.logger.Logger

/** Single registry backing the public service and built-in implementations. */
object FusionRegistry : FusionApi {
    private val outputs = linkedMapOf<String, FusionOutputType>()
    private val behaviors = linkedMapOf<String, FusionItemBehavior>()
    private val contributedRecipes = linkedMapOf<String, FusionRecipe>()
    private val guards = linkedSetOf<FusionCraftGuard>()
    private val observers = linkedSetOf<FusionCraftObserver>()

    private fun server() = check(Bukkit.isPrimaryThread()) { "Fusion API requires the server thread" }
    private fun <T> register(map: MutableMap<String, T>, id: String, value: T): AutoCloseable {
        server()
        require(id.matches(Regex("[A-Za-z0-9_.:-]+"))) { "Invalid registry ID" }
        require(id !in map) { "Already registered: $id" }
        map[id] = value
        return AutoCloseable { server(); if (map[id] === value) map.remove(id) }
    }

    override fun recipes(): List<FusionRecipe> {
        server(); return RecipeStore.book.recipes + contributedRecipes.values
    }

    override fun recipe(id: String): FusionRecipe? = recipes().firstOrNull { it.id == id }
    override fun registerRecipe(recipe: FusionRecipe): AutoCloseable {
        server()
        require(this.recipe(recipe.id) == null) { "Duplicate recipe ID: ${recipe.id}" }
        recipe.validate()
        return register(contributedRecipes, recipe.id, recipe)
    }

    override fun registerOutputType(id: String, type: FusionOutputType) = register(outputs, id, type)
    override fun registerBehavior(kind: String, behavior: FusionItemBehavior): AutoCloseable {
        server()
        require(kind in outputs) { "Register the output type first" }
        return register(behaviors, kind, behavior)
    }

    override fun registerCraftGuard(guard: FusionCraftGuard): AutoCloseable {
        server(); require(guards.add(guard))
        return AutoCloseable { server(); guards.remove(guard) }
    }

    override fun registerCraftObserver(observer: FusionCraftObserver): AutoCloseable {
        server(); require(observers.add(observer))
        return AutoCloseable { server(); observers.remove(observer) }
    }

    override fun itemId(item: ItemStack) = FusionItems.id(item)
    override fun itemKind(item: ItemStack) = FusionItems.kind(item)
    override fun createOutput(recipe: FusionRecipe, selection: OutputSelection): ItemStack {
        server(); recipe.validate(); return FusionItems.output(recipe, selection)
    }

    override fun craft(player: Player, recipeId: String, requested: Int, selection: OutputSelection): Int {
        server(); check(FusionYVtils.active) { "Fusion is disabled" }
        val recipe = recipe(recipeId) ?: error("stale")
        return Crafting.craft(player, recipe, createOutput(recipe, selection), requested)
    }

    fun outputType(id: String): FusionOutputType {
        server(); return outputs[id] ?: error("Unknown output type: $id")
    }

    fun behavior(id: String): FusionItemBehavior? {
        server(); return behaviors[id]
    }

    fun runtimeIds(): Set<String> {
        server(); return contributedRecipes.keys.toSet()
    }

    fun guard(context: FusionCraftContext) {
        guards.toList().forEach {
            val rejection = it.reject(context.copy(output = context.output.clone()))
            check(rejection == null) { rejection!! }
        }
    }

    fun notifyCraft(context: FusionCraftContext) {
        observers.toList().forEach {
            try {
                it.crafted(context.copy(output = context.output.clone()))
            } catch (error: Exception) {
                Logger.error("Fusion craft observer failed after commit", error)
            }
        }
    }

    fun clear() {
        server(); outputs.clear(); behaviors.clear(); contributedRecipes.clear(); guards.clear(); observers.clear()
    }
}
