package yv.tils.discord.logic

import logger.Logger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import org.apache.logging.log4j.LogManager
import yv.tils.discord.commands.JDACommandsListener
import yv.tils.discord.commands.JDACommandsRegister
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.logic.sync.serverChats.ServerChatsSyncManager
import yv.tils.discord.logic.sync.serverChats.SyncChats
import yv.tils.discord.logic.sync.serverConsole.GetConsole
import yv.tils.discord.logic.sync.serverConsole.SendCMD
import yv.tils.discord.logic.sync.serverStats.CollectStats
import java.time.Duration

class AppLogic {
    companion object {
        lateinit var instance: AppLogic
        lateinit var jda: JDA
        lateinit var builder: JDABuilder
        var started = false

        lateinit var appID: String
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
        }
    }

    fun stopApp() {
        if (started) {
            Logger.info("Stopping Discord bot...") // TODO: Replace with actual stopping message

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
                Logger.error("Failed to shut down Discord bot: ${e.message}") // TODO: Replace with actual error message
            }
            Logger.info("Discord bot stopped successfully.") // TODO: Replace with actual success message
            started = false
        } else {
            Logger.warn("Discord bot is not running. No action taken.") // TODO: Replace with actual warning message
        }
    }

    private fun eventListeners() {
        builder.addEventListeners(SendCMD())
        builder.addEventListeners(SyncChats())
        builder.addEventListeners(JDACommandsRegister())
        builder.addEventListeners(JDACommandsListener())
    }

    private fun checkToken(): Boolean {
        if (appToken == null || appToken.isEmpty() || appToken.isBlank()) {
            Logger.error("App token is not set. Please configure it in the config file.") // TODO: Replace with actual error message
            return false
        }
        builder = JDABuilder.createDefault(appToken)
        return true
    }

    private fun appearance() {
        try {
            builder.setStatus(OnlineStatus.fromKey(status))
        } catch (e: IllegalArgumentException) {
            Logger.warn("Invalid online status: $status. Defaulting to 'online'.") // TODO: Replace with actual warning message
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
            jda.awaitReady() // Wait for JDA to be ready
            started = true
            appID = jda.selfUser.id // Get the bot's application ID
            launchFeatures() // Start background tasks
            Logger.info("Discord bot started successfully.") // TODO: Replace with actual success message
        } catch (e: Exception) {
            Logger.error("Failed to start Discord bot: ${e.message}") // TODO: Replace with actual error message
            started = false
        }
    }

    private fun launchFeatures() {
        ServerChatsSyncManager().loadChannelFromID()

        CollectStats().collect()

        val appender = GetConsole()
        runCatching {
            val logger = LogManager.getRootLogger() as org.apache.logging.log4j.core.Logger
            logger.addAppender(appender)
        }

        appender.syncTask()
    }
}