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

package yv.tils.discord.connection

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.impl.Log4jLogEvent
import org.apache.logging.log4j.message.SimpleMessage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JdaNetworkLogFilterTest {
    private val route = "There was an I/O error while executing a REST request: No route to host"
    private fun event(logger: String, message: String) = Log4jLogEvent.newBuilder()
        .setLoggerName(logger).setLevel(Level.ERROR).setMessage(SimpleMessage(message)).build()

    @Test
    fun `Log4j accepts null argument arrays from SLF4J and component loggers`() {
        val context = LoggerContext("yvtils-null-arguments")
        val notices = NetworkNotices()
        val filter = JdaNetworkLogFilter(notices) { true }
        context.addFilter(filter)
        try {
            val unrelated = context.getLogger("another.plugin")
            assertEquals(
                Filter.Result.NEUTRAL,
                filter.filter(unrelated, Level.INFO, null, "hello", null as Array<Any?>?)
            )
            assertDoesNotThrow {
                unrelated.isEnabled(Level.ERROR, null, "startup failed", null as Array<Any?>?)
                unrelated.isEnabled(Level.INFO, null, "server ready", null as Array<Any?>?)
            }
            assertEquals(
                Filter.Result.DENY,
                filter.filter(
                    context.getLogger(JdaNetworkLogFilter.REQUESTER),
                    Level.ERROR,
                    null,
                    route,
                    null as Array<Any?>?
                )
            )
            assertEquals(1, notices.drain().size)
        } finally {
            context.stop()
        }
    }

    @Test
    fun `only known transport diagnostics are replaced`() {
        val notices = NetworkNotices()
        val filter = JdaNetworkLogFilter(notices) { true }
        assertEquals(Filter.Result.DENY, filter.filter(event(JdaNetworkLogFilter.REQUESTER, route)))
        assertEquals(Filter.Result.NEUTRAL, filter.filter(event("another.plugin", route)))
        assertEquals(
            Filter.Result.NEUTRAL, filter.filter(
                event(
                    JdaNetworkLogFilter.REQUESTER,
                    "There was an unexpected error while executing a REST request"
                )
            )
        )
        assertEquals(
            Filter.Result.NEUTRAL, filter.filter(
                event(
                    JdaNetworkLogFilter.REQUESTER,
                    "There was an I/O error while executing a REST request: PKIX path building failed"
                )
            )
        )
        assertEquals(
            Filter.Result.NEUTRAL, filter.filter(
                event(
                    JdaNetworkLogFilter.SOCKET,
                    "WebSocket connection was closed and cannot be recovered due to identification issues"
                )
            )
        )
        assertEquals(
            Filter.Result.DENY, filter.filter(
                event(
                    JdaNetworkLogFilter.SOCKET,
                    "Got disconnected from WebSocket (Code 1000). Appending to reconnect queue"
                )
            )
        )
        assertEquals(2, notices.drain().size)
    }

    @Test
    fun `other JDA instances and deactivated filters leave logs alone`() {
        val notices = NetworkNotices()
        assertEquals(Filter.Result.NEUTRAL, JdaNetworkLogFilter(notices) { false }
            .filter(event(JdaNetworkLogFilter.REQUESTER, route)))
        val filter = JdaNetworkLogFilter(notices) { true }
        filter.deactivate()
        assertEquals(Filter.Result.NEUTRAL, filter.filter(event(JdaNetworkLogFilter.REQUESTER, route)))
        assertTrue(notices.drain().isEmpty())
    }

    @Test
    fun `real parameterized Log4j calls are intercepted without changing unrelated logger configuration`() {
        val context = LoggerContext("yvtils-network-test")
        val notices = NetworkNotices()
        val filter = JdaNetworkLogFilter(notices) { true }
        val captured = mutableListOf<String>()
        val appender = object : AbstractAppender("capture", null, null, true, null) {
            override fun append(event: LogEvent) {
                captured += event.message.formattedMessage
            }
        }
        appender.start()
        context.configuration.rootLogger.addAppender(appender, Level.ALL, null)
        context.configuration.rootLogger.level = Level.ALL
        context.addFilter(filter)
        context.updateLoggers()
        try {
            val logger = context.getLogger(JdaNetworkLogFilter.REQUESTER)
            logger.error("There was an I/O error while executing a REST request: {}", "No route to host")
            logger.error("Unexpected authentication failure")
            assertEquals(listOf("Unexpected authentication failure"), captured)
            assertEquals(1, notices.drain().size)
            context.removeFilter(filter)
            logger.error(route)
            assertEquals(route, captured.last())
        } finally {
            context.stop()
        }
    }
}
