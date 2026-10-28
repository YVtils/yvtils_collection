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
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.scheduler.BukkitTask
import yv.tils.config.language.LanguageHandler
import yv.tils.stats.api.RegistryStatsBackend
import yv.tils.stats.commands.StatsCommand
import yv.tils.stats.configs.ConfigFile
import yv.tils.stats.configs.ManageGUI
import yv.tils.stats.language.RegisterStrings
import yv.tils.stats.listeners.FirstOperatorJoin
import yv.tils.stats.logic.StatsAdministration
import yv.tils.stats.logic.StatsPusher
import yv.tils.stats.logic.StatsService
import yv.tils.stats.registry.StatsRegistry
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module
import yv.tils.utils.stats.StatsApi
import yv.tils.utils.stats.ModuleTelemetry

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
class StatsYVtils : Module.YVtilsModule {
    private var joinListener: FirstOperatorJoin? = null
    private var commandRegistered = false
    private var bootAnnounced = false

    companion object {
        val MODULE = Module.YVtilsModuleData(
            "stats",
            Module.readVersion(StatsYVtils::class.java, "stats"),
            "Stats module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/stats/",
            configGuiOpener = { player -> ManageGUI().openGUI(player) },
        )

        @Volatile private var enabled = false
        private var playerCountTask: BukkitTask? = null
        private var statsBackend: RegistryStatsBackend? = null

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
        }

        /**
         * Check if remote push is enabled.
         * Requires both an enabled module and persisted consent.
         */
        fun isRemotePushEnabled(): Boolean = enabled && isOptedIn()

        /**
         * Manually trigger a push to the remote API server.
         * Synchronous: call from a worker thread, or use StatsPusher.pushOnIo().
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
            if (isRemotePushEnabled()) StatsPusher.startScheduledPush()
        }

        /**
         * Stop the remote push scheduler.
         */
        private fun stopRemotePush() {
            StatsPusher.stopScheduledPush()
        }

        private fun refreshConfiguration() {
            // Revoke network access immediately, even when called by a worker.
            if (!isOptedIn()) stopRemotePush()
            if (!enabled) return
            if (!Bukkit.isPrimaryThread()) {
                Bukkit.getScheduler().runTask(Core.instance, Runnable { refreshConfiguration() })
                return
            }
            StatsRegistry.configureLimits(ConfigFile.state.max_list_size, ConfigFile.state.max_stats_count)
            if (!isOptedIn()) {
                playerCountTask?.cancel()
                playerCountTask = null
                statsBackend?.clearRegistry() ?: StatsRegistry.clear()
                return
            }
            registerDefaultStats()
            ModuleTelemetry.publishModules()
            startRemotePush()
        }

