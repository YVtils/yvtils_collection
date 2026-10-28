/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.storage

import org.sqlite.JDBC
import yv.tils.metrics.data.DatabaseSnapshot
import yv.tils.metrics.data.LeaderboardEntry
import yv.tils.metrics.data.PlayerStat
import yv.tils.metrics.data.WriteBatch
import yv.tils.metrics.data.BreakdownCategory
import yv.tils.metrics.data.BreakdownKey
import yv.tils.metrics.data.FavoriteEntry
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.time.Instant
import java.time.ZoneOffset
import java.util.Properties
import java.util.UUID

/** Owned by one background worker. Never call this class from a Bukkit event or HTTP handler. */
class SqliteStore(path: Path) : AutoCloseable {
    private val connection: Connection

    init {
        Files.createDirectories(path.toAbsolutePath().parent)
        // Direct connection avoids JDBC DriverManager visibility surprises in Paper's library tier.
        connection = JDBC().connect("jdbc:sqlite:${path.toAbsolutePath()}", Properties())
        try {
            connection.createStatement().use { statement ->
                statement.execute("PRAGMA busy_timeout = 1000")
                statement.execute("PRAGMA foreign_keys = ON")
                statement.execute("PRAGMA journal_mode = WAL")
                statement.execute("PRAGMA synchronous = FULL")
            }
            migrate()
            // A previous process may have crashed: end sessions at their last durable checkpoint.
            connection.createStatement().use {
                it.executeUpdate("UPDATE player_sessions SET ended_ms = checkpoint_ms, interrupted = 1 WHERE ended_ms IS NULL")
            }
        } catch (failure: Throwable) {
            connection.close()
            throw failure
        }
    }

    private fun migrate() {
        val version = connection.createStatement().use { statement ->
            statement.executeQuery("PRAGMA user_version").use { it.next(); it.getInt(1) }
        }
        require(version in 0..2) { "Unsupported metrics database schema $version; refusing to overwrite it" }
        if (version == 2) return
        if (version == 0) transaction {
            connection.createStatement().use { statement ->
                listOf(
                    """CREATE TABLE players (
                        uuid TEXT PRIMARY KEY, name TEXT NOT NULL, first_seen_ms INTEGER NOT NULL,
                        last_seen_ms INTEGER NOT NULL, vanilla_imported INTEGER NOT NULL DEFAULT 0
                    )""",
                    "CREATE INDEX players_last_seen ON players(last_seen_ms)",
                    """CREATE TABLE player_totals (
                        uuid TEXT NOT NULL REFERENCES players(uuid), stat TEXT NOT NULL,
                        value INTEGER NOT NULL CHECK(typeof(value) = 'integer' AND value >= 0),
                        PRIMARY KEY(uuid, stat)
                    )""",
                    "CREATE INDEX player_totals_ranking ON player_totals(stat, value DESC, uuid)",
                    """CREATE TABLE player_daily_stats (
                        uuid TEXT NOT NULL REFERENCES players(uuid), day TEXT NOT NULL, stat TEXT NOT NULL,
                        value INTEGER NOT NULL CHECK(typeof(value) = 'integer' AND value >= 0),
                        PRIMARY KEY(uuid, day, stat)
                    )""",
                    "CREATE INDEX player_daily_period ON player_daily_stats(day, stat)",
                    """CREATE TABLE player_sessions (
                        id TEXT PRIMARY KEY, uuid TEXT NOT NULL REFERENCES players(uuid),
                        started_ms INTEGER NOT NULL, checkpoint_ms INTEGER NOT NULL,
                        duration_ms INTEGER NOT NULL CHECK(duration_ms >= 0), ended_ms INTEGER,
                        interrupted INTEGER NOT NULL DEFAULT 0
                    )""",
                    "CREATE INDEX player_sessions_end ON player_sessions(ended_ms)",
                    "CREATE TABLE applied_batches (id TEXT PRIMARY KEY, applied_ms INTEGER NOT NULL)",
                    "PRAGMA user_version = 1",
                ).forEach(statement::execute)
            }
        }
        transaction {
            connection.createStatement().use { statement ->
                listOf(
                    """CREATE TABLE player_breakdown_totals (
                        uuid TEXT NOT NULL REFERENCES players(uuid), category TEXT NOT NULL, subject TEXT NOT NULL,
                        value INTEGER NOT NULL CHECK(typeof(value) = 'integer' AND value >= 0),
                        PRIMARY KEY(uuid, category, subject)
                    )""",
                    "CREATE INDEX player_breakdown_ranking ON player_breakdown_totals(uuid, category, value DESC, subject)",
                    """CREATE TABLE player_breakdown_daily (
                        uuid TEXT NOT NULL REFERENCES players(uuid), day TEXT NOT NULL, category TEXT NOT NULL, subject TEXT NOT NULL,
                        value INTEGER NOT NULL CHECK(typeof(value) = 'integer' AND value >= 0),
                        PRIMARY KEY(uuid, day, category, subject)
                    )""",
                    "CREATE INDEX player_breakdown_period ON player_breakdown_daily(day, category)",
                    "PRAGMA user_version = 2",
                ).forEach(statement::execute)
            }
        }
    }

