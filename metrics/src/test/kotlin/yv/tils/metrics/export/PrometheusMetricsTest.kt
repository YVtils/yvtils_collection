/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.export

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import yv.tils.metrics.data.*
import java.util.UUID

class PrometheusMetricsTest {
    @Test fun `export converts storage units and emits proper cumulative histogram buckets`() {
        val metrics = PrometheusMetrics(false)
        metrics.database = DatabaseSnapshot(totals = mapOf(
            PlayerStat.PLAYTIME to 2500L, PlayerStat.DISTANCE to 125L, PlayerStat.DAMAGE_DEALT to 35L,
            PlayerStat.DAMAGE_TAKEN to 25L, PlayerStat.SNEAK_TIME to 1500L, PlayerStat.AFK_TIME to 7500L,
        ))
        metrics.tickDuration.observe(0.01)
        metrics.tickDuration.observe(0.1)
        metrics.render()
        val text = metrics.body().toString(Charsets.UTF_8)
        assertTrue(text.contains("yvtils_players_playtime_seconds_total 2.5"))
        assertTrue(text.contains("yvtils_players_distance_meters_total 1.25"))
        assertTrue(text.contains("yvtils_players_damage_dealt_health_points_total 3.5"))
        assertTrue(text.contains("yvtils_players_damage_taken_health_points_total 2.5"))
        assertTrue(text.contains("yvtils_players_sneak_seconds_total 1.5"))
        assertTrue(text.contains("yvtils_players_afk_seconds_total 7.5"))
        assertTrue(text.contains("yvtils_server_tick_duration_seconds_bucket{le=\"+Inf\"} 2"))
        assertTrue(text.contains("yvtils_server_tick_duration_seconds_count 2"))
        assertFalse(text.contains("player_uuid"))
    }

    @Test fun `leaderboards require explicit opt in and labels are escaped by client library`() {
        val snapshot = DatabaseSnapshot(leaderboards = mapOf(PlayerStat.DEATHS to listOf(
            LeaderboardEntry(UUID.randomUUID(), "Quoted\"Name\\", 4),
        )))
        val privateMetrics = PrometheusMetrics(false)
        privateMetrics.database = snapshot
        privateMetrics.render()
        assertFalse(privateMetrics.body().toString(Charsets.UTF_8).contains("player_name"))
        val publicMetrics = PrometheusMetrics(true)
        publicMetrics.database = snapshot
        publicMetrics.render()
        assertTrue(publicMetrics.body().toString(Charsets.UTF_8).contains("player_name=\"Quoted\\\"Name\\\\\""))
    }

    @Test fun `favorites have a separate explicit publication opt in`() {
        val snapshot = DatabaseSnapshot(favorites = listOf(FavoriteEntry(
            UUID.randomUUID(), "Player", BreakdownCategory.BLOCKS_PLACED, "minecraft:stone", 1, 42,
        )))
        val hidden = PrometheusMetrics(true)
        hidden.database = snapshot
        hidden.render()
        assertFalse(hidden.body().toString(Charsets.UTF_8).contains("yvtils_player_favorite_value"))
        val visible = PrometheusMetrics(false, true)
        visible.database = snapshot
        visible.render()
        val text = visible.body().toString(Charsets.UTF_8)
        assertTrue(text.contains("yvtils_player_favorite_value"))
        assertTrue(text.contains("subject=\"minecraft:stone\""))
        assertTrue(text.contains("player_name=\"Player\""))
    }
}
