/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.collection

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockMultiPlaceEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerStatisticIncrementEvent
import org.bukkit.plugin.java.JavaPlugin
import yv.tils.metrics.configs.MetricsConfig
import yv.tils.metrics.data.PlayerIdentity
import yv.tils.metrics.data.BreakdownCategory
import yv.tils.metrics.data.BreakdownKey
import yv.tils.metrics.data.PlayerStat
import yv.tils.metrics.data.SessionRecord
import yv.tils.metrics.data.WriteBuffer
import yv.tils.metrics.export.PrometheusMetrics
import yv.tils.metrics.export.WorldSnapshot
import yv.tils.utils.logger.Logger
import yv.tils.utils.server.ServerUtils
import java.time.Instant
import java.util.UUID

/** All methods run on Paper's server thread. No file, SQL, network or full-world entity scans. */
class PlayerCollector(
    private val plugin: JavaPlugin,
    private val config: MetricsConfig,
    private val buffer: WriteBuffer,
    private val metrics: PrometheusMetrics,
    private val requestFlush: () -> Unit,
) : Listener {
    private data class OnlineSession(
        val id: UUID = UUID.randomUUID(),
        val started: Instant,
        var lastInstant: Instant,
        var lastNano: Long,
        var durationMillis: Long = 0,
        var statistics: Map<Statistic, Int>,
    )

    private val online = mutableMapOf<UUID, OnlineSession>()
    private var maxTickSeconds = 0.0
    private var reportedSaturation = false

    private fun collecting() = config.collectPlayerStats && !buffer.saturated

    fun start() {
        metrics.versions = plugin.server.minecraftVersion to plugin.server.bukkitVersion
        if (collecting()) plugin.server.onlinePlayers.forEach(::join)
        sample()
    }

    private fun join(player: Player) {
        if (!collecting() || online.containsKey(player.uniqueId)) return
        val now = Instant.now()
        val vanilla = if (config.collectGameplay) VanillaStatistics.snapshot(player) else emptyMap()
        val baseline = if (config.importVanillaOnFirstJoin)
            VanillaStatistics.baseline(player, config.collectGameplay, vanilla, config.collectBreakdowns) else VanillaStatistics.Baseline()
        online[player.uniqueId] = OnlineSession(started = now, lastInstant = now, lastNano = System.nanoTime(), statistics = vanilla)
        buffer.identity(PlayerIdentity(player.uniqueId, player.name, now.toEpochMilli(), now.toEpochMilli(), baseline.totals, baseline.breakdowns))
        buffer.increment(player.uniqueId, PlayerStat.JOINS, 1, now)
    }

    private fun checkpoint(player: Player, ended: Boolean = false) {
        if (!collecting()) return
        val session = online[player.uniqueId] ?: return
        val now = Instant.now()
        val nano = System.nanoTime()
        val elapsedMillis = ((nano - session.lastNano) / 1_000_000).coerceAtLeast(0)
        buffer.playtime(player.uniqueId, session.lastInstant, now, elapsedMillis)
        if (config.trackAfk) {
            val afkMillis = AfkTime.millis(elapsedMillis, player.idleDuration.toMillis(), config.afkThresholdSeconds * 1000)
            buffer.duration(player.uniqueId, PlayerStat.AFK_TIME, now.minusMillis(afkMillis), now, afkMillis)
        }
        session.lastNano += elapsedMillis * 1_000_000
        session.lastInstant = now
        session.durationMillis += elapsedMillis
        if (config.collectGameplay) {
            val current = VanillaStatistics.snapshot(player)
            current.forEach { (vanilla, value) ->
                // Vanilla resets are rebased, never subtracted from durable lifetime counters.
                val delta = VanillaStatistics.storageValue(vanilla, (value.toLong() - (session.statistics[vanilla] ?: value)).coerceAtLeast(0))
                val stat = VanillaStatistics.sampled.getValue(vanilla)
                buffer.increment(player.uniqueId, stat, delta, now)
                if (stat in VanillaStatistics.distanceStats) buffer.increment(player.uniqueId, PlayerStat.DISTANCE, delta, now)
            }
            session.statistics = current
        }
        buffer.identity(PlayerIdentity(player.uniqueId, player.name, session.started.toEpochMilli(), now.toEpochMilli()))
        buffer.session(SessionRecord(session.id, player.uniqueId, session.started.toEpochMilli(), now.toEpochMilli(), session.durationMillis, ended))
        if (ended) {
            metrics.sessions.observe(session.durationMillis / 1000.0)
            online.remove(player.uniqueId)
        }
    }

    fun sample() {
        val server = plugin.server
        val players = server.onlinePlayers
        if (buffer.saturated && !reportedSaturation) {
            reportedSaturation = true
            metrics.ready.set(0.0)
            Logger.error("[Metrics] Pending-record limit reached. Player collection is paused until restart; further activity will not be recorded. Restore database availability before restarting.")
        }
        if (collecting()) players.forEach { player ->
            join(player)
            checkpoint(player)
            metrics.ping.observe(player.ping.coerceAtLeast(0) / 1000.0)
        }
        metrics.online.set(players.size.toDouble())
        metrics.capacity.set(server.maxPlayers.toDouble())
        metrics.maintenance.set(if (ServerUtils.serverInMaintenance) 1.0 else 0.0)
        server.tps.forEachIndexed { index, value ->
            metrics.tps.labelValues(listOf("1m", "5m", "15m")[index]).set(value)
        }
        metrics.tickAverage.set(server.averageTickTime / 1000.0)
        metrics.tickMax.set(maxTickSeconds)
        maxTickSeconds = 0.0
        metrics.worlds = server.worlds.map {
            WorldSnapshot(it.name, it.chunkCount, it.entityCount, it.tileEntityCount, it.playerCount)
        }
        metrics.gameModes = players.groupingBy { it.gameMode.name.lowercase() }.eachCount()
        if (collecting()) metrics.highPing.set(players.count { it.ping > 250 }.toDouble())
        metrics.afk.set(if (collecting() && config.trackAfk)
            players.count { it.idleDuration.toMillis() >= config.afkThresholdSeconds * 1000 }.toDouble() else 0.0)
    }

    fun stop() {
        try {
            plugin.server.onlinePlayers.forEach { checkpoint(it, ended = true) }
        } finally {
            online.clear()
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onTick(event: ServerTickEndEvent) {
        val seconds = event.tickDuration / 1000.0
        if (seconds.isFinite() && seconds >= 0) {
            metrics.tickDuration.observe(seconds)
            maxTickSeconds = maxOf(maxTickSeconds, seconds)
            listOf(0.05, 0.1, 0.25).forEach { threshold ->
                if (seconds > threshold) metrics.slowTicks.labelValues(threshold.toString()).inc()
            }
        }
        metrics.heartbeat.set(System.currentTimeMillis() / 1000.0)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) = join(event.player)

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        if (!collecting()) return
        checkpoint(event.player, ended = true)
        metrics.departures.inc()
        requestFlush()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onKick(event: PlayerKickEvent) {
        if (collecting()) metrics.kicks.labelValues(event.cause.name.lowercase()).inc()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onStatistic(event: PlayerStatisticIncrementEvent) {
        if (!collecting() || !config.collectGameplay || event.player.uniqueId !in online) return
        val delta = (event.newValue.toLong() - event.previousValue).coerceAtLeast(0)
        val now = Instant.now()
        if (event.statistic == Statistic.KILL_ENTITY) {
            val category = VanillaStatistics.killCategory(event.entityType) ?: return
            buffer.increment(event.player.uniqueId, category.stat, delta, now)
            if (config.collectBreakdowns) buffer.breakdown(event.player.uniqueId,
                BreakdownKey(category.breakdown, event.entityType!!.key.toString()), delta, now)
            return
        }
        val stat = VanillaStatistics.materialStat(event.statistic, event.material) ?: return
        buffer.increment(event.player.uniqueId, stat, delta, now)
        if (config.collectBreakdowns && event.statistic == Statistic.MINE_BLOCK) event.material?.let {
            buffer.breakdown(event.player.uniqueId, BreakdownKey(BreakdownCategory.BLOCKS_BROKEN, it.key.toString()), delta, now)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlace(event: BlockPlaceEvent) {
        if (!collecting() || !config.collectGameplay || !event.canBuild() || event.player.uniqueId !in online) return
        val now = Instant.now()
        val count = if (event is BlockMultiPlaceEvent) event.replacedBlockStates.size.toLong() else 1L
        buffer.increment(event.player.uniqueId, PlayerStat.BLOCKS_PLACED, count, now)
        if (config.collectBreakdowns) {
            val types = if (event is BlockMultiPlaceEvent) event.replacedBlockStates.map { it.block.type } else listOf(event.blockPlaced.type)
            types.groupingBy { it }.eachCount().forEach { (type, amount) ->
                buffer.breakdown(event.player.uniqueId, BreakdownKey(BreakdownCategory.BLOCKS_PLACED, type.key.toString()), amount.toLong(), now)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onAdvancement(event: PlayerAdvancementDoneEvent) {
        if (!collecting() || !config.collectGameplay || event.player.uniqueId !in online) return
        if (!event.advancement.key.key.startsWith("recipes/"))
            buffer.increment(event.player.uniqueId, PlayerStat.ADVANCEMENTS, 1, Instant.now())
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        if (collecting() && config.collectGameplay && event.player.uniqueId in online)
            buffer.increment(event.player.uniqueId, PlayerStat.DIMENSION_CHANGES, 1, Instant.now())
    }
}
