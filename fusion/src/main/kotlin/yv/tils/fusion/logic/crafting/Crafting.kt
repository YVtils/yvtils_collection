/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.logic.crafting

import yv.tils.fusion.FusionRecipe

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import yv.tils.fusion.api.FusionRegistry
import yv.tils.fusion.api.FusionCraftContext

object Crafting {
    private val busy = mutableSetOf<UUID>()
    private val lastTick = mutableMapOf<UUID, Int>()

    class Preview(val contents: Array<ItemStack?>, val missing: IntArray, val room: Boolean) {
        val possible get() = missing.all { it == 0 } && room
    }

    fun plan(contents: Array<ItemStack?>, recipe: FusionRecipe, output: ItemStack, count: Int): Preview {
        require(count in 1..64)
        val allocation = IngredientAllocation.plan(
            IntArray(contents.size) { contents[it]?.amount ?: 0 },
            IntArray(recipe.ingredients.size) { recipe.ingredients[it].amount * count }) { slot, demand ->
            contents[slot]?.let { recipe.ingredients[demand].matches(it) } == true
        }
        val updated = Array(contents.size) { index ->
            contents[index]?.clone()?.apply {
                amount -= allocation.consumed[index]
            }?.takeIf { it.amount > 0 }
        }
        var remaining = output.amount * count
        updated.indices.forEach { index ->
            val stack = updated[index]
            if (stack != null && stack.isSimilar(output)) {
                val added = minOf(remaining, (stack.maxStackSize - stack.amount).coerceAtLeast(0))
                stack.amount += added
                remaining -= added
            }
        }
        updated.indices.forEach { index ->
            if (updated[index] == null && remaining > 0) {
                val added = minOf(remaining, output.maxStackSize)
                updated[index] = output.clone().apply { amount = added }
                remaining -= added
            }
        }
        return Preview(updated, allocation.missing, remaining == 0)
    }

    fun maximum(player: Player, recipe: FusionRecipe, output: ItemStack): Int = (64 downTo 1).firstOrNull {
        player.level.toLong() >= recipe.xpLevels.toLong() * it && plan(
            player.inventory.storageContents,
            recipe,
            output,
            it
        ).possible
    } ?: 0

    fun craft(player: Player, recipe: FusionRecipe, output: ItemStack, requested: Int): Int {
        check(Bukkit.isPrimaryThread())
        if (!busy.add(player.uniqueId)) return 0
        try {
            if (lastTick[player.uniqueId] == Bukkit.getCurrentTick()) return 0
            lastTick[player.uniqueId] = Bukkit.getCurrentTick()
            check(FusionRegistry.recipe(recipe.id) == recipe && recipe.enabled) { "stale" }
            check(
                player.hasPermission("yvtils.fusion.use") && (recipe.permission.isBlank() || player.hasPermission(
                    recipe.permission
                ))
            ) { "denied" }
            val count = if (requested == 0) maximum(player, recipe, output) else requested
            check(count in 1..64) { "missing" }
            val context = FusionCraftContext(player, recipe, count, output.clone())
            FusionRegistry.guard(context)
            check(FusionRegistry.recipe(recipe.id) == recipe && recipe.enabled) { "stale" }
            check(
                player.hasPermission("yvtils.fusion.use") && (recipe.permission.isBlank() || player.hasPermission(
                    recipe.permission
                ))
            ) { "denied" }
            val before = player.inventory.storageContents.map { it?.clone() }.toTypedArray()
            val plan = plan(before, recipe, output, count)
            check(plan.missing.all { it == 0 }) { "missing" }
            check(plan.room) { "full" }
            val xp = recipe.xpLevels * count
            check(player.level >= xp) { "xp" }
            val level = player.level
            val progress = player.exp
            try {
                player.inventory.storageContents = plan.contents
                player.level = level - xp
                player.exp = progress
            } catch (error: Exception) {
                player.inventory.storageContents = before
                player.level = level
                player.exp = progress
                throw error
            }
            FusionRegistry.notifyCraft(context)
            return count
        } finally {
            busy.remove(player.uniqueId)
        }
    }

    fun forget(uuid: UUID) {
        busy.remove(uuid); lastTick.remove(uuid)
    }

    fun clear() {
        busy.clear(); lastTick.clear()
    }
}
