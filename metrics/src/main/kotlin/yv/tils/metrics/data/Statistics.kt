/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

/** Integer storage units avoid rounding errors in accumulated values. Export in base SI units. */
enum class PlayerStat(val metric: String, val help: String, val divisor: Double = 1.0) {
    PLAYTIME("playtime_seconds", "Accumulated online player time in seconds, including AFK time.", 1000.0),
    JOINS("joins", "Player sessions started while collection was active."),
    DEATHS("deaths", "Player deaths."),
    MOB_KILLS("mob_kills", "Non-player mobs killed, following vanilla statistics."),
    MONSTER_KILLS("monster_kills", "Enemy-class mobs killed, following vanilla entity-kill statistics."),
    ANIMAL_KILLS("animal_kills", "Non-enemy Mob-class entities killed, including animals, aquatic mobs, golems and villagers."),
    PLAYER_KILLS("player_kills", "Players killed, following vanilla statistics."),
    BLOCKS_BROKEN("blocks_broken", "Blocks mined, following vanilla statistics."),
    BLOCKS_PLACED("blocks_placed", "Blocks placed in successful block placement events."),
    DISTANCE("distance_meters", "Combined vanilla movement distance, excluding teleports.", 100.0),
    WALK_DISTANCE("walk_distance_meters", "Walking, sprinting and crouching distance.", 100.0),
    SWIM_DISTANCE("swim_distance_meters", "Swimming and walking in water distance.", 100.0),
    FLY_DISTANCE("fly_distance_meters", "Flight and elytra distance.", 100.0),
    VEHICLE_DISTANCE("vehicle_distance_meters", "Distance travelled in vehicles and on mounts.", 100.0),
    OTHER_DISTANCE("other_distance_meters", "Climbing distance.", 100.0),
    FALL_DISTANCE("fall_distance_meters", "Vertical distance fallen; excluded from combined travel distance.", 100.0),
    TOTEMS_USED("totems_used", "Totems consumed, following vanilla item-use statistics."),
    DAMAGE_DEALT("damage_dealt_health_points", "Vanilla damage dealt in health points; two points equal one heart.", 10.0),
    DAMAGE_TAKEN("damage_taken_health_points", "Vanilla damage taken in health points; two points equal one heart.", 10.0),
    FOOD_EATEN("food_eaten", "Edible items consumed, excluding cake slices."),
    ITEMS_CRAFTED("items_crafted", "Items crafted, following vanilla item statistics."),
    FISH_CAUGHT("fish_caught", "Fish caught, following vanilla statistics."),
    JUMPS("jumps", "Player jumps, following vanilla statistics."),
    ITEMS_PICKED_UP("items_picked_up", "Individual items picked up, not stack or pickup-event count."),
    ITEMS_DROPPED("items_dropped", "Individual items dropped, not stack or drop-event count."),
    RAIDS_WON("raids_won", "Raid victories credited by vanilla."),
    ANIMALS_BRED("animals_bred", "Animal breeding credited by vanilla."),
    VILLAGER_TRADES("villager_trades", "Trades with villagers credited by vanilla."),
    CHESTS_OPENED("chests_opened", "Vanilla chest-open count; barrels and ender chests are separate vanilla statistics and excluded."),
    ITEMS_ENCHANTED("items_enchanted", "Items enchanted, following vanilla statistics."),
    BELLS_RUNG("bells_rung", "Bells rung, following vanilla statistics."),
    SNEAK_TIME("sneak_seconds", "Vanilla sneaking time in seconds; native ticks are converted at 20 TPS.", 1000.0),
    AFK_TIME("afk_seconds", "Estimated time beyond the configured Paper idle-duration threshold while observed by this module.", 1000.0),
    ADVANCEMENTS("advancements", "Non-recipe advancements completed while collection was active."),
    DIMENSION_CHANGES("dimension_changes", "Player changes between worlds while collection was active."),
}

enum class BreakdownCategory(val metric: String) {
    BLOCKS_BROKEN("blocks_broken"),
    BLOCKS_PLACED("blocks_placed"),
    MOBS_KILLED("mobs_killed"),
    ANIMALS_KILLED("animals_killed"),
}

data class BreakdownKey(val category: BreakdownCategory, val subject: String) {
    companion object {
        private val SUBJECT_PATTERN = Regex("[a-z0-9_.-]+:[a-z0-9/._-]+")
    }
    init {
        // Only registry keys, never player-provided free text or dynamic metric names.
        require(subject.matches(SUBJECT_PATTERN)) { "Invalid statistics registry key: $subject" }
    }
}

data class PlayerIdentity(
    val uuid: UUID,
    val name: String,
    val firstSeenMillis: Long,
    val lastSeenMillis: Long,
    val vanillaBaseline: Map<PlayerStat, Long> = emptyMap(),
    val breakdownBaseline: Map<BreakdownKey, Long> = emptyMap(),
)

data class DeltaKey(val player: UUID, val stat: PlayerStat, val day: LocalDate)
data class BreakdownDeltaKey(val player: UUID, val breakdown: BreakdownKey, val day: LocalDate)
data class SessionRecord(
    val id: UUID,
    val player: UUID,
    val startedMillis: Long,
    val checkpointMillis: Long,
    val durationMillis: Long,
    val ended: Boolean = false,
)

