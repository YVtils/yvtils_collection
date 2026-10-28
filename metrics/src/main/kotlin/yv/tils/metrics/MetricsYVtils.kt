/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics

import org.bukkit.event.HandlerList
import org.bukkit.scheduler.BukkitTask
import yv.tils.config.files.ConfigurateFileUtils
import yv.tils.metrics.collection.PlayerCollector
import yv.tils.metrics.configs.MetricsConfig
import yv.tils.metrics.data.WriteBuffer
import yv.tils.metrics.data.DatabaseSnapshot
import yv.tils.metrics.export.MetricsEndpoint
import yv.tils.metrics.export.PrometheusMetrics
import yv.tils.metrics.storage.BatchWriter
import yv.tils.metrics.storage.SqliteStore
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class MetricsYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "metrics", Module.readVersion(MetricsYVtils::class.java, "metrics"),
            "Player/server monitoring and persistent single-server statistics", "YVtils",
            "https://docs.yvtils.net/metrics/",
        )
    }

    private val stopping = AtomicBoolean(false)
    private val flushRequested = AtomicBoolean(false)
    private var worker: ScheduledThreadPoolExecutor? = null
    private var endpoint: MetricsEndpoint? = null
    private var collector: PlayerCollector? = null
    private var sampleTask: BukkitTask? = null
    private var config: MetricsConfig? = null
    // The following two fields are accessed only by the worker, including shutdown.
    private var store: SqliteStore? = null
    private var writer: BatchWriter? = null
    private var lastErrorMillis = 0L
    private var lastPruneDay: java.time.LocalDate? = null

    override fun enablePlugin() {
        val settings = MetricsConfig.load().also { config = it }
        val plugin = Core.instance
        val metrics = PrometheusMetrics(settings.publishLeaderboards, settings.publishFavorites)
        val buffer = WriteBuffer(settings.maxPendingEntries)
        val token = settings.bearerTokenEnvironment.takeIf { it.isNotBlank() }?.let {
            requireNotNull(System.getenv(it)?.takeIf(String::isNotBlank)) { "Missing metrics bearer token environment variable: $it" }
        }
        val path = (ConfigurateFileUtils.baseDirectory ?: ConfigurateFileUtils.defaultBaseDirectory())
            .resolve("metrics/metrics.db").toPath()
        endpoint = MetricsEndpoint(settings.bindAddress, settings.port, token, metrics::body)
        val executor = ScheduledThreadPoolExecutor(1) { runnable ->
            Thread(runnable, "yvtils-metrics-storage").apply { isDaemon = true }
        }.apply {
            removeOnCancelPolicy = true
            setExecuteExistingDelayedTasksAfterShutdownPolicy(false)
            setContinueExistingPeriodicTasksAfterShutdownPolicy(false)
        }
        worker = executor
        Module.addModule(MODULE)
        executor.execute {
            try {
                val database = SqliteStore(path)
                store = database
                writer = BatchWriter(buffer, database::apply)
                refreshDatabase(metrics, settings)
                metrics.render()
                if (!stopping.get()) plugin.server.scheduler.runTask(plugin, Runnable {
                    if (stopping.get()) return@Runnable
                    val listener = PlayerCollector(plugin, settings, buffer, metrics) { requestFlush(metrics, settings) }
                    collector = listener
                    try {
                        plugin.server.pluginManager.registerEvents(listener, plugin)
                        listener.start()
                        sampleTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
                            try {
                                listener.sample()
                            } catch (failure: Exception) {
                                metrics.ready.set(0.0)
                                sampleTask?.cancel()
                                HandlerList.unregisterAll(listener)
                                Logger.error("[Metrics] Player/server sampler failed; collection stopped until restart: ${failure.message}")
                            }
                        }, settings.sampleTicks, settings.sampleTicks)
                        metrics.ready.set(if (buffer.saturated) 0.0 else 1.0)
                        Logger.info("[Metrics] Collection active; SQLite at $path, endpoint ${settings.bindAddress}:${settings.port}/metrics")
                    } catch (failure: Exception) {
                        HandlerList.unregisterAll(listener)
                        metrics.ready.set(0.0)
                        Logger.error("[Metrics] Collector initialization failed; collection stopped until restart: ${failure.message}")
                    }
                })
            } catch (failure: Exception) {
                metrics.ready.set(0.0)
                metrics.storageHealthy.set(0.0)
                Logger.error("[Metrics] Initialization failed; collection remains inactive and existing data is not replaced: ${failure.message}")
            }
        }
        executor.scheduleWithFixedDelay({
            if (!stopping.get() && store != null) flush(metrics, settings)
        }, settings.flushSeconds, settings.flushSeconds, TimeUnit.SECONDS)
        executor.scheduleWithFixedDelay({
            if (!stopping.get()) runCatching { metrics.render() }.onFailure { report(it) }
        }, 1, 1, TimeUnit.SECONDS)
    }

    private fun requestFlush(metrics: PrometheusMetrics, settings: MetricsConfig) {
        if (stopping.get() || !flushRequested.compareAndSet(false, true)) return
        worker?.execute {
            try { flush(metrics, settings) } finally { flushRequested.set(false) }
        }
    }

    private fun flush(metrics: PrometheusMetrics, settings: MetricsConfig) {
        try {
            writer?.flush()
            refreshDatabase(metrics, settings)
        } catch (failure: Exception) {
            metrics.storageHealthy.set(0.0)
            report(failure)
        }
    }

    private fun refreshDatabase(metrics: PrometheusMetrics, settings: MetricsConfig) {
        val database = store ?: return
        val day = java.time.LocalDate.now(java.time.ZoneOffset.UTC)
        if (lastPruneDay != day) {
            database.prune(settings.dailyRetentionDays, settings.sessionRetentionDays)
            lastPruneDay = day
        }
        metrics.database = if (settings.collectPlayerStats) database.snapshot(
            settings.leaderboardSize, settings.publishLeaderboards,
            publishFavorites = settings.publishFavorites, favoriteSize = settings.favoriteSize, favoritePlayerLimit = settings.favoritePlayerLimit,
        )
        else DatabaseSnapshot(updatedSeconds = System.currentTimeMillis() / 1000.0)
        metrics.lastFlush.set(metrics.database.updatedSeconds)
        metrics.storageHealthy.set(1.0)
    }

    private fun report(failure: Throwable) {
        val now = System.currentTimeMillis()
        if (now - lastErrorMillis >= 60_000 || lastErrorMillis == 0L) {
            lastErrorMillis = now
            Logger.error("[Metrics] Database/export failure; pending writes will be retried: ${failure.message}")
        }
    }

    override fun disablePlugin() {
        stopping.set(true)
        sampleTask?.cancel()
        collector?.let {
            HandlerList.unregisterAll(it)
            runCatching { it.stop() }.onFailure { failure ->
                Logger.error("[Metrics] Final player checkpoint failed; preserving existing buffered writes: ${failure.message}")
            }
        }
        collector = null
        endpoint?.close()
        endpoint = null
        val executor = worker
        if (executor != null) {
            val finish = executor.submit {
                try {
                    writer?.flush()
                } catch (failure: Exception) {
                    Logger.error("[Metrics] Final save failed; uncommitted statistics may be lost: ${failure.message}")
                } finally {
                    store?.close()
                }
            }
            executor.shutdown()
            try {
                finish.get(config?.shutdownTimeoutSeconds ?: 10, TimeUnit.SECONDS)
            } catch (failure: Exception) {
                if (failure is InterruptedException) Thread.currentThread().interrupt()
                Logger.error("[Metrics] Shutdown save did not finish in time; recent statistics may be lost: ${failure.message}")
            }
        }
        worker = null
        Module.removeModule(MODULE)
    }
}