    /** Batch ID and increments commit together: retries after an ambiguous commit cannot double-count. */
    fun apply(batch: WriteBatch, now: Instant = Instant.now()) = transaction {
        val inserted = connection.prepareStatement("INSERT OR IGNORE INTO applied_batches VALUES (?, ?)").use {
            it.setString(1, batch.id.toString())
            it.setLong(2, now.toEpochMilli())
            it.executeUpdate() == 1
        }
        if (!inserted) return@transaction

        for (player in batch.players) {
            val fresh = connection.prepareStatement(
                "INSERT OR IGNORE INTO players(uuid, name, first_seen_ms, last_seen_ms, vanilla_imported) VALUES (?, ?, ?, ?, ?)"
            ).use {
                it.setString(1, player.uuid.toString())
                it.setString(2, player.name)
                it.setLong(3, player.firstSeenMillis)
                it.setLong(4, player.lastSeenMillis)
                it.setInt(5, if (player.vanillaBaseline.isEmpty() && player.breakdownBaseline.isEmpty()) 0 else 1)
                it.executeUpdate() == 1
            }
            connection.prepareStatement(
                "UPDATE players SET name = ?, first_seen_ms = MIN(first_seen_ms, ?), last_seen_ms = MAX(last_seen_ms, ?) WHERE uuid = ?"
            ).use {
                it.setString(1, player.name)
                it.setLong(2, player.firstSeenMillis)
                it.setLong(3, player.lastSeenMillis)
                it.setString(4, player.uuid.toString())
                it.executeUpdate()
            }
            // Only a genuinely new record can receive a baseline. Enabling import later cannot
            // add vanilla totals on top of statistics already tracked by this module.
            if (fresh) player.vanillaBaseline.forEach { (stat, value) ->
                require(value >= 0)
                addTotal(player.uuid, stat, value)
            }
            if (fresh) player.breakdownBaseline.forEach { (key, value) ->
                require(value >= 0)
                addBreakdown(player.uuid, key, value)
            }
        }

        for ((key, value) in batch.deltas) {
            require(value >= 0)
            addTotal(key.player, key.stat, value)
            connection.prepareStatement(
                """INSERT INTO player_daily_stats(uuid, day, stat, value) VALUES (?, ?, ?, ?)
                    ON CONFLICT(uuid, day, stat) DO UPDATE SET value = value + excluded.value"""
            ).use {
                it.setString(1, key.player.toString())
                it.setString(2, key.day.toString())
                it.setString(3, key.stat.name)
                it.setLong(4, value)
                it.executeUpdate()
            }
        }
        for ((key, value) in batch.breakdowns) {
            require(value >= 0)
            addBreakdown(key.player, key.breakdown, value)
            connection.prepareStatement(
                """INSERT INTO player_breakdown_daily(uuid, day, category, subject, value) VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT(uuid, day, category, subject) DO UPDATE SET value = value + excluded.value"""
            ).use {
                it.setString(1, key.player.toString())
                it.setString(2, key.day.toString())
                it.setString(3, key.breakdown.category.name)
                it.setString(4, key.breakdown.subject)
                it.setLong(5, value)
                it.executeUpdate()
            }
        }
        for (session in batch.sessions) {
            connection.prepareStatement(
                """INSERT INTO player_sessions(id, uuid, started_ms, checkpoint_ms, duration_ms, ended_ms)
                    VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET
                    checkpoint_ms = excluded.checkpoint_ms, duration_ms = excluded.duration_ms, ended_ms = excluded.ended_ms"""
            ).use {
                it.setString(1, session.id.toString())
                it.setString(2, session.player.toString())
                it.setLong(3, session.startedMillis)
                it.setLong(4, session.checkpointMillis)
                it.setLong(5, session.durationMillis)
                if (session.ended) it.setLong(6, session.checkpointMillis) else it.setNull(6, java.sql.Types.BIGINT)
                it.executeUpdate()
            }
        }
    }

    private fun addTotal(player: UUID, stat: PlayerStat, value: Long) {
        connection.prepareStatement(
            """INSERT INTO player_totals(uuid, stat, value) VALUES (?, ?, ?)
                ON CONFLICT(uuid, stat) DO UPDATE SET value = value + excluded.value"""
        ).use {
            it.setString(1, player.toString())
            it.setString(2, stat.name)
            it.setLong(3, value)
            it.executeUpdate()
        }
    }

    private fun addBreakdown(player: UUID, key: BreakdownKey, value: Long) {
        connection.prepareStatement(
            """INSERT INTO player_breakdown_totals(uuid, category, subject, value) VALUES (?, ?, ?, ?)
                ON CONFLICT(uuid, category, subject) DO UPDATE SET value = value + excluded.value"""
        ).use {
            it.setString(1, player.toString())
            it.setString(2, key.category.name)
            it.setString(3, key.subject)
            it.setLong(4, value)
            it.executeUpdate()
        }
    }