        /**
         * Request an opt-in prompt for the specified player.
         * This should be called by other modules when an admin first joins.
         *
         * The prompt includes:
         * - Explanation of what data is collected
         * - Privacy information
         * - Clickable, permission-checked in-game accept/decline/settings actions
         *
         * @param triggeringPlayer The admin player to show the prompt to
         */
        fun requestOptInPrompt(triggeringPlayer: Player) {
            if (!needsOptInPrompt()) {
                Logger.debug("[Stats] Opt-in prompt not needed, decision already made")
                return
            }

            if (!StatsAdministration.canConfigure(triggeringPlayer)) {
                Logger.debug("[Stats] Player ${triggeringPlayer.name} lacks permission for opt-in")
                return
            }

            triggeringPlayer.sendMessage(RegisterStrings.formatOptInPrompt(
                LanguageHandler.getMessage(RegisterStrings.LangStrings.OPTIN_PROMPT.key, triggeringPlayer),
            ))
            triggeringPlayer.sendMessage(Component.empty()
                .append(LanguageHandler.getMessage(RegisterStrings.LangStrings.GUI_ACCEPT.key, triggeringPlayer)
                    .clickEvent(ClickEvent.runCommand("/yvtilsstats optin accept")))
                .append(Component.text("  "))
                .append(LanguageHandler.getMessage(RegisterStrings.LangStrings.GUI_DECLINE.key, triggeringPlayer)
                    .clickEvent(ClickEvent.runCommand("/yvtilsstats optin decline")))
                .append(Component.text("  "))
                .append(LanguageHandler.getMessage(RegisterStrings.LangStrings.GUI_SETTINGS.key, triggeringPlayer)
                    .clickEvent(ClickEvent.runCommand("/yvtilsstats config"))))
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
            pluginsHandle?.set(Module.getModuleNames(sort = true))

            // Server version
            val versionHandle = StatsRegistry.registerString("server_version", "Paper/Bukkit server version")
            try {
                versionHandle?.set(Bukkit.getVersion())
            } catch (e: Exception) {
                versionHandle?.set("Unknown")
            }

            // Player count (if enabled)
            if (ConfigFile.state.metadata.collect_player_count) {
                val playerCountHandle = StatsRegistry.registerGauge("player_count", "Current online player count")
                try {
                    playerCountHandle?.set(Bukkit.getOnlinePlayers().size.toLong())
                } catch (e: Exception) {
                    // Bukkit might not be available in tests
                }

                // Bukkit reads must stay on Paper's server thread. Keep exactly one collector.
                if (playerCountHandle != null && (playerCountTask == null || playerCountTask?.isCancelled == true)) {
                    playerCountTask = Bukkit.getScheduler().runTaskTimer(
                        Core.instance,
                        Runnable {
                            if (enabled && isOptedIn() && ConfigFile.state.metadata.collect_player_count) {
                                StatsRegistry.set("player_count", Bukkit.getOnlinePlayers().size.toLong())
                            }
                        },
                        1200L,
                        1200L,
                    )
                }
            } else {
                playerCountTask?.cancel()
                playerCountTask = null
                StatsRegistry.unregister("player_count")
            }

            Logger.debug("[Stats] Default stats registered")
        }
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
    }

    override fun enablePlugin() {
        loadConfigs()
        Module.addModule(MODULE)
        enabled = true
        bootAnnounced = false
        StatsPusher.setEnabled(true)
        statsBackend = RegistryStatsBackend(::isRemotePushEnabled).also(StatsApi::installBackend)
        ConfigFile.onChange = ::refreshConfiguration
        refreshConfiguration()
        joinListener = FirstOperatorJoin().also {
            Bukkit.getPluginManager().registerEvents(it, Core.instance)
        }
        StatsCommand.register()
        commandRegistered = true

        Logger.debug("[Stats] Module enabled")
    }

    override fun onLateEnablePlugin() {
        ModuleTelemetry.publishModules()
        if (!bootAnnounced) {
            bootAnnounced = true
            Logger.info(bootMessage(ConfigFile.state.opt_in))
        }
        // Update module list after all modules are loaded
        if (isOptedIn()) {
            val pluginsHandle = StatsRegistry.registerList("used_yvtils_plugins", "List of YVtils modules in use")
            pluginsHandle?.set(Module.getModuleNames(sort = true))
        }
    }

    override fun disablePlugin() {
        enabled = false
        ConfigFile.onChange = null
        StatsPusher.setEnabled(false)
        statsBackend?.let {
            it.close()
            StatsApi.uninstallBackend(it)
        }
        statsBackend = null
        playerCountTask?.cancel()
        playerCountTask = null

        // Clear registry
        StatsRegistry.clear()

        joinListener?.let(HandlerList::unregisterAll)
        joinListener = null
        if (commandRegistered) StatsCommand.unregister()
        commandRegistered = false

        Module.removeModule(MODULE)
        Logger.info("[Stats] Module disabled")
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
    }

    internal fun bootMessage(decision: Boolean?): String {
        val status = when (decision) { true -> "enabled"; false -> "declined"; null -> "not configured (off)" }
        return "[Stats] Optional usage telemetry: $status; in-game settings/consent: /yvtilsstats"
    }
}
