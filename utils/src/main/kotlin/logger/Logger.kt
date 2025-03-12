package logger

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.logger.slf4j.ComponentLogger

class Logger {
    companion object {
        var logger: ComponentLogger? = null
        private var debugMode = false

        fun setDebugMode(enabled: Boolean) {
            debugMode = enabled
        }

        fun debug(message: String) {
            if (debugMode) {
                logger?.info(message)
            }
        }

        fun debug(message: Component) {
            if (debugMode) {
                logger?.info(message)
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