    fun snapshot(
        leaderboardSize: Int,
        publishLeaderboards: Boolean,
        now: Instant = Instant.now(),
        publishFavorites: Boolean = false,
        favoriteSize: Int = 10,
        favoritePlayerLimit: Int = 25,
    ): DatabaseSnapshot {
        require(leaderboardSize in 1..100) { "Leaderboard size must be between 1 and 100" }
        require(favoriteSize in 1..100 && favoritePlayerLimit in 1..100) { "Favorite limits must be between 1 and 100" }
        val totals = mutableMapOf<PlayerStat, Long>()
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT stat, SUM(value) FROM player_totals GROUP BY stat").use {
                while (it.next()) totals[PlayerStat.valueOf(it.getString(1))] = it.getLong(2)
            }
        }
        val rankings = if (!publishLeaderboards) emptyMap() else PlayerStat.entries.associateWith { stat ->
            connection.prepareStatement(
                """SELECT t.uuid, p.name, t.value FROM player_totals t JOIN players p ON p.uuid = t.uuid
                    WHERE stat = ? AND value > 0 ORDER BY value DESC, t.uuid LIMIT ?"""
            ).use { statement ->
                statement.setString(1, stat.name)
                statement.setInt(2, leaderboardSize)
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) add(LeaderboardEntry(UUID.fromString(result.getString(1)), result.getString(2), result.getLong(3)))
                    }
                }
            }
        }
        val dayStart = now.atOffset(ZoneOffset.UTC).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val monthStart = now.atOffset(ZoneOffset.UTC).toLocalDate().withDayOfMonth(1)
            .atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val favorites = if (publishFavorites) favorites(favoriteSize, favoritePlayerLimit) else emptyList()
        connection.prepareStatement(
            "SELECT COUNT(*), COALESCE(SUM(last_seen_ms >= ?), 0), COALESCE(SUM(last_seen_ms >= ?), 0) FROM players"
        ).use { statement ->
            statement.setLong(1, dayStart)
            statement.setLong(2, monthStart)
            statement.executeQuery().use {
                it.next()
                return DatabaseSnapshot(totals, rankings, it.getLong(1), it.getLong(2), it.getLong(3), now.toEpochMilli() / 1000.0, favorites)
            }
        }
    }

    private fun favorites(size: Int, playerLimit: Int): List<FavoriteEntry> {
        val players = connection.prepareStatement("SELECT uuid, name FROM players ORDER BY last_seen_ms DESC, uuid LIMIT ?").use {
            it.setInt(1, playerLimit)
            it.executeQuery().use { result ->
                buildList { while (result.next()) add(UUID.fromString(result.getString(1)) to result.getString(2)) }
            }
        }
        return buildList {
            connection.prepareStatement(
                """SELECT subject, value FROM player_breakdown_totals
                    WHERE uuid = ? AND category = ? AND value > 0 ORDER BY value DESC, subject LIMIT ?"""
            ).use { statement ->
                players.forEach { (uuid, name) ->
                    BreakdownCategory.entries.forEach { category ->
                        statement.setString(1, uuid.toString())
                        statement.setString(2, category.name)
                        statement.setInt(3, size)
                        statement.executeQuery().use { result ->
                            var rank = 0
                            while (result.next()) add(FavoriteEntry(uuid, name, category, result.getString(1), ++rank, result.getLong(2)))
                        }
                    }
                }
            }
        }
    }

    fun prune(dailyDays: Int, sessionDays: Int, now: Instant = Instant.now()) = transaction {
        if (dailyDays > 0) connection.prepareStatement("DELETE FROM player_daily_stats WHERE day < ?").use {
            it.setString(1, now.atOffset(ZoneOffset.UTC).toLocalDate().minusDays(dailyDays.toLong() - 1).toString())
            it.executeUpdate()
        }
        if (dailyDays > 0) connection.prepareStatement("DELETE FROM player_breakdown_daily WHERE day < ?").use {
            it.setString(1, now.atOffset(ZoneOffset.UTC).toLocalDate().minusDays(dailyDays.toLong() - 1).toString())
            it.executeUpdate()
        }
        if (sessionDays > 0) connection.prepareStatement("DELETE FROM player_sessions WHERE ended_ms < ?").use {
            it.setLong(1, now.minusSeconds(sessionDays * 86400L).toEpochMilli())
            it.executeUpdate()
        }
        // One ordered writer retries only its current batch, well inside this deduplication window.
        connection.prepareStatement("DELETE FROM applied_batches WHERE applied_ms < ?").use {
            it.setLong(1, now.minusSeconds(7 * 86400L).toEpochMilli())
            it.executeUpdate()
        }
    }

    private fun <T> transaction(action: () -> T): T {
        connection.autoCommit = false
        try {
            val result = action()
            connection.commit()
            return result
        } catch (failure: Throwable) {
            runCatching { connection.rollback() }.exceptionOrNull()?.let(failure::addSuppressed)
            throw failure
        } finally {
            connection.autoCommit = true
        }
    }

    override fun close() = connection.close()
}
