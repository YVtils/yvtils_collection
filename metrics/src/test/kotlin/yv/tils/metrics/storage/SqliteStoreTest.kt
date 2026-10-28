/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.storage

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.sqlite.JDBC
import yv.tils.metrics.data.*
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDate
import java.util.Properties
import java.util.UUID

class SqliteStoreTest {
    @TempDir lateinit var directory: Path
    private val player = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private val day = LocalDate.of(2026, 10, 28)
    private val now = Instant.parse("2026-10-28T12:00:00Z")
    private val path get() = directory.resolve("metrics.db")

    private fun identity(name: String = "Player", baseline: Map<PlayerStat, Long> = emptyMap()) =
        PlayerIdentity(player, name, now.toEpochMilli(), now.toEpochMilli(), baseline)

    private fun batch(value: Long = 3, name: String = "Player", baseline: Map<PlayerStat, Long> = emptyMap()) = WriteBatch(
        players = listOf(identity(name, baseline)),
        deltas = mapOf(DeltaKey(player, PlayerStat.DEATHS, day) to value), sessions = emptyList(),
    )

    @Test fun `totals persist and an acknowledged batch can be replayed safely`() {
        val batch = batch()
        SqliteStore(path).use {
            it.apply(batch, now)
            it.apply(batch, now)
            assertEquals(3L, it.snapshot(10, true, now).totals[PlayerStat.DEATHS])
        }
        SqliteStore(path).use {
            it.apply(batch, now)
            assertEquals(3L, it.snapshot(10, true, now).totals[PlayerStat.DEATHS])
            assertEquals("Player", it.snapshot(10, true, now).leaderboards[PlayerStat.DEATHS]!!.single().name)
        }
    }

    @Test fun `failed transaction rolls back its marker and all changes`() {
        val invalid = batch().copy(deltas = mapOf(DeltaKey(UUID.randomUUID(), PlayerStat.DEATHS, day) to 1L))
        SqliteStore(path).use {
            assertThrows(Exception::class.java) { it.apply(invalid, now) }
            assertEquals(0L, it.snapshot(10, true, now).players)
            // Same ID is still usable: the failed marker was rolled back along with the player insert.
            it.apply(batch().copy(id = invalid.id), now)
            assertEquals(3L, it.snapshot(10, true, now).totals[PlayerStat.DEATHS])
        }
    }

    @Test fun `lost acknowledgement after commit retries exactly once while preserving newer events`() {
        val buffer = WriteBuffer()
        val key = BreakdownKey(BreakdownCategory.BLOCKS_BROKEN, "minecraft:stone")
        buffer.identity(identity())
        buffer.increment(player, PlayerStat.DEATHS, 2, now)
        buffer.breakdown(player, key, 2, now)
        SqliteStore(path).use { store ->
            var loseAcknowledgement = true
            val writer = BatchWriter(buffer) { batch ->
                store.apply(batch, now)
                if (loseAcknowledgement) throw IllegalStateException("commit succeeded but acknowledgement was lost")
            }
            assertThrows(IllegalStateException::class.java) { writer.flush() }
            buffer.increment(player, PlayerStat.DEATHS, 3, now)
            buffer.breakdown(player, key, 3, now)
            loseAcknowledgement = false
            writer.flush()
            assertEquals(5L, store.snapshot(10, false, now).totals[PlayerStat.DEATHS])
            assertEquals(5L, store.snapshot(10, false, now, publishFavorites = true).favorites.single().value)
        }
    }

    @Test fun `vanilla import applies once only to new players and never to daily history`() {
        SqliteStore(path).use {
            it.apply(batch(baseline = mapOf(PlayerStat.DEATHS to 100L)), now)
            it.apply(batch(name = "Renamed", baseline = mapOf(PlayerStat.DEATHS to 200L)), now)
            val snapshot = it.snapshot(10, true, now)
            assertEquals(106L, snapshot.totals[PlayerStat.DEATHS])
            assertEquals(1L, snapshot.players)
            assertEquals("Renamed", snapshot.leaderboards[PlayerStat.DEATHS]!!.single().name)
        }
        connection().use { c ->
            c.createStatement().use { s ->
                s.executeQuery("SELECT SUM(value) FROM player_daily_stats WHERE stat = 'DEATHS'").use {
                    it.next(); assertEquals(6L, it.getLong(1))
                }
            }
        }
    }

    @Test fun `enabling import later cannot double count an already tracked player`() {
        SqliteStore(path).use {
            it.apply(batch(), now)
            it.apply(batch(baseline = mapOf(PlayerStat.DEATHS to 100L)), now)
            assertEquals(6L, it.snapshot(10, true, now).totals[PlayerStat.DEATHS])
        }
    }

    @Test fun `open sessions recover at last checkpoint without adding server downtime`() {
        val session = SessionRecord(UUID.randomUUID(), player, now.minusSeconds(30).toEpochMilli(), now.toEpochMilli(), 30_000)
        SqliteStore(path).use { it.apply(batch().copy(sessions = listOf(session)), now) }
        SqliteStore(path).close()
        connection().use { c ->
            c.createStatement().use { s ->
                s.executeQuery("SELECT duration_ms, ended_ms, interrupted FROM player_sessions").use {
                    it.next()
                    assertEquals(30_000L, it.getLong(1))
                    assertEquals(now.toEpochMilli(), it.getLong(2))
                    assertEquals(1, it.getInt(3))
                }
            }
        }
    }