data class WriteBatch(
    val id: UUID = UUID.randomUUID(),
    val players: List<PlayerIdentity>,
    val deltas: Map<DeltaKey, Long>,
    val sessions: List<SessionRecord>,
    val breakdowns: Map<BreakdownDeltaKey, Long> = emptyMap(),
)

data class LeaderboardEntry(val player: UUID, val name: String, val value: Long)
data class FavoriteEntry(val player: UUID, val name: String, val category: BreakdownCategory, val subject: String, val rank: Int, val value: Long)
data class DatabaseSnapshot(
    val totals: Map<PlayerStat, Long> = emptyMap(),
    val leaderboards: Map<PlayerStat, List<LeaderboardEntry>> = emptyMap(),
    val players: Long = 0,
    val dailyPlayers: Long = 0,
    val monthlyPlayers: Long = 0,
    val updatedSeconds: Double = 0.0,
    val favorites: List<FavoriteEntry> = emptyList(),
)

/** Shared only by the server-thread producer and the single database worker. No Bukkit references. */
class WriteBuffer(private val maxEntries: Int = 100_000) {
    @Volatile var saturated: Boolean = false
        private set
    private val players = mutableMapOf<UUID, PlayerIdentity>()
    private val deltas = mutableMapOf<DeltaKey, Long>()
    private val sessions = mutableMapOf<UUID, SessionRecord>()
    private val breakdowns = mutableMapOf<BreakdownDeltaKey, Long>()
    private var baselineEntries = 0

    @Synchronized
    fun identity(identity: PlayerIdentity) {
        val previous = players[identity.uuid]
        val merged = identity.copy(
            firstSeenMillis = minOf(previous?.firstSeenMillis ?: identity.firstSeenMillis, identity.firstSeenMillis),
            vanillaBaseline = previous?.vanillaBaseline?.takeIf { it.isNotEmpty() } ?: identity.vanillaBaseline,
            breakdownBaseline = previous?.breakdownBaseline?.takeIf { it.isNotEmpty() } ?: identity.breakdownBaseline,
        )
        val additional = merged.vanillaBaseline.size + merged.breakdownBaseline.size -
            (previous?.vanillaBaseline?.size ?: 0) - (previous?.breakdownBaseline?.size ?: 0)
        if (!canRecord(previous != null, additional)) return
        baselineEntries += additional
        players[identity.uuid] = merged
    }

    @Synchronized
    fun breakdown(player: UUID, key: BreakdownKey, value: Long, at: Instant) {
        require(value >= 0) { "Statistics cannot decrease" }
        if (value == 0L) return
        val delta = BreakdownDeltaKey(player, key, at.atOffset(ZoneOffset.UTC).toLocalDate())
        if (!canRecord(breakdowns.containsKey(delta))) return
        breakdowns[delta] = Math.addExact(breakdowns[delta] ?: 0L, value)
    }

    @Synchronized
    fun increment(player: UUID, stat: PlayerStat, value: Long, at: Instant) {
        require(value >= 0) { "Statistics cannot decrease" }
        if (value == 0L) return
        val key = DeltaKey(player, stat, at.atOffset(ZoneOffset.UTC).toLocalDate())
        if (!canRecord(deltas.containsKey(key))) return
        deltas[key] = Math.addExact(deltas[key] ?: 0L, value)
    }

    /** Split elapsed playtime at UTC midnight instead of assigning the whole sample to its end day. */
    @Synchronized
    fun playtime(player: UUID, from: Instant, to: Instant, millis: Long) {
        duration(player, PlayerStat.PLAYTIME, from, to, millis)
    }

    @Synchronized
    fun duration(player: UUID, stat: PlayerStat, from: Instant, to: Instant, millis: Long) {
        require(millis >= 0)
        if (millis == 0L) return
        if (!to.isAfter(from)) {
            increment(player, stat, millis, to)
            return
        }
        val wallMillis = java.time.Duration.between(from, to).toMillis().coerceAtLeast(1)
        var cursor = from
        var allocated = 0L
        while (cursor.isBefore(to)) {
            val nextDay = cursor.atOffset(ZoneOffset.UTC).toLocalDate().plusDays(1)
                .atStartOfDay().toInstant(ZoneOffset.UTC)
            val end = minOf(nextDay, to)
            val portion = if (end == to) millis - allocated else
                (millis.toDouble() * java.time.Duration.between(cursor, end).toMillis() / wallMillis).toLong()
            increment(player, stat, portion, cursor)
            allocated += portion
            cursor = end
        }
    }

    @Synchronized
    fun session(session: SessionRecord) {
        if (!canRecord(sessions.containsKey(session.id))) return
        sessions[session.id] = session
    }

    private fun canRecord(existing: Boolean, additional: Int = 0): Boolean {
        if (saturated) return false
        if (players.size + deltas.size + sessions.size + breakdowns.size + baselineEntries + additional + (if (existing) 0 else 1) > maxEntries) {
            saturated = true
            return false
        }
        return true
    }

    @Synchronized
    fun drain(): WriteBatch? {
        if (players.isEmpty() && deltas.isEmpty() && sessions.isEmpty() && breakdowns.isEmpty()) return null
        return WriteBatch(players = players.values.toList(), deltas = deltas.toMap(), sessions = sessions.values.toList(), breakdowns = breakdowns.toMap())
            .also { players.clear(); deltas.clear(); sessions.clear(); breakdowns.clear(); baselineEntries = 0 }
    }
}
