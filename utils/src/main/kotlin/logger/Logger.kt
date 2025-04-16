package logger

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.kyori.adventure.text.minimessage.MiniMessage

class Logger {
    companion object {
        var logger: ComponentLogger? = null
        private var debugMode = true
        private var debugLevel = 3


        fun setDebugMode(enabled: Boolean, level: Int) {
            debugMode = enabled
            debugLevel = level
        }

        fun dev(message: String) {
            logger?.info(message)
        }

        fun debug(message: String, level: Int = -1) {
            if (debugMode && (level == -1 || level <= debugLevel)) {
                logger?.info("[$level] $message")
            }
        }

        fun debug(message: Component, level: Int = -1) {
            if (debugMode && (level == -1 || level <= debugLevel)) {
                logger?.info(MiniMessage.miniMessage().deserialize("[$level]").append(message))
            }
        }

        fun info(message: String) {
            logger?.info(message)
        }

        fun info(message: Component) {
            logger?.info(message)
        }

        fun warn(message: String) {
            logger?.warn(message)
        }

        fun warn(message: Component) {
            logger?.warn(message)
        }

        fun error(message: String) {
            logger?.error(message)
        }

        fun error(message: Component) {
            logger?.error(message)
        }

        fun log(level: Level, message: String) {
            when (level) {
                Level.INFO -> info(message)
                Level.WARN -> warn(message)
                Level.ERROR -> error(message)
                Level.DEBUG -> debug(message)
            }
        }

        fun log(level: Level, message: Component) {
            when (level) {
                Level.INFO -> info(message)
                Level.WARN -> warn(message)
                Level.ERROR -> error(message)
                Level.DEBUG -> debug(message)
            }
        }

        enum class Level {
            INFO,
            WARN,
            ERROR,
            DEBUG
        }
    }
}