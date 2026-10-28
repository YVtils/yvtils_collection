/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.stats.logic

import org.bukkit.command.CommandSender
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy

class StatsAdministrationTest {
    private fun sender(operator: Boolean, permissions: Set<String> = emptySet()): CommandSender =
        Proxy.newProxyInstance(CommandSender::class.java.classLoader, arrayOf(CommandSender::class.java)) { _, method, args ->
            when (method.name) {
                "isOp" -> operator
                "hasPermission" -> args?.get(0) in permissions
                "toString" -> "test-sender"
                else -> throw UnsupportedOperationException(method.name)
            }
        } as CommandSender

    @Test
    fun `operators can configure without explicit permission attachments`() {
        assertTrue(StatsAdministration.canConfigure(sender(true)))
    }

    @Test
    fun `explicit manage or legacy optin permission permits administration`() {
        assertTrue(StatsAdministration.canConfigure(sender(false, setOf("yvtils.stats.manage"))))
        assertTrue(StatsAdministration.canConfigure(sender(false, setOf("yvtils.stats.optin"))))
    }

    @Test
    fun `ordinary players cannot change settings or consent`() {
        assertFalse(StatsAdministration.canConfigure(sender(false)))
    }
}
