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
import yv.tils.utils.stats.ModuleTelemetry

/**
 * Core service for stats serialization and export.
 *
 * This service provides methods to:
 * - Export stats in Prometheus text format (exposition format 0.0.4)
 * - Export stats in JSON format
 * - Generate metadata for exports
 *
 * ## Privacy
 * Default metrics contain no player identities. Custom metric authors are responsible
 * for keeping identifying data out of their values. Exports never query Bukkit.
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
            ModuleTelemetry.publishModules()
            val sb = StringBuilder()
            val stats = StatsRegistry.snapshot()

            // Add metadata as comments
            sb.appendLine("# YVtils Stats Export")
            sb.appendLine("# Generated: ${System.currentTimeMillis()}")
            sb.appendLine()

            for (stat in stats) {
                val key = stat.key
                when (StatType.valueOf(stat.type)) {
                    StatType.COUNTER -> {
                        sb.appendLine("# HELP $key ${escapeHelp(stat.help)}")
                        sb.appendLine("# TYPE $key counter")
                        sb.appendLine("$key ${stat.value.longValue}")
                        sb.appendLine()
                    }
                    StatType.GAUGE -> {
                        sb.appendLine("# HELP $key ${escapeHelp(stat.help)}")
                        sb.appendLine("# TYPE $key gauge")
                        sb.appendLine("$key ${stat.value.doubleValue ?: stat.value.longValue}")
                        sb.appendLine()
                    }
                    StatType.TIMESTAMP -> {
                        // Timestamps are exported as gauges with Unix timestamp value
                        sb.appendLine("# HELP $key ${escapeHelp(stat.help)}")
                        sb.appendLine("# TYPE $key gauge")
                        sb.appendLine("$key ${stat.value.longValue}")
                        sb.appendLine()
                    }
                    StatType.HISTOGRAM -> {
                        val histData = stat.value.histogramValue ?: HistogramData()
                        sb.appendLine("# HELP $key ${escapeHelp(stat.help)}")
                        sb.appendLine("# TYPE $key histogram")
                        sb.appendLine("${key}_bucket{le=\"+Inf\"} ${histData.count}")
                        sb.appendLine("${key}_count ${histData.count}")
                        sb.appendLine("${key}_sum ${histData.sum}")
                        sb.appendLine()
                    }
                    StatType.STRING -> {
                        // Strings are included as info comments
                        sb.appendLine("# INFO $key: ${escapeHelp(stat.value.stringValue.orEmpty())}")
                        sb.appendLine()
                    }
                    StatType.STRING_LIST -> {
                        // Lists are included as JSON comment
                        sb.appendLine("# INFO $key: ${escapeHelp(stat.value.listValue.orEmpty().joinToString(", "))}")
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
            return StatsMetadata(
                serverName = ConfigFile.state.metadata.server_name,
                serverVersion = StatsRegistry.getValue("server_version")?.stringValue ?: "Unknown",
                collectTimestamp = System.currentTimeMillis(),
                region = false
            )
        }

        /**
         * Build stat entries for export.
         */
        private fun buildStatEntries(): List<StatEntry> {
            ModuleTelemetry.publishModules()
            return StatsRegistry.snapshot()
        }

        /**
         * Escape help text for Prometheus format.
         */
        private fun escapeHelp(help: String): String {
            return help.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n")
        }
    }
}