    @Test fun `retention removes old history but preserves lifetime totals`() {
        SqliteStore(path).use {
            it.apply(batch(), now)
            it.prune(1, 1, now.plusSeconds(2 * 86400))
            assertEquals(3L, it.snapshot(10, false, now).totals[PlayerStat.DEATHS])
            assertTrue(it.snapshot(10, false, now).leaderboards.isEmpty())
        }
        connection().use { c -> c.createStatement().use { s ->
            s.executeQuery("SELECT COUNT(*) FROM player_daily_stats").use { it.next(); assertEquals(0, it.getInt(1)) }
        } }
    }

    @Test fun `future schema is refused without replacing existing data`() {
        SqliteStore(path).use { it.apply(batch(), now) }
        connection().use { c -> c.createStatement().use { it.execute("PRAGMA user_version = 999") } }
        assertThrows(IllegalArgumentException::class.java) { SqliteStore(path) }
        connection().use { c -> c.createStatement().use { s ->
            s.executeQuery("SELECT value FROM player_totals").use { it.next(); assertEquals(3L, it.getLong(1)) }
        } }
    }

    @Test fun `schema one upgrades without changing existing totals or importing an old player again`() {
        SqliteStore(path).use { it.apply(batch(), now) }
        connection().use { c -> c.createStatement().use {
            it.execute("DROP TABLE player_breakdown_daily")
            it.execute("DROP TABLE player_breakdown_totals")
            it.execute("PRAGMA user_version = 1")
        } }
        val key = BreakdownKey(BreakdownCategory.BLOCKS_BROKEN, "minecraft:stone")
        SqliteStore(path).use { store ->
            val update = batch().copy(
                players = listOf(identity(baseline = mapOf(PlayerStat.DEATHS to 100L)).copy(breakdownBaseline = mapOf(key to 1000L))),
                breakdowns = mapOf(BreakdownDeltaKey(player, key, day) to 4L),
            )
            store.apply(update, now)
            store.apply(update, now)
            val snapshot = store.snapshot(10, true, now, publishFavorites = true)
            assertEquals(6L, snapshot.totals[PlayerStat.DEATHS])
            assertEquals(4L, snapshot.favorites.single().value)
        }
        connection().use { c -> c.createStatement().use { s ->
            s.executeQuery("PRAGMA user_version").use { it.next(); assertEquals(2, it.getInt(1)) }
        } }
    }

    @Test fun `favorite imports and daily deltas commit once and remain separate`() {
        val key = BreakdownKey(BreakdownCategory.BLOCKS_BROKEN, "minecraft:stone")
        val update = batch().copy(
            players = listOf(identity().copy(breakdownBaseline = mapOf(key to 100L))),
            breakdowns = mapOf(BreakdownDeltaKey(player, key, day) to 4L),
        )
        SqliteStore(path).use { store ->
            store.apply(update, now)
            store.apply(update, now)
            assertTrue(store.snapshot(10, true, now).favorites.isEmpty())
            assertEquals(104L, store.snapshot(10, true, now, publishFavorites = true).favorites.single().value)
        }
        connection().use { c -> c.createStatement().use { s ->
            s.executeQuery("SELECT value FROM player_breakdown_daily").use { it.next(); assertEquals(4L, it.getLong(1)) }
        } }
        SqliteStore(path).use { store ->
            store.prune(1, 1, now.plusSeconds(86400 * 2))
            assertEquals(104L, store.snapshot(10, true, now, publishFavorites = true).favorites.single().value)
        }
        connection().use { c -> c.createStatement().use { s ->
            s.executeQuery("SELECT COUNT(*) FROM player_breakdown_daily").use { it.next(); assertEquals(0, it.getInt(1)) }
        } }
    }

    @Test fun `favorite export limits both players and entries per category`() {
        val newer = UUID.randomUUID()
        val key1 = BreakdownKey(BreakdownCategory.BLOCKS_PLACED, "minecraft:stone")
        val key2 = BreakdownKey(BreakdownCategory.BLOCKS_PLACED, "minecraft:dirt")
        val update = batch().copy(
            players = listOf(identity(), PlayerIdentity(newer, "Newer", now.toEpochMilli(), now.plusSeconds(5).toEpochMilli())),
            breakdowns = mapOf(
                BreakdownDeltaKey(player, key1, day) to 100L,
                BreakdownDeltaKey(newer, key1, day) to 3L,
                BreakdownDeltaKey(newer, key2, day) to 7L,
            ),
        )
        SqliteStore(path).use { store ->
            store.apply(update, now)
            val favorite = store.snapshot(10, false, now, publishFavorites = true, favoriteSize = 1, favoritePlayerLimit = 1).favorites.single()
            assertEquals(newer, favorite.player)
            assertEquals("minecraft:dirt", favorite.subject)
            assertEquals(1, favorite.rank)
        }
    }

    private fun connection() = JDBC().connect("jdbc:sqlite:$path", Properties())
}
