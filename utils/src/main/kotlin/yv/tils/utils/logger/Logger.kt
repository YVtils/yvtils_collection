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

        @Deprecated("Use debug(message: String, level: Int) instead", ReplaceWith("debug(message, level)"))
        fun debug(message: String, level: Int = -1) {
            if (debugMode && (level == -1 || level <= debugLevel)) {
                logger?.info("[$level] $message")
            }
        }

        @Deprecated("Use debug(message: String, level: Int) instead", ReplaceWith("debug(\"\", level, message)"))
        fun debug(message: Component, level: Int = -1) {
            if (debugMode && (level == -1 || level <= debugLevel)) {
                logger?.info(MiniMessage.miniMessage().deserialize("[$level]").append(message))
            }
        }

        fun debug(message: String, level: DEBUGLEVEL, cMessage: Component? = null) {
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

        fun debug(message: String, throwable: Throwable, level: DEBUGLEVEL) {
            val levelInt = level.level

            if (debugMode && (levelInt == -1 || levelInt <= debugLevel)) {
                logger?.error("[$level] $message", throwable)
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

enum class DEBUGLEVEL(val level: Int) {
    NONE(0),
    BASIC(1),
    DETAILED(2),
    VERBOSE(3),
    EXTRA(4),
    SPAM(5);
}
