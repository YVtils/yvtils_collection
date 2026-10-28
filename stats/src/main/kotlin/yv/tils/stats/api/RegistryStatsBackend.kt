/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.stats.api

import yv.tils.stats.registry.StatsRegistry
import yv.tils.utils.stats.ModuleStats.MetricType
import yv.tils.utils.stats.StatsApi
import java.util.function.BooleanSupplier

/** Consent-aware adapter; no collection, buffering or registration happens before consent. */
internal class RegistryStatsBackend(private val collecting: BooleanSupplier) : StatsApi.Backend, AutoCloseable {
    @Volatile private var active = true

    override fun isCollecting(): Boolean = active && collecting.asBoolean

    @Synchronized
    override fun publish(module: String, key: String, help: String, type: MetricType, value: Any): Boolean {
        if (!isCollecting()) return false
        val name = prefix(module) + key
        return when (type) {
            MetricType.COUNTER -> StatsRegistry.registerCounter(name, help)?.let { it.inc((value as Number).toLong()); true } ?: false
            MetricType.GAUGE -> StatsRegistry.registerGauge(name, help)?.let { StatsRegistry.set(name, value); true } ?: false
            MetricType.STRING -> StatsRegistry.registerString(name, help)?.let { it.set(value as String); true } ?: false
            MetricType.STRING_LIST -> StatsRegistry.registerList(name, help)?.let { StatsRegistry.set(name, value); true } ?: false
            MetricType.TIMESTAMP -> StatsRegistry.registerTimestamp(name, help)?.let { it.set((value as Number).toLong()); true } ?: false
            MetricType.HISTOGRAM -> StatsRegistry.registerHistogram(name, help)?.let { it.observe((value as Number).toDouble()); true } ?: false
        }
    }

    @Synchronized
    override fun removeModule(module: String) {
        if (!active) return
        val prefix = prefix(module)
        StatsRegistry.getKeys().filter { it.startsWith(prefix) }.forEach(StatsRegistry::unregister)
    }

    @Synchronized
    override fun close() {
        active = false
    }

    @Synchronized
    internal fun clearRegistry() = StatsRegistry.clear()

    companion object {
        /** Escape module separators injectively; `a-b`, `a.b` and `a_b` never alias. */
        internal fun prefix(module: String): String = "yvtils_module_" + module
            .replace("_", "_u").replace("-", "_h").replace(".", "_d") + "__"
    }
}
