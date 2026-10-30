/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.export

import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.core.metrics.CounterWithCallback
import io.prometheus.metrics.core.metrics.Gauge
import io.prometheus.metrics.core.metrics.GaugeWithCallback
import io.prometheus.metrics.core.metrics.Histogram
import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter
import io.prometheus.metrics.model.registry.PrometheusRegistry
import yv.tils.metrics.data.DatabaseSnapshot
import yv.tils.metrics.data.PlayerStat
import java.io.ByteArrayOutputStream
import java.lang.management.ManagementFactory

data class WorldSnapshot(val name: String, val chunks: Int, val entities: Int, val blockEntities: Int, val players: Int)

/** Registry is module-local, never the global default registry. All scrape callbacks are memory-only. */
class PrometheusMetrics(publishLeaderboards: Boolean, publishFavorites: Boolean = false) {
    val registry = PrometheusRegistry()
    @Volatile var database = DatabaseSnapshot()
    @Volatile var worlds: List<WorldSnapshot> = emptyList()
    @Volatile var gameModes: Map<String, Int> = emptyMap()
    @Volatile var versions: Pair<String, String>? = null
    @Volatile private var gcCollections: Long = 0
    @Volatile private var gcTimeSeconds: Double = 0.0

    val ready = gauge("collection_ready", "One when SQLite is initialized and collection is active.")
    val storageHealthy = gauge("storage_healthy", "One when the latest SQLite write/read succeeded.")
    val lastFlush = gauge("storage_last_success_seconds", "Unix time of the last successful database refresh.")
    val heartbeat = gauge("tick_heartbeat_seconds", "Unix time of the latest completed server tick.")
    val online = gauge("players_online", "Current online players, including hidden players; this endpoint is private.")
    val capacity = gauge("players_capacity", "Configured maximum online players.")
    val maintenance = gauge("maintenance", "One when the shared server maintenance flag is active.")
    val tickAverage = gauge("tick_duration_average_seconds", "Paper average tick duration in seconds.")
    val tickMax = gauge("tick_duration_max_seconds", "Maximum tick duration since the previous server sample.")
    val tps = Gauge.builder().name("yvtils_server_tps").help("Paper TPS moving average.")
        .labelNames("window").register(registry)
    val tickDuration = Histogram.builder().name("yvtils_server_tick_duration_seconds")
        .help("Completed server tick durations; resets on process/module restart.")
        .classicUpperBounds(0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 5.0).register(registry)
    val slowTicks = Counter.builder().name("yvtils_server_slow_ticks_total").help("Ticks exceeding the labelled duration.")
        .labelNames("threshold_seconds").register(registry)
    val sessions = Histogram.builder().name("yvtils_player_session_duration_seconds")
        .help("Completed session durations observed since module start; shutdown sessions are included.")
        .classicUpperBounds(60.0, 300.0, 900.0, 1800.0, 3600.0, 7200.0, 14400.0).register(registry)
    val departures = Counter.builder().name("yvtils_player_departures_total")
        .help("Player departures observed since module start, excluding module shutdown.").register(registry)
    val kicks = Counter.builder().name("yvtils_player_kicks_total").help("Non-cancelled kicks by Paper cause, not free-text reasons.")
        .labelNames("cause").register(registry)
    val ping = Histogram.builder().name("yvtils_player_ping_seconds")
        .help("Online-player ping observations at each sample, not one observation per session.")
        .classicUpperBounds(0.025, 0.05, 0.1, 0.15, 0.25, 0.5, 1.0).register(registry)
    val highPing = gauge("players_high_ping", "Online players with ping greater than 250 milliseconds.")
    val afk = gauge("players_afk", "Online players beyond the configured Paper idle-duration threshold.")
    private val heap = Gauge.builder().name("yvtils_jvm_heap_bytes").help("JVM heap memory in bytes.")
        .labelNames("kind").register(registry)
    private val cpu = gauge("process_cpu_ratio", "Recent process CPU usage divided by available CPU capacity; NaN when unavailable.")
    private val uptime = gauge("process_uptime_seconds", "JVM uptime in seconds.")
    private val start = gauge("process_start_seconds", "JVM process start as a Unix timestamp.")
    @Volatile private var bytes = "# Metrics initializing\n".toByteArray()

