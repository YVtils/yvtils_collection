/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.storage

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import yv.tils.metrics.data.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class WriteBufferTest {
    private val player = UUID.randomUUID()
    private val now = Instant.parse("2026-10-28T12:00:00Z")

    @Test fun `playtime is split exactly across UTC midnight`() {
        val buffer = WriteBuffer()
        buffer.playtime(player, Instant.parse("2026-10-27T23:59:58Z"), Instant.parse("2026-10-28T00:00:03Z"), 5000)
        val deltas = buffer.drain()!!.deltas
        assertEquals(2000L, deltas[DeltaKey(player, PlayerStat.PLAYTIME, LocalDate.of(2026, 10, 27))])
        assertEquals(3000L, deltas[DeltaKey(player, PlayerStat.PLAYTIME, LocalDate.of(2026, 10, 28))])
        assertNull(buffer.drain())
    }

    @Test fun `wall clock rollback does not subtract monotonic playtime`() {
        val buffer = WriteBuffer()
        buffer.playtime(player, now, now.minusSeconds(100), 2000)
        assertEquals(2000L, buffer.drain()!!.deltas.values.sum())
    }

    @Test fun `identity heartbeats retain the first uncommitted import baseline`() {
        val buffer = WriteBuffer()
        buffer.identity(PlayerIdentity(player, "Player", 1, 2, mapOf(PlayerStat.DEATHS to 10L)))
        buffer.identity(PlayerIdentity(player, "Renamed", 3, 4))
        val identity = buffer.drain()!!.players.single()
        assertEquals(1L, identity.firstSeenMillis)
        assertEquals("Renamed", identity.name)
        assertEquals(10L, identity.vanillaBaseline[PlayerStat.DEATHS])
    }

    @Test fun `failed commit keeps immutable batch while new events are buffered`() {
        val buffer = WriteBuffer()
        buffer.increment(player, PlayerStat.DEATHS, 2, now)
        var fail = true
        val attempted = mutableListOf<WriteBatch>()
        val committed = mutableListOf<WriteBatch>()
        val writer = BatchWriter(buffer) {
            attempted += it
            if (fail) throw IllegalStateException("disk full")
            committed += it
        }
        assertThrows(IllegalStateException::class.java) { writer.flush() }
        buffer.increment(player, PlayerStat.DEATHS, 3, now)
        fail = false
        writer.flush()
        assertEquals(attempted[0].id, attempted[1].id)
        assertEquals(listOf(2L, 3L), committed.map { it.deltas.values.sum() })
    }

    @Test fun `negative increments are rejected`() {
        assertThrows(IllegalArgumentException::class.java) { WriteBuffer().increment(player, PlayerStat.DEATHS, -1, now) }
    }

    @Test fun `prolonged storage failure cannot grow the buffer without bound`() {
        val buffer = WriteBuffer(maxEntries = 2)
        buffer.increment(player, PlayerStat.DEATHS, 1, now)
        buffer.increment(player, PlayerStat.DEATHS, 2, now)
        buffer.increment(player, PlayerStat.MOB_KILLS, 1, now)
        assertFalse(buffer.saturated)
        buffer.increment(player, PlayerStat.PLAYER_KILLS, 1, now)
        assertTrue(buffer.saturated)
        assertEquals(2, buffer.drain()!!.deltas.size)
        buffer.increment(player, PlayerStat.DEATHS, 1, now)
        assertNull(buffer.drain()) // Recovery requires an explicit restart, not a silent partial resume.
    }

    @Test fun `favorites coalesce by player category key and UTC day`() {
        val buffer = WriteBuffer()
        val mined = BreakdownKey(BreakdownCategory.BLOCKS_BROKEN, "minecraft:stone")
        val placed = BreakdownKey(BreakdownCategory.BLOCKS_PLACED, "minecraft:stone")
        buffer.breakdown(player, mined, 2, now)
        buffer.breakdown(player, mined, 3, now)
        buffer.breakdown(player, placed, 4, now)
        val batch = buffer.drain()!!
        assertEquals(5L, batch.breakdowns[BreakdownDeltaKey(player, mined, LocalDate.of(2026, 10, 28))])
        assertEquals(4L, batch.breakdowns[BreakdownDeltaKey(player, placed, LocalDate.of(2026, 10, 28))])
        assertNull(buffer.drain())
    }

    @Test fun `new duration statistics also split at UTC midnight`() {
        val buffer = WriteBuffer()
        buffer.duration(player, PlayerStat.AFK_TIME, Instant.parse("2026-10-27T23:59:58Z"), Instant.parse("2026-10-28T00:00:03Z"), 5000)
        val deltas = buffer.drain()!!.deltas
        assertEquals(2000L, deltas[DeltaKey(player, PlayerStat.AFK_TIME, LocalDate.of(2026, 10, 27))])
        assertEquals(3000L, deltas[DeltaKey(player, PlayerStat.AFK_TIME, LocalDate.of(2026, 10, 28))])
    }

    @Test fun `large import baselines are included in the memory safety budget`() {
        val buffer = WriteBuffer(maxEntries = 2)
        val baselines = mapOf(
            BreakdownKey(BreakdownCategory.BLOCKS_BROKEN, "minecraft:stone") to 1L,
            BreakdownKey(BreakdownCategory.BLOCKS_BROKEN, "minecraft:dirt") to 2L,
        )
        buffer.identity(PlayerIdentity(player, "Player", 1, 2, breakdownBaseline = baselines))
        assertTrue(buffer.saturated)
        assertNull(buffer.drain())
    }

    @Test fun `identity heartbeat preserves pending favorite imports across renames`() {
        val buffer = WriteBuffer()
        val key = BreakdownKey(BreakdownCategory.MOBS_KILLED, "minecraft:zombie")
        buffer.identity(PlayerIdentity(player, "Player", 1, 2, breakdownBaseline = mapOf(key to 10L)))
        buffer.identity(PlayerIdentity(player, "Renamed", 3, 4))
        val identity = buffer.drain()!!.players.single()
        assertEquals(10L, identity.breakdownBaseline[key])
        assertEquals("Renamed", identity.name)
    }
}
