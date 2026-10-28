/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.collection

import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.entity.Enemy
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import yv.tils.metrics.data.BreakdownCategory
import yv.tils.metrics.data.BreakdownKey
import yv.tils.metrics.data.PlayerStat

object VanillaStatistics {
    val sampled: Map<Statistic, PlayerStat> = buildMap {
        put(Statistic.DEATHS, PlayerStat.DEATHS)
        put(Statistic.MOB_KILLS, PlayerStat.MOB_KILLS)
        put(Statistic.PLAYER_KILLS, PlayerStat.PLAYER_KILLS)
        put(Statistic.DAMAGE_DEALT, PlayerStat.DAMAGE_DEALT)
        put(Statistic.DAMAGE_TAKEN, PlayerStat.DAMAGE_TAKEN)
        put(Statistic.FISH_CAUGHT, PlayerStat.FISH_CAUGHT)
        put(Statistic.JUMP, PlayerStat.JUMPS)
        put(Statistic.RAID_WIN, PlayerStat.RAIDS_WON)
        put(Statistic.ANIMALS_BRED, PlayerStat.ANIMALS_BRED)
        put(Statistic.TRADED_WITH_VILLAGER, PlayerStat.VILLAGER_TRADES)
        put(Statistic.CHEST_OPENED, PlayerStat.CHESTS_OPENED)
        put(Statistic.ITEM_ENCHANTED, PlayerStat.ITEMS_ENCHANTED)
        put(Statistic.BELL_RING, PlayerStat.BELLS_RUNG)
        put(Statistic.SNEAK_TIME, PlayerStat.SNEAK_TIME)
        val groups = mapOf(
            PlayerStat.WALK_DISTANCE to listOf("WALK_ONE_CM", "SPRINT_ONE_CM", "CROUCH_ONE_CM"),
            PlayerStat.SWIM_DISTANCE to listOf("SWIM_ONE_CM", "WALK_ON_WATER_ONE_CM", "WALK_UNDER_WATER_ONE_CM"),
            PlayerStat.FLY_DISTANCE to listOf("FLY_ONE_CM", "AVIATE_ONE_CM"),
            PlayerStat.VEHICLE_DISTANCE to listOf(
                "BOAT_ONE_CM", "MINECART_ONE_CM", "HORSE_ONE_CM", "PIG_ONE_CM", "STRIDER_ONE_CM",
                "HAPPY_GHAST_ONE_CM", "NAUTILUS_ONE_CM",
            ),
            PlayerStat.OTHER_DISTANCE to listOf("CLIMB_ONE_CM"),
            // Falling can overlap airborne travel. Keep it separate from the travel leaderboard.
            PlayerStat.FALL_DISTANCE to listOf("FALL_ONE_CM"),
        )
        groups.forEach { (stat, names) ->
            // Some supported server versions introduce additional mounts.
            names.forEach { name -> Statistic.entries.find { it.name == name }?.let { put(it, stat) } }
        }
    }
    val distanceStats = setOf(
        PlayerStat.WALK_DISTANCE, PlayerStat.SWIM_DISTANCE, PlayerStat.FLY_DISTANCE,
        PlayerStat.VEHICLE_DISTANCE, PlayerStat.OTHER_DISTANCE,
    )

    fun snapshot(player: Player): Map<Statistic, Int> = sampled.keys.associateWith(player::getStatistic)

    fun materialStat(statistic: Statistic, material: Material?): PlayerStat? = when (statistic) {
        Statistic.MINE_BLOCK -> PlayerStat.BLOCKS_BROKEN
        Statistic.CRAFT_ITEM -> PlayerStat.ITEMS_CRAFTED
        Statistic.PICKUP -> PlayerStat.ITEMS_PICKED_UP
        Statistic.DROP -> PlayerStat.ITEMS_DROPPED
        Statistic.USE_ITEM -> when {
            material == Material.TOTEM_OF_UNDYING -> PlayerStat.TOTEMS_USED
            material?.isEdible == true -> PlayerStat.FOOD_EATEN
            else -> null
        }
        else -> null
    }

    fun storageValue(statistic: Statistic, value: Long): Long {
        require(value >= 0)
        return if (statistic == Statistic.SNEAK_TIME) Math.multiplyExact(value, 50L) else value
    }

    data class KillCategory(val stat: PlayerStat, val breakdown: BreakdownCategory)

    fun killCategory(type: EntityType?): KillCategory? {
        val entityClass = type?.entityClass ?: return null
        return when {
            Enemy::class.java.isAssignableFrom(entityClass) -> KillCategory(PlayerStat.MONSTER_KILLS, BreakdownCategory.MOBS_KILLED)
            Mob::class.java.isAssignableFrom(entityClass) -> KillCategory(PlayerStat.ANIMAL_KILLS, BreakdownCategory.ANIMALS_KILLED)
            else -> null // Players, armor stands and non-living entities do not belong to these lists.
        }
    }

    data class Baseline(val totals: Map<PlayerStat, Long> = emptyMap(), val breakdowns: Map<BreakdownKey, Long> = emptyMap())

    /** Only requested for the optional, first-record import. It never writes daily history. */
    fun baseline(player: Player, gameplay: Boolean, snapshot: Map<Statistic, Int>, collectBreakdowns: Boolean): Baseline {
        val breakdowns = mutableMapOf<BreakdownKey, Long>()
        val totals = buildMap<PlayerStat, Long> {
            put(PlayerStat.PLAYTIME, player.getStatistic(Statistic.PLAY_ONE_MINUTE).toLong().coerceAtLeast(0) * 50)
            if (!gameplay) return@buildMap
            snapshot.forEach { (vanilla, value) ->
                val stat = sampled.getValue(vanilla)
                put(stat, (get(stat) ?: 0L) + storageValue(vanilla, value.toLong().coerceAtLeast(0)))
            }
            put(PlayerStat.DISTANCE, distanceStats.sumOf { get(it) ?: 0L })
            // Block placement, AFK, historical advancements and world changes cannot be reconstructed.
            Material.entries.filterNot { it.isLegacy }.forEach { material ->
                if (material.isBlock) {
                    val count = player.getStatistic(Statistic.MINE_BLOCK, material).toLong().coerceAtLeast(0)
                    put(PlayerStat.BLOCKS_BROKEN, (get(PlayerStat.BLOCKS_BROKEN) ?: 0L) + count)
                    if (collectBreakdowns && count > 0) breakdowns[BreakdownKey(BreakdownCategory.BLOCKS_BROKEN, material.key.toString())] = count
                }
                if (material.isItem) {
                    listOf(Statistic.CRAFT_ITEM, Statistic.PICKUP, Statistic.DROP).forEach { vanilla ->
                        val stat = materialStat(vanilla, material)!!
                        put(stat, (get(stat) ?: 0L) + player.getStatistic(vanilla, material).coerceAtLeast(0))
                    }
                    materialStat(Statistic.USE_ITEM, material)?.let { stat ->
                        put(stat, (get(stat) ?: 0L) + player.getStatistic(Statistic.USE_ITEM, material).coerceAtLeast(0))
                    }
                }
            }
            EntityType.entries.forEach { type ->
                val category = killCategory(type) ?: return@forEach
                val count = player.getStatistic(Statistic.KILL_ENTITY, type).toLong().coerceAtLeast(0)
                put(category.stat, (get(category.stat) ?: 0L) + count)
                if (collectBreakdowns && count > 0) breakdowns[BreakdownKey(category.breakdown, type.key.toString())] = count
            }
        }
        return Baseline(totals, breakdowns)
    }
}
