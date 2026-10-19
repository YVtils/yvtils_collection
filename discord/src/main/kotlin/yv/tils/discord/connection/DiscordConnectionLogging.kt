package yv.tils.discord.connection

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.session.SessionRecreateEvent
import net.dv8tion.jda.api.events.session.SessionResumeEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.bukkit.scheduler.BukkitTask
import yv.tils.utils.modules.Core
import yv.tils.utils.logger.Logger
import java.net.UnknownHostException
import java.util.Collections
import java.util.IdentityHashMap

object DiscordConnectionLogging : ListenerAdapter() {
    private var notices = NetworkNotices()
    private var filter: JdaNetworkLogFilter? = null
    private var context: LoggerContext? = null
    private var task: BukkitTask? = null
    private val walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

    fun start() {
        close()
        notices = NetworkNotices()
        val ctx = LogManager.getContext(false) as? LoggerContext
        if (ctx == null) {
            Logger.warn("[Discord] Could not install connection log formatting; JDA diagnostics remain enabled.")
            return
        }
        val installed = JdaNetworkLogFilter(notices) {
            walker.walk { frames ->
                frames.anyMatch {
                    (it.className == JdaNetworkLogFilter.REQUESTER || it.className == JdaNetworkLogFilter.SOCKET) &&
                            it.declaringClass.classLoader === JDA::class.java.classLoader
                }
            }
        }
        installed.start()
        ctx.addFilter(installed)
        context = ctx
        filter = installed
        task = Core.instance.server.scheduler.runTaskTimer(Core.instance, Runnable {
            notices.drain().forEach { message ->
                if (message.endsWith("has been restored.")) Logger.info(message) else Logger.warn(message)
            }
        }, 1L, 20L)
    }

    fun stopping() {
        notices.stop()
    }

    fun restFailure(error: Throwable): Boolean {
        if (filter == null) return false
        var cause: Throwable? = error
        val seen = Collections.newSetFromMap(IdentityHashMap<Throwable, Boolean>())
        while (cause != null && seen.add(cause)) {
            if (cause is UnknownHostException) {
                notices.failure(NetworkNotices.Kind.REST, "Discord's address could not be resolved")
                return true
            }
            val classified = JdaNetworkLogFilter.classify(
                JdaNetworkLogFilter.REQUESTER,
                "There was an I/O error while executing a REST request: ${cause.message}"
            )
            if (classified != null) {
                notices.failure(classified.first, classified.second); return true
            }
            cause = cause.cause
        }
        return false
    }

    override fun onSessionResume(event: SessionResumeEvent) = notices.recovered()
    override fun onSessionRecreate(event: SessionRecreateEvent) = notices.recovered()
    fun close() {
        notices.stop()
        task?.cancel()
        task = null
        filter?.let { it.deactivate(); context?.removeFilter(it); it.stop() }
        filter = null
        context = null
    }
}
