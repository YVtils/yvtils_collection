/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.stats.logic

import kotlinx.serialization.json.Json
import yv.tils.stats.configs.ConfigFile
import yv.tils.stats.data.*
import yv.tils.stats.registry.StatsRegistry

/**
 * Core service for stats serialization and export.
 *
 * This service provides methods to:
 * - Export stats in Prometheus text format (exposition format 0.0.4)
 * - Export stats in JSON format
 * - Generate metadata for exports
 *
 * ## Privacy
 * By default, no sensitive PII is collected. The config option `metadata.region`
 * controls whether region information is included in exports.
 */
class StatsService {
    companion object {
        private val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }

        /**
         * Export all registered stats as Prometheus text format.
         *
         * Follows Prometheus exposition format 0.0.4:
         * - HELP lines describe the metric
         * - TYPE lines declare the metric type
         * - Metric lines contain the value
         *
         * Non-numeric stats (STRING, STRING_LIST) are included as comments.
         *
         * @return Prometheus-formatted text
         */
        fun exportAsPrometheusText(): String {
            val sb = StringBuilder()
            val stats = StatsRegistry.getAllStats()

            // Add metadata as comments
            sb.appendLine("# YVtils Stats Export")
            sb.appendLine("# Generated: ${System.currentTimeMillis()}")
            sb.appendLine()

            for ((key, stat) in stats.entries.sortedBy { it.key }) {
                when (stat.type) {
                    StatType.COUNTER -> {
                        sb.appendLine("# HELP $key ${escapeHelp(stat.help)}")
                        sb.appendLine("# TYPE $key counter")
                        sb.appendLine("$key ${stat.atomicValue.get()}")
                        sb.appendLine()
                    }
                    StatType.GAUGE -> {
                        sb.appendLine("# HELP $key ${escapeHelp(stat.help)}")
                        sb.appendLine("# TYPE $key gauge")
                        sb.appendLine("$key ${stat.atomicValue.get()}")
                        sb.appendLine()
                    }
                    StatType.TIMESTAMP -> {
                        // Timestamps are exported as gauges with Unix timestamp value
                        sb.appendLine("# HELP $key ${escapeHelp(stat.help)}")
                        sb.appendLine("# TYPE $key gauge")
                        sb.appendLine("$key ${stat.atomicValue.get()}")
                        sb.appendLine()
                    }
                    StatType.HISTOGRAM -> {
                        val histData = stat.histogramData.toHistogramData()
                        sb.appendLine("# HELP $key ${escapeHelp(stat.help)}")
                        sb.appendLine("# TYPE $key histogram")
                        sb.appendLine("${key}_count ${histData.count}")
                        sb.appendLine("${key}_sum ${histData.sum}")
                        sb.appendLine()
                    }
                    StatType.STRING -> {
                        // Strings are included as info comments
                        sb.appendLine("# INFO $key: ${stat.stringValue}")
                        sb.appendLine()
                    }
                    StatType.STRING_LIST -> {
                        // Lists are included as JSON comment
                        synchronized(stat.lock) {
                            sb.appendLine("# INFO $key: ${stat.listValue.joinToString(", ")}")
                        }
                        sb.appendLine()
                    }
                }
            }

            return sb.toString()
        }

        /**
         * Export all registered stats as JSON.
         *
         * @return JSON-formatted string
         */
        fun exportAsJson(): String {
            val metadata = buildMetadata()
            val statEntries = buildStatEntries()
            val export = StatsExport(metadata, statEntries)
            return json.encodeToString(export)
        }

        /**
         * Build a StatsExport object for programmatic access.
         */
        fun buildExport(): StatsExport {
            return StatsExport(
                metadata = buildMetadata(),
                stats = buildStatEntries()
            )
        }

        /**
         * Build metadata for the export.
         */
        private fun buildMetadata(): StatsMetadata {
            val serverVersion = try {
                org.bukkit.Bukkit.getVersion()
            } catch (_: Exception) {
                "Unknown"
            }

            return StatsMetadata(
                serverName = ConfigFile.getString("metadata.server_name") ?: "",
                serverVersion = serverVersion,
                collectTimestamp = System.currentTimeMillis(),
                region = ConfigFile.getBoolean("metadata.region") ?: false
            )
        }

        /**
         * Build stat entries for export.
         */
        private fun buildStatEntries(): List<StatEntry> {
            val stats = StatsRegistry.getAllStats()
            val entries = mutableListOf<StatEntry>()

            for ((key, stat) in stats) {
                val value = synchronized(stat.lock) {
                    when (stat.type) {
                        StatType.COUNTER, StatType.GAUGE, StatType.TIMESTAMP ->
                            StatValue.ofLong(stat.atomicValue.get())
                        StatType.STRING ->
                            StatValue.ofString(stat.stringValue)
                        StatType.STRING_LIST ->
                            StatValue.ofList(stat.listValue.toList())
                        StatType.HISTOGRAM ->
                            StatValue.ofHistogram(stat.histogramData.toHistogramData())
                    }
                }

                entries.add(StatEntry(
                    key = key,
                    type = stat.type.name,
                    help = stat.help,
                    value = value,
                    lastUpdated = stat.lastUpdated
                ))
            }

            return entries.sortedBy { it.key }
        }

        /**
         * Escape help text for Prometheus format.
         */
        private fun escapeHelp(help: String): String {
            return help.replace("\\", "\\\\").replace("\n", "\\n")
        }
    }
}
