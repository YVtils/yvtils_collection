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

package yv.tils.discord.logic

import kotlinx.coroutines.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import okhttp3.OkHttpClient
import org.apache.logging.log4j.LogManager
import org.bukkit.scheduler.BukkitTask
import yv.tils.discord.DiscordYVtils
import yv.tils.discord.actions.buttons.JDAButtonsListener
import yv.tils.discord.actions.commands.JDACommandsListener
import yv.tils.discord.actions.commands.JDACommandsRegister
import yv.tils.discord.actions.modals.JDARegistration
import yv.tils.discord.actions.select.JDASelectListener
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.connection.DiscordConnectionLogging
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager
import yv.tils.discord.logic.sync.serverChats.SyncChats
import yv.tils.discord.logic.sync.serverConsole.GetConsole
import yv.tils.discord.logic.sync.serverConsole.SendCMD
import yv.tils.discord.logic.sync.serverStats.CollectStats
import yv.tils.discord.logic.whitelist.WhitelistManage
import yv.tils.discord.utils.emoji.DiscordEmoji
import yv.tils.discord.utils.emoji.RegistrationEmoji
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import org.apache.logging.log4j.core.Logger as Logger4J

class AppLogic {
    companion object {
        lateinit var instance: AppLogic
        lateinit var jda: JDA
        lateinit var builder: JDABuilder

        @Volatile
        var started = false

        @Volatile
        private var stopping = false
        private var connectionTask: BukkitTask? = null
        private var consoleAppender: GetConsole? = null
        fun clearConsoleHistory() = consoleAppender?.clearHistory()
        private fun hasJDA() = ::jda.isInitialized

        lateinit var appID: String

        /**
         * Returns the current instance of the AppLogic.
         * @return The current instance of AppLogic.
         * @throws IllegalStateException if the instance is not initialized.
         */
        fun getJDA(): JDA {
            if (!::jda.isInitialized) {
                throw IllegalStateException("JDA is not initialized")
            }
            return jda
        }
    }

    private val appToken = ConfigFile.getValueAsString("appToken")
    private val mainGuild = ConfigFile.getValueAsString("mainGuild")
    private val status = ConfigFile.getValueAsString("botSettings.onlineStatus") ?: "online"
    private val activity = ConfigFile.getValueAsString("botSettings.activity") ?: "none"
    private val activityMessage = ConfigFile.getValueAsString("botSettings.activityMessage") ?: "Minecraft"
    private val featureScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var httpClient: OkHttpClient? = null

    fun startApp() {
        instance = this
        stopping = false
        try {
            DiscordConnectionLogging.start()
            if (checkToken()) {
                appearance()
                intents()
                eventListeners()
                buildJDA()
            } else {
                stopApp()
                DiscordYVtils().unregisterModule()
            }
        } catch (error: Exception) {
            stopApp()
            throw error
        }
    }

    fun stopApp() {
        stopping = true
        DiscordConnectionLogging.stopping()
        val wasStarted = started
        started = false
        try {
            connectionTask?.cancel()
            connectionTask = null
            featureScope.cancel()
            GetConsole.active = false
            consoleAppender?.let { appender ->
                (LogManager.getRootLogger() as? Logger4J)?.removeAppender(appender)
                appender.stop()
            }
            consoleAppender = null
            CollectStats().stop()
            if (hasJDA()) {
                if (wasStarted) {
                    runCatching {
                        val actions = CollectStats().serverShutdown()
                        runBlocking(Dispatchers.IO) {
                            val updates = actions.map {
                                runCatching { it.timeout(5, TimeUnit.SECONDS).submit() }
                                    .getOrElse { error -> CompletableFuture.failedFuture<Any?>(error) }
                            }
                            awaitShutdownUpdates(updates, Duration.ofSeconds(5))
                        }
                    }
                        .onFailure {
                            if (it is InterruptedException) throw it
                            Logger.warn("Could not publish Discord shutdown statistics: ${it.message}")
                        }
                }
                jda.shutdown()
                if (!jda.awaitShutdown(Duration.ofSeconds(10))) {
                    jda.shutdownNow()
                    jda.awaitShutdown(Duration.ofSeconds(2))
                }
            }
        } catch (error: Exception) {
            if (hasJDA()) {
                jda.shutdownNow()
                runCatching { jda.awaitShutdown(Duration.ofSeconds(2)) }
            }
            if (error is InterruptedException) Thread.currentThread().interrupt()
            Logger.error("Failed to stop Discord app", error)
        } finally {
            // JDA does not own the OkHttp dispatcher/pool lifecycle. Close ours before
            // Paper closes the library classloader, including failed/pending logins.
            httpClient?.let { client ->
                client.dispatcher.cancelAll()
                client.connectionPool.evictAll()
                client.dispatcher.executorService.shutdownNow()
            }
            httpClient = null
            DiscordConnectionLogging.close()
        }
    }

