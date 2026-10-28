/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.collection

import org.bukkit.Statistic
import org.bukkit.entity.EntityType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import yv.tils.metrics.data.PlayerStat
import yv.tils.metrics.data.BreakdownCategory

class VanillaStatisticsTest {
    @Test fun `fall distance is separate so airborne travel is not counted twice`() {
        assertEquals(PlayerStat.FALL_DISTANCE, VanillaStatistics.sampled[Statistic.FALL_ONE_CM])
        assertFalse(PlayerStat.FALL_DISTANCE in VanillaStatistics.distanceStats)
        assertTrue(PlayerStat.FLY_DISTANCE in VanillaStatistics.distanceStats)
        assertTrue(PlayerStat.OTHER_DISTANCE in VanillaStatistics.distanceStats)
    }

    @Test fun `each sampled statistic has exactly one source category`() {
        assertEquals(PlayerStat.WALK_DISTANCE, VanillaStatistics.sampled[Statistic.WALK_ONE_CM])
        assertEquals(PlayerStat.SWIM_DISTANCE, VanillaStatistics.sampled[Statistic.SWIM_ONE_CM])
        assertEquals(PlayerStat.DAMAGE_DEALT, VanillaStatistics.sampled[Statistic.DAMAGE_DEALT])
        assertFalse(Statistic.PLAY_ONE_MINUTE in VanillaStatistics.sampled) // Live time uses a monotonic clock.
    }

    @Test fun `new scalar statistics map to vanilla without reusing event sources`() {
        val expected = mapOf(
            Statistic.DAMAGE_TAKEN to PlayerStat.DAMAGE_TAKEN,
            Statistic.JUMP to PlayerStat.JUMPS,
            Statistic.RAID_WIN to PlayerStat.RAIDS_WON,
            Statistic.ANIMALS_BRED to PlayerStat.ANIMALS_BRED,
            Statistic.TRADED_WITH_VILLAGER to PlayerStat.VILLAGER_TRADES,
            Statistic.CHEST_OPENED to PlayerStat.CHESTS_OPENED,
            Statistic.ITEM_ENCHANTED to PlayerStat.ITEMS_ENCHANTED,
            Statistic.BELL_RING to PlayerStat.BELLS_RUNG,
            Statistic.SNEAK_TIME to PlayerStat.SNEAK_TIME,
        )
        expected.forEach { (vanilla, metric) -> assertEquals(metric, VanillaStatistics.sampled[vanilla]) }
        assertEquals(PlayerStat.ITEMS_PICKED_UP, VanillaStatistics.materialStat(Statistic.PICKUP, null))
        assertEquals(PlayerStat.ITEMS_DROPPED, VanillaStatistics.materialStat(Statistic.DROP, null))
        assertFalse(Statistic.PICKUP in VanillaStatistics.sampled)
        assertFalse(Statistic.KILL_ENTITY in VanillaStatistics.sampled)
    }

    @Test fun `sneaking ticks convert to milliseconds while damage and item counts retain raw units`() {
        assertEquals(2500L, VanillaStatistics.storageValue(Statistic.SNEAK_TIME, 50))
        assertEquals(35L, VanillaStatistics.storageValue(Statistic.DAMAGE_TAKEN, 35))
        assertEquals(64L, VanillaStatistics.storageValue(Statistic.PICKUP, 64))
    }

    @Test fun `monsters and non enemy mobs use disjoint kill categories`() {
        listOf(EntityType.ZOMBIE, EntityType.SPIDER, EntityType.SLIME, EntityType.GHAST, EntityType.ENDER_DRAGON).forEach {
            assertEquals(PlayerStat.MONSTER_KILLS, VanillaStatistics.killCategory(it)?.stat, it.name)
        }
        listOf(EntityType.COW, EntityType.SQUID, EntityType.COD, EntityType.IRON_GOLEM, EntityType.ZOMBIE_HORSE).forEach {
            assertEquals(BreakdownCategory.ANIMALS_KILLED, VanillaStatistics.killCategory(it)?.breakdown, it.name)
        }
        assertNull(VanillaStatistics.killCategory(EntityType.PLAYER))
        assertNull(VanillaStatistics.killCategory(EntityType.ARMOR_STAND))
        assertNull(VanillaStatistics.killCategory(null))
    }
}