    init {
        CounterWithCallback.builder().name("yvtils_jvm_gc_collections_total").help("JVM garbage collections since process start.")
            .callback { it.call(gcCollections.toDouble()) }.register(registry)
        CounterWithCallback.builder().name("yvtils_jvm_gc_seconds_total").help("JVM garbage collection time in seconds since process start.")
            .callback { it.call(gcTimeSeconds) }.register(registry)
        GaugeWithCallback.builder().name("yvtils_server_version_info").help("Minecraft and Paper API version information.")
            .labelNames("minecraft", "paper_api").callback { callback ->
                versions?.let { callback.call(1.0, it.first, it.second) }
            }.register(registry)
        PlayerStat.entries.forEach { stat ->
            CounterWithCallback.builder().name("yvtils_players_${stat.metric}_total").help(stat.help)
                .callback { it.call((database.totals[stat] ?: 0L) / stat.divisor) }.register(registry)
        }
        GaugeWithCallback.builder().name("yvtils_players_unique").help("Unique players recorded by this module; UTC calendar periods.")
            .labelNames("period").callback {
                val snapshot = database
                it.call(snapshot.players.toDouble(), "all_time")
                it.call(snapshot.dailyPlayers.toDouble(), "day")
                it.call(snapshot.monthlyPlayers.toDouble(), "month")
            }.register(registry)
        if (publishLeaderboards) GaugeWithCallback.builder().name("yvtils_player_leaderboard_value")
            .help("Top-N player totals in base units; stat identifies units. Opt-in, identifying player data.")
            .labelNames("stat", "rank", "player_uuid", "player_name").callback { callback ->
                database.leaderboards.forEach { (stat, entries) ->
                    entries.forEachIndexed { index, entry ->
                        callback.call(entry.value / stat.divisor, stat.metric, (index + 1).toString(), entry.player.toString(), entry.name)
                    }
                }
            }.register(registry)
        if (publishFavorites) GaugeWithCallback.builder().name("yvtils_player_favorite_value")
            .help("Bounded per-player material/entity Favorites; explicit identifying publication opt-in.")
            .labelNames("category", "subject", "rank", "player_uuid", "player_name").callback { callback ->
                database.favorites.forEach {
                    callback.call(it.value.toDouble(), it.category.metric, it.subject, it.rank.toString(), it.player.toString(), it.name)
                }
            }.register(registry)
        mapOf(
            "chunks_loaded" to { world: WorldSnapshot -> world.chunks },
            "entities_loaded" to { world: WorldSnapshot -> world.entities },
            "block_entities_loaded" to { world: WorldSnapshot -> world.blockEntities },
            "players_online" to { world: WorldSnapshot -> world.players },
        ).forEach { (name, value) ->
            GaugeWithCallback.builder().name("yvtils_world_$name").help("Current $name by loaded world.")
                .labelNames("world").callback { callback -> worlds.forEach { callback.call(value(it).toDouble(), it.name) } }
                .register(registry)
        }
        GaugeWithCallback.builder().name("yvtils_players_by_gamemode").help("Current online players by game mode.")
            .labelNames("gamemode").callback { callback -> gameModes.forEach { (mode, count) -> callback.call(count.toDouble(), mode) } }
            .register(registry)
    }

    private fun gauge(name: String, help: String): Gauge =
        Gauge.builder().name("yvtils_$name").help(help).register(registry)

    /** Paper returns 1m/5m/15m; Purpur 26.3 prepends a 5s average. Keep the exported windows stable. */
    fun sampleTps(values: DoubleArray) {
        val offset = when (values.size) {
            3 -> 0
            4 -> 1
            else -> {
                // Do not guess window labels for an unknown layout or retain stale samples.
                tps.clear()
                return
            }
        }
        listOf("1m", "5m", "15m").forEachIndexed { index, window ->
            tps.labelValues(window).set(values[index + offset])
        }
    }

    /** Called on the background worker, never on a scrape or the server tick thread. */
    fun render() {
        val memory = ManagementFactory.getMemoryMXBean().heapMemoryUsage
        heap.labelValues("used").set(memory.used.toDouble())
        heap.labelValues("committed").set(memory.committed.toDouble())
        if (memory.max >= 0) heap.labelValues("max").set(memory.max.toDouble())
        val operatingSystem = ManagementFactory.getOperatingSystemMXBean() as? com.sun.management.OperatingSystemMXBean
        cpu.set(operatingSystem?.processCpuLoad?.takeIf { it >= 0 && it.isFinite() } ?: Double.NaN)
        val collectors = ManagementFactory.getGarbageCollectorMXBeans()
        gcCollections = collectors.sumOf { it.collectionCount.coerceAtLeast(0) }
        gcTimeSeconds = collectors.sumOf { it.collectionTime.coerceAtLeast(0) } / 1000.0
        val runtime = ManagementFactory.getRuntimeMXBean()
        uptime.set(runtime.uptime / 1000.0)
        start.set(runtime.startTime / 1000.0)
        val output = ByteArrayOutputStream()
        PrometheusTextFormatWriter.create().write(output, registry.scrape())
        bytes = output.toByteArray()
    }

    fun body(): ByteArray = bytes
}
