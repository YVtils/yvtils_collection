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

package yv.tils.stats

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import yv.tils.stats.configs.ConfigFile
import yv.tils.stats.data.StatType
import yv.tils.stats.language.RegisterStrings
import yv.tils.stats.logic.StatsPusher
import yv.tils.stats.logic.StatsService
import yv.tils.stats.registry.StatsRegistry
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger

/**
 * Main module class for the stats module.
 *
 * This module provides a registry for tracking various statistics and metrics
 * that can be exported in Prometheus or JSON format, and optionally pushed
 * to the remote Grafana instance at statistics.yvtils.net.
 *
 * ## Features
 * - Thread-safe stat registration and updates
 * - Multiple stat types: Counter, Gauge, String, List, Timestamp, Histogram
 * - Prometheus and JSON export formats
 * - Opt-in system for privacy
 * - Remote push to api.yvtils.net/stats
 *
 * ## Public API
 * - [StatsRegistry] - Register and update stats
 * - [StatsService] - Export stats
 * - [StatsPusher] - Remote push to api.yvtils.net
 * - [needsOptInPrompt] - Check if opt-in is needed
 * - [markOptIn] - Set opt-in decision
 * - [requestOptInPrompt] - Show opt-in prompt to admin
 */
class StatsYVtils : Data.YVtilsModule {
    companion object {
        val MODULE = Data.YVtilsModuleData(
            "stats",
            "1.0.0-beta.1",
            "Stats module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/stats/"
        )

        private var pushTaskId: String? = null

        // ========================
        // Public API
        // ========================

        /**
         * Check if the user needs to be prompted for opt-in.
         * Returns true if no decision has been made yet.
         */
        fun needsOptInPrompt(): Boolean = ConfigFile.needsOptInPrompt()

        /**
         * Check if the user has opted in to stats collection.
         */
        fun isOptedIn(): Boolean = ConfigFile.isOptedIn()

        /**
         * Persist the opt-in decision and enable/disable stats accordingly.
         *
         * @param decision true to opt-in, false to opt-out
         */
        fun markOptIn(decision: Boolean) {
            ConfigFile.markOptIn(decision)
            if (decision) {
                registerDefaultStats()
                // Start remote push if enabled
                startRemotePush()
            } else {
                // Stop remote push if running
                stopRemotePush()
            }
        }

        /**
         * Check if remote push is enabled.
         * This is always true when opted in.
         */
        fun isRemotePushEnabled(): Boolean = isOptedIn()

        /**
         * Manually trigger a push to the remote API server.
         * Useful for testing or commands.
         *
         * @return The result of the push operation
         */
        fun forcePush(): StatsPusher.PushResult = StatsPusher.forcePush()

        /**
         * Get the timestamp of the last successful remote push.
         *
         * @return Unix timestamp, or 0 if never pushed
         */
        fun getLastPushTime(): Long = StatsPusher.getLastPushTime()

        /**
         * Start the remote push scheduler.
         * Called automatically when opted in.
         */
        private fun startRemotePush() {
            if (isOptedIn()) {
                pushTaskId = StatsPusher.startScheduledPush()
                Logger.info("[Stats] Remote push enabled")
            }
        }

        /**
         * Stop the remote push scheduler.
         */
        private fun stopRemotePush() {
            StatsPusher.stopScheduledPush()
            pushTaskId = null
        }

        /**
         * Request an opt-in prompt for the specified player.
         * This should be called by other modules when an admin first joins.
         *
         * The prompt includes:
         * - Explanation of what data is collected
         * - Privacy information
         * - Clickable accept/decline buttons
         *
         * @param triggeringPlayer The admin player to show the prompt to
         */
        fun requestOptInPrompt(triggeringPlayer: Player) {
            if (!needsOptInPrompt()) {
                Logger.debug("[Stats] Opt-in prompt not needed, decision already made")
                return
            }

            if (!triggeringPlayer.hasPermission("yvtils.stats.optin")) {
                Logger.debug("[Stats] Player ${triggeringPlayer.name} lacks permission for opt-in")
                return
            }

            // Build the prompt message
            val prefix = Component.text("[YVtils Stats] ", NamedTextColor.GOLD)
            
            val message = Component.empty()
                .append(prefix)
                .append(Component.text("We would like to collect anonymous usage statistics ", NamedTextColor.WHITE))
                .append(Component.text("to help improve YVtils.", NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.newline())
                .append(prefix)
                .append(Component.text("What we collect:", NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("  • Server version (Paper/Spigot)", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("  • Active YVtils modules", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("  • Player count (optional)", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.newline())
                .append(prefix)
                .append(Component.text("What we DON'T collect:", NamedTextColor.GREEN, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("  • Player names or UUIDs", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("  • Server IP addresses (by default)", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("  • Any personal information", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.newline())
                .append(prefix)
                .append(Component.text("Data is sent to: ", NamedTextColor.GRAY))
                .append(Component.text("api.yvtils.net/stats", NamedTextColor.AQUA))
                .append(Component.newline())
                .append(Component.newline())
                .append(prefix)
                .append(Component.text("[ACCEPT]", NamedTextColor.GREEN, TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/yvtils stats optin accept"))
                    .hoverEvent(Component.text("Click to accept stats collection")))
                .append(Component.text("  ", NamedTextColor.WHITE))
                .append(Component.text("[DECLINE]", NamedTextColor.RED, TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/yvtils stats optin decline"))
                    .hoverEvent(Component.text("Click to decline stats collection")))
                .append(Component.newline())
                .append(Component.newline())
                .append(prefix)
                .append(Component.text("You can change this later in ", NamedTextColor.GRAY))
                .append(Component.text("plugins/yvtils/stats/config.yml", NamedTextColor.AQUA))

            triggeringPlayer.sendMessage(message)
        }

        /**
         * Export all stats as Prometheus text format.
         */
        fun exportAsPrometheusText(): String = StatsService.exportAsPrometheusText()

        /**
         * Export all stats as JSON.
         */
        fun exportAsJson(): String = StatsService.exportAsJson()

        /**
         * Register default stats tracked by the module.
         */
        private fun registerDefaultStats() {
            if (!isOptedIn()) {
                Logger.debug("[Stats] Not registering default stats - not opted in")
                return
            }

            Logger.debug("[Stats] Registering default stats")

            // Active module
            StatsRegistry.registerString("active_module", "The currently active or last used YVtils module")

            // Used plugins list
            val pluginsHandle = StatsRegistry.registerList("used_yvtils_plugins", "List of YVtils modules in use")
            pluginsHandle?.set(Data.getModuleNames(sorted = true))

            // Server version
            val versionHandle = StatsRegistry.registerString("server_version", "Paper/Bukkit server version")
            try {
                versionHandle?.set(org.bukkit.Bukkit.getVersion())
            } catch (e: Exception) {
                versionHandle?.set("Unknown")
            }

            // Player count (if enabled)
            if (ConfigFile.getBoolean("metadata.collect_player_count") != false) {
                val playerCountHandle = StatsRegistry.registerGauge("player_count", "Current online player count")
                try {
                    playerCountHandle?.set(org.bukkit.Bukkit.getOnlinePlayers().size.toLong())
                } catch (e: Exception) {
                    // Bukkit might not be available in tests
                }

                // Schedule periodic player count updates
                CoroutineHandler.launchTask(
                    suspend {
                        try {
                            StatsRegistry.set("player_count", org.bukkit.Bukkit.getOnlinePlayers().size.toLong())
                        } catch (e: Exception) {
                            // Ignore if Bukkit not available
                        }
                    },
                    "yvtils-stats-playercount",
                    beforeDelay = 60_000L, // 1 minute
                    afterDelay = 60_000L,
                    isOnce = false
                )
            }

            Logger.info("[Stats] Default stats registered")
        }
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
        ConfigFile().registerStrings()
    }

    override fun enablePlugin() {
        Data.addModule(MODULE)

        registerCommands()
        registerCoroutines()

        loadConfigs()

        // Register default stats if opted in
        if (isOptedIn()) {
            registerDefaultStats()
        }

        // Start remote push if opted in
        startRemotePush()

        Logger.info("[Stats] Module enabled")
    }

    override fun onLateEnablePlugin() {
        // Update module list after all modules are loaded
        if (isOptedIn()) {
            val pluginsHandle = StatsRegistry.registerList("used_yvtils_plugins", "List of YVtils modules in use")
            pluginsHandle?.set(Data.getModuleNames(sorted = true))
        }
    }

    override fun disablePlugin() {
        // Stop remote push scheduler
        stopRemotePush()

        // Clear registry
        StatsRegistry.clear()

        Data.removeModule(MODULE)
        Logger.info("[Stats] Module disabled")
    }

    private fun registerCommands() {
        // Commands will be registered by the main plugin
        // This module only provides the API
    }

    private fun registerCoroutines() {
        // Coroutines are registered in enablePlugin and registerDefaultStats
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
    }
}
