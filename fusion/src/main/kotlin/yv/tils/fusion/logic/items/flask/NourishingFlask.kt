/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */
package yv.tils.fusion.logic.items.flask

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.Material
import org.bukkit.Color
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataType
import yv.tils.fusion.FusionRecipe
import yv.tils.fusion.api.FusionItemBehavior
import yv.tils.fusion.api.FusionOutputType
import yv.tils.fusion.api.OutputSelection
import yv.tils.fusion.language.FusionText
import yv.tils.fusion.logic.items.OutputLore
import java.util.*

object NourishingFlask : FusionOutputType, FusionItemBehavior {
    private val lastUse = mutableMapOf<UUID, Int>()
    private val costKey = NamespacedKey("yvtils", "flask_xp_per_food")
    private val foodKey = NamespacedKey("yvtils", "flask_food_per_use")
    private val cooldownKey = NamespacedKey("yvtils", "flask_cooldown")
    private val saturationKey = NamespacedKey("yvtils", "flask_saturation_per_use")
    private val saturationCostKey = NamespacedKey("yvtils", "flask_xp_per_saturation")
    private fun setting(recipe: FusionRecipe, key: String, default: Int): Int =
        recipe.settings[key]?.toIntOrNull()
            ?: if (key in recipe.settings) error("Invalid flask setting: $key") else default

    override fun validate(recipe: FusionRecipe) {
        require(recipe.output in listOf("GLASS_BOTTLE", "POTION") && recipe.outputAmount == 1)
        require(setting(recipe, "xpPerFoodPoint", 8) in 1..10000)
        require(setting(recipe, "foodPerUse", 4) in 1..20)
        require(setting(recipe, "cooldownTicks", 20) in 1..1200)
        require(setting(recipe, "saturationPerUse", 4) in 0..20)
        require(setting(recipe, "xpPerSaturationPoint", 8) in 1..10000)
    }

    override fun create(recipe: FusionRecipe, base: ItemStack, selection: OutputSelection): ItemStack = base.apply {
        // Accept legacy glass-bottle recipes, but always produce the colored potion representation.
        type = Material.POTION
        editMeta {
            (it as PotionMeta).color = Color.fromRGB(0x7ED957)
            it.setMaxStackSize(1)
            val cost = setting(recipe, "xpPerFoodPoint", 8)
            val food = setting(recipe, "foodPerUse", 4)
            val saturation = setting(recipe, "saturationPerUse", 4)
            val saturationCost = setting(recipe, "xpPerSaturationPoint", 8)
            it.persistentDataContainer.set(saturationKey, PersistentDataType.INTEGER, saturation)
            it.persistentDataContainer.set(saturationCostKey, PersistentDataType.INTEGER, saturationCost)
            it.persistentDataContainer.set(costKey, PersistentDataType.INTEGER, cost)
            it.persistentDataContainer.set(foodKey, PersistentDataType.INTEGER, food)
            it.persistentDataContainer.set(
                cooldownKey,
                PersistentDataType.INTEGER,
                setting(recipe, "cooldownTicks", 20)
            )
        }
        OutputLore.apply(this, recipe, listOf("Right-click to nourish yourself.", "Reusable • Hunger is restored first."),
            listOf("Hunger / use" to "Up to ${setting(recipe, "foodPerUse", 4)} points",
                "Hunger price" to "${setting(recipe, "xpPerFoodPoint", 8)} XP points / point",
                "Saturation / use" to "Up to ${setting(recipe, "saturationPerUse", 4)} points",
                "Saturation price" to "${setting(recipe, "xpPerSaturationPoint", 8)} XP points / point",
                "Cooldown" to "${setting(recipe, "cooldownTicks", 20) / 20.0} s"))
    }

    override fun interact(event: PlayerInteractEvent, item: ItemStack) {
        if (event.action !in listOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) return
        // Right-click-air is pre-cancelled by vanilla when no vanilla use exists.
        if (event.action == Action.RIGHT_CLICK_BLOCK && event.isCancelled) return
        event.isCancelled = true
        val player = event.player
        if (player.gameMode !in listOf(GameMode.SURVIVAL, GameMode.ADVENTURE) || player.isDead) return
        if (!player.hasPermission("yvtils.fusion.use") || !player.hasPermission("yvtils.fusion.flask.use")) {
            FusionText.send(player, "denied"); return
        }
        val pdc = item.itemMeta.persistentDataContainer
        val cost = pdc.get(costKey, PersistentDataType.INTEGER) ?: 8
        val food = pdc.get(foodKey, PersistentDataType.INTEGER) ?: 4
        val cooldown = pdc.get(cooldownKey, PersistentDataType.INTEGER) ?: 20
        val saturation = pdc.get(saturationKey, PersistentDataType.INTEGER) ?: 4
        val saturationCost = pdc.get(saturationCostKey, PersistentDataType.INTEGER) ?: 8
        if (cost !in 1..10000 || food !in 1..20 || cooldown !in 1..1200) return
        if (saturation !in 0..20 || saturationCost !in 1..10000) return
        val tick = Bukkit.getCurrentTick()
        if (lastUse[player.uniqueId]?.let { tick - it < cooldown } == true) return
        val points = player.calculateTotalExperiencePoints()
        val quote = FlaskPricing.quote(player.foodLevel, points, cost, food)
        val saturationQuote = FlaskPricing.saturation(player.foodLevel + quote.food, player.saturation,
            points - quote.cost, saturationCost, saturation)
        if (quote.food == 0 && saturationQuote.amount == 0f) {
            val full = player.foodLevel == 20 && (saturation == 0 || player.saturation >= player.foodLevel)
            FusionText.send(player, if (full) "flaskFull" else "flaskXp", mapOf("cost" to minOf(cost, saturationCost)))
            lastUse[player.uniqueId] = tick
            return
        }
        // Reserve the use before invoking other listeners, preventing recursive/double-hand spending.
        lastUse[player.uniqueId] = tick
        val change = org.bukkit.event.entity.FoodLevelChangeEvent(player, player.foodLevel + quote.food, item.clone())
        if (!change.callEvent()) return
        // Other listeners may change hunger or XP while handling the event; price from the current state.
        val currentPoints = player.calculateTotalExperiencePoints()
        val gained = minOf(
            (change.foodLevel - player.foodLevel).coerceIn(0, quote.food),
            currentPoints / cost, 20 - player.foodLevel
        )
        val nourishment = FlaskPricing.saturation(player.foodLevel + gained, player.saturation,
            currentPoints - gained * cost, saturationCost, saturation)
        if (gained == 0 && nourishment.amount == 0f) return
        val beforeFood = player.foodLevel
        val beforeSaturation = player.saturation
        val totalCost = gained * cost + nourishment.cost
        try {
            player.setExperienceLevelAndProgress(currentPoints - totalCost)
            player.foodLevel = beforeFood + gained
            player.saturation = (beforeSaturation + nourishment.amount).coerceAtMost(player.foodLevel.toFloat())
        } catch (error: Exception) {
            player.setExperienceLevelAndProgress(currentPoints)
            player.foodLevel = beforeFood
            player.saturation = beforeSaturation
            throw error
        }
        lastUse[player.uniqueId] = tick
        FusionText.send(player, "flaskNourished", mapOf("food" to gained, "saturation" to nourishment.amount, "cost" to totalCost))
    }

    fun forget(id: UUID) {
        lastUse.remove(id)
    }

    fun clear() {
        lastUse.clear()
    }
}