    private fun eventListeners() {
        builder.addEventListeners(DiscordConnectionLogging)
        builder.addEventListeners(SendCMD())

        builder.addEventListeners(SyncChats())

        builder.addEventListeners(JDACommandsListener())

        builder.addEventListeners(JDAButtonsListener())
        builder.addEventListeners(JDARegistration())

        builder.addEventListeners(JDASelectListener())

        builder.addEventListeners(WhitelistManage())
    }

    private fun checkToken(): Boolean {
        if (appToken.isNullOrEmpty() || appToken.isBlank() || appToken.trim().contains(" ")) {
            Logger.error("App token is not set. Please configure it in the config file of the discord module.")
            return false
        }
        builder = JDABuilder.createDefault(appToken)
        // Paper owns our lifecycle. JDA's JVM hook calls shutdownNow and races onDisable.
        builder.setEnableShutdownHook(false)
        httpClient = OkHttpClient.Builder().build()
        builder.setHttpClient(httpClient!!)
        return true
    }

    private fun appearance() {
        try {
            builder.setStatus(OnlineStatus.fromKey(status))
        } catch (_: IllegalArgumentException) {
            Logger.warn("Invalid online status: $status. Defaulting to 'online'.")
            builder.setStatus(OnlineStatus.ONLINE)
        }

        when (activity.lowercase()) {
            "playing" -> builder.setActivity(Activity.playing(activityMessage))
            "listening" -> builder.setActivity(Activity.listening(activityMessage))
            "watching" -> builder.setActivity(Activity.watching(activityMessage))
            "competing" -> builder.setActivity(Activity.competing(activityMessage))
            "custom" -> builder.setActivity(Activity.customStatus(activityMessage))
            else -> builder.setActivity(null)
        }
    }

    private fun intents() {
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
        builder.setMemberCachePolicy(MemberCachePolicy.ALL)
    }

    private fun buildJDA() {
        try {
            jda = builder.build()
            connectionTask = Core.instance.server.scheduler.runTaskAsynchronously(
                Core.instance, Runnable {
                    try {
                        jda.awaitReady()
                        if (stopping) {
                            jda.shutdownNow(); return@Runnable
                        }
                        Core.instance.server.scheduler.runTask(
                            Core.instance, Runnable ready@{
                                if (stopping) return@ready
                                started = true
                                appID = jda.selfUser.id
                                try {
                                    launchFeatures()
                                    JDACommandsRegister().registerCommands()
                                    Logger.info("Discord connection ready.")
                                } catch (error: Exception) {
                                    stopApp()
                                    Logger.error("Discord feature initialization failed", error)
                                }
                            })
                    } catch (error: Exception) {
                        val report = !stopping
                        if (report) stopApp()
                        if (report) Logger.error("Discord connection failed", error)
                    }
                })
        } catch (e: Exception) {
            stopApp()
            Logger.error("Failed to start Discord app", e)
            DiscordYVtils().unregisterModule()
        }
    }

    private fun launchFeatures() {
        RegistrationEmoji.initialize()
        featureScope.launch {
            if (!stopping) try {
                runInterruptible {
                    DiscordEmoji().setPersistentEmojis()
                    DiscordEmoji().loadPersistentEmojis()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Logger.error("Failed to initialize Discord emojis: ${e.message}")
            }
        }

        ServerChatsSyncManager().loadChannelFromID()

        CollectStats().collect()

        if (ConfigFile.getValueAsBoolean("syncFeature.consoleSync.enabled") == true) {
            val appender = GetConsole()
            consoleAppender = appender
            GetConsole.active = true
            appender.start()
            val logger = LogManager.getRootLogger() as Logger4J
            logger.addAppender(appender)
            appender.syncTask()
        }
    }
}
