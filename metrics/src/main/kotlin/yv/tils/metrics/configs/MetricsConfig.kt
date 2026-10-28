/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.configs

import yv.tils.config.data.annotations.ConfigDescription
import yv.tils.config.files.ConfigFormat
import yv.tils.config.files.ObjectMapperFileUtils

data class MetricsConfig(
    @ConfigDescription("HTTP bind address. Non-loopback addresses require a bearer token; use TLS outside trusted networks.")
    val bindAddress: String = "127.0.0.1",
    val port: Int = 9108,
    @ConfigDescription("Optional environment variable containing the endpoint bearer token. Never put secrets in this file.")
    val bearerTokenEnvironment: String = "",
    @ConfigDescription("Seconds between background SQLite commits and leaderboard refreshes.")
    val flushSeconds: Long = 20,
    @ConfigDescription("Ticks between player/server samples. Tick duration is observed separately on every tick.")
    val sampleTicks: Long = 100,
    @ConfigDescription("Persist UUID-keyed player totals and sessions. False collects server health only.")
    val collectPlayerStats: Boolean = true,
    val collectGameplay: Boolean = true,
    @ConfigDescription("Store per-material mining/placement and per-entity kill Favorites in SQLite, not an unbounded Prometheus matrix.")
    val collectBreakdowns: Boolean = true,
    @ConfigDescription("Count estimated AFK time using Paper's idle-duration signal. Does not change the server's idle kick settings.")
    val trackAfk: Boolean = true,
    val afkThresholdSeconds: Long = 300,
    @ConfigDescription("Import supported vanilla totals only when a player is first recorded. Historical imports do not enter daily buckets.")
    val importVanillaOnFirstJoin: Boolean = false,
    @ConfigDescription("Expose names/UUIDs in the Prometheus leaderboard. Disabled by default; SQLite rankings remain available locally.")
    val publishLeaderboards: Boolean = false,
    val leaderboardSize: Int = 10,
    @ConfigDescription("Explicitly publish identifying per-player Favorites. Only the most recently seen favoritePlayerLimit players are exported.")
    val publishFavorites: Boolean = false,
    val favoritePlayerLimit: Int = 25,
    val favoriteSize: Int = 10,
    @ConfigDescription("Keep daily statistics for this many UTC days. Zero keeps them indefinitely.")
    val dailyRetentionDays: Int = 365,
    @ConfigDescription("Keep completed sessions for this many days. Zero keeps them indefinitely.")
    val sessionRetentionDays: Int = 90,
    @ConfigDescription("Maximum aggregated pending records before player collection pauses until restart. Protects memory during prolonged storage failures.")
    val maxPendingEntries: Int = 100_000,
    val shutdownTimeoutSeconds: Long = 10,
) {
    fun validate() {
        require(bindAddress.isNotBlank()) { "metrics.bindAddress must not be blank" }
        require(port in 1..65535) { "metrics.port must be between 1 and 65535" }
        require(flushSeconds in 1..3600) { "metrics.flushSeconds must be between 1 and 3600" }
        require(sampleTicks in 20..1200) { "metrics.sampleTicks must be between 20 and 1200" }
        require(leaderboardSize in 1..100) { "metrics.leaderboardSize must be between 1 and 100" }
        require(favoriteSize in 1..100 && favoritePlayerLimit in 1..100) { "metrics favorite limits must be between 1 and 100" }
        require(afkThresholdSeconds in 1..86400) { "metrics.afkThresholdSeconds must be between 1 and 86400" }
        require(dailyRetentionDays >= 0 && sessionRetentionDays >= 0) { "metrics retention must not be negative" }
        require(shutdownTimeoutSeconds in 1..60) { "metrics.shutdownTimeoutSeconds must be between 1 and 60" }
        require(maxPendingEntries in 1000..1_000_000) { "metrics.maxPendingEntries must be between 1000 and 1000000" }
        require(!publishLeaderboards || collectPlayerStats) { "Publishing leaderboards requires collectPlayerStats" }
        require(!publishFavorites || (collectPlayerStats && collectGameplay && collectBreakdowns)) {
            "Publishing Favorites requires collectPlayerStats, collectGameplay and collectBreakdowns"
        }
    }

    companion object {
        fun load(): MetricsConfig {
            val config = ObjectMapperFileUtils.load("/metrics/config.yml", MetricsConfig(), format = ConfigFormat.YAML)
            config.validate()
            ObjectMapperFileUtils.save("/metrics/config.yml", config, format = ConfigFormat.YAML)
            return config
        }
    }
}
