/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.discord.logic

import net.dv8tion.jda.api.*
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import org.apache.logging.log4j.LogManager
import yv.tils.config.language.LanguageHandler
import yv.tils.discord.DiscordYVtils
import yv.tils.discord.actions.buttons.JDAButtonsListener
import yv.tils.discord.actions.commands.JDACommandsListener
import yv.tils.discord.actions.select.JDASelectListener
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.language.RegisterStrings
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager
import yv.tils.discord.logic.sync.serverChats.SyncChats
import yv.tils.discord.logic.sync.serverConsole.GetConsole
import yv.tils.discord.logic.sync.serverConsole.SendCMD
import yv.tils.discord.logic.sync.serverStats.CollectStats
import yv.tils.discord.logic.whitelist.WhitelistManage
import yv.tils.discord.utils.emoji.DiscordEmoji
import yv.tils.utils.logger.Logger
import java.time.Duration
import org.apache.logging.log4j.core.Logger as Logger4J

class AppLogic {
    companion object {
        lateinit var instance: AppLogic
        lateinit var jda: JDA
        lateinit var builder: JDABuilder
        var started = false

        lateinit var appID: String

        /**
         * Returns the current instance of the AppLogic.
         * @return The current instance of AppLogic.
         * @throws IllegalStateException if the instance is not initialized.
         */
        fun getJDA(): JDA {
            if (! ::jda.isInitialized) {
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

    fun startApp() {
        if (checkToken()) {
            instance = this
            appearance()
            intents()
            eventListeners()
            buildJDA()
        } else {
            DiscordYVtils().unregisterModule()
        }
    }

    fun stopApp() {
        if (started) {
            Logger.info(LanguageHandler.getMessage(RegisterStrings.LangStrings.BOT_STOP_SHUTDOWN.key))

            CollectStats().serverShutdown()

            GetConsole.active = false
            GetConsole().stop()

            try {
                builder.setStatus(OnlineStatus.OFFLINE)
                jda.shutdown()

                if (!jda.awaitShutdown(Duration.ofSeconds(10))) {
                    jda.shutdownNow()
                    jda.awaitShutdown()
                }
            } catch (e: Exception) {
                Logger.error(
                    LanguageHandler.getMessage(
                        RegisterStrings.LangStrings.BOT_STOP_FAILED.key,
                        mapOf<String, Any>(
                            "error" to (e.message ?: "Unknown error occurred")
                        )
                    )
                )
            }
            Logger.info(LanguageHandler.getMessage(RegisterStrings.LangStrings.BOT_STOP_SUCCESS.key))
            started = false
        } else {
            Logger.warn(LanguageHandler.getMessage(RegisterStrings.LangStrings.BOT_STOP_NOT_RUNNING.key))
        }
    }

    private fun eventListeners() {
        builder.addEventListeners(SendCMD())

        builder.addEventListeners(SyncChats())

        builder.addEventListeners(JDACommandsListener())

        builder.addEventListeners(JDAButtonsListener())

        builder.addEventListeners(JDASelectListener())

        builder.addEventListeners(WhitelistManage())
    }

    private fun checkToken(): Boolean {
        if (appToken == null || appToken.isEmpty() || appToken.isBlank() || appToken.trim().contains(" ")) {
            Logger.error("App token is not set. Please configure it in the config file of the discord module.")
            return false
        }
        builder = JDABuilder.createDefault(appToken)
        return true
    }

    private fun appearance() {
        try {
            builder.setStatus(OnlineStatus.fromKey(status))
        } catch (e: IllegalArgumentException) {
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
            jda.awaitReady()
            started = true
            appID = jda.selfUser.id
            launchFeatures()
        } catch (e: Exception) {
            Logger.error("Failed to start Discord app: ${e.message}")
            started = false

            DiscordYVtils().unregisterModule()
        }
    }

    private fun launchFeatures() {
        try {
            DiscordEmoji().setPersistentEmojis()
            DiscordEmoji().loadPersistentEmojis()
        } catch (e: Exception) {
            Logger.error("Failed to initialize Discord emojis: ${e.message}")
            Logger.debug("Stack trace: ${e.stackTraceToString()}", 2)
        }

        ServerChatsSyncManager().loadChannelFromID()

        CollectStats().collect()

        val appender = GetConsole()
        runCatching {
            val logger = LogManager.getRootLogger() as Logger4J
            logger.addAppender(appender)
        }

        appender.syncTask()
    }
}
