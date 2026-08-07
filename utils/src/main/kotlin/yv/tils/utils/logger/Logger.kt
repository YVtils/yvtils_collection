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

package yv.tils.utils.logger

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.kyori.adventure.text.minimessage.MiniMessage

class Logger {
    companion object {
        var logger: ComponentLogger? = null
        private var debugMode = false
        private var debugLevel = 0

        fun setDebugMode(enabled: Boolean, level: Int) {
            debugMode = enabled
            debugLevel = level
        }

        fun dev(message: String) {
            logger?.info("[DEV] $message")
        }

        fun debug(message: String, level: DEBUG_LEVEL = DEBUG_LEVEL.BASIC, cMessage: Component? = null) {
            val levelInt = level.level

            if (message != "") {
                if (debugMode && (levelInt == -1 || levelInt <= debugLevel)) {
                    logger?.info("[$level] $message")
                }
            }

            if (cMessage != null) {
                if (debugMode && (levelInt == -1 || levelInt <= debugLevel)) {
                    logger?.info(MiniMessage.miniMessage().deserialize("[$level]").append(cMessage))
                }
            }
        }

        /**
         * Convenience overload for call sites still passing a raw debug level integer
         * instead of a [DEBUG_LEVEL] value.
         */
        fun debug(message: String, level: Int, cMessage: Component? = null) {
            debug(message, DEBUG_LEVEL.entries.find { it.level == level } ?: DEBUG_LEVEL.BASIC, cMessage)
        }

        fun debug(message: String, level: DEBUG_LEVEL = DEBUG_LEVEL.BASIC, throwable: Throwable) {
            val levelInt = level.level

            if (debugMode && (levelInt == -1 || levelInt <= debugLevel)) {
                logger?.info("[$level] $message", throwable)
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

        fun error(message: String, throwable: Throwable) {
            logger?.error(message, throwable)
        }
    }
}

/**
 * DEBUG LEVELS:
 * NONE: No debug messages will be logged.
 * BASIC: Only basic debug messages will be logged.
 * DETAILED: More detailed debug messages will be logged, including additional context.
 * VERBOSE: All debug messages will be logged, including very detailed information.
 * EXTRA: Logs all debug messages, including extremely detailed information that may be useful for in-depth troubleshooting.
 * SPAM: Logs all debug messages, including potentially excessive information that may be overwhelming but can be useful for diagnosing very specific issues.
 */
enum class DEBUG_LEVEL(val level: Int) {
    NONE(0),
    BASIC(1),
    DETAILED(2),
    VERBOSE(3),
    EXTRA(4),
    SPAM(5);
}
