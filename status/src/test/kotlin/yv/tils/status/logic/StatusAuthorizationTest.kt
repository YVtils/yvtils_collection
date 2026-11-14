/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.status.logic

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import yv.tils.status.data.Permissions
import java.lang.reflect.Proxy
import java.util.UUID

class StatusAuthorizationTest {
    private val targetId = UUID.randomUUID()
    private val target = player(targetId)

    @Test
    fun `self changes require command access and the selected action but not other player management`() {
        for (action in listOf(Permissions.COMMAND_STATUS_SET, Permissions.COMMAND_STATUS_DEFAULT, Permissions.COMMAND_STATUS_CLEAR)) {
            assertFalse(StatusAuthorization.canChange(player(targetId), target, action))
            assertFalse(StatusAuthorization.canChange(player(targetId, Permissions.COMMAND_STATUS), target, action))
            assertFalse(StatusAuthorization.canChange(player(targetId, action), target, action))
            assertTrue(StatusAuthorization.canChange(player(targetId, Permissions.COMMAND_STATUS, action), target, action))
        }
    }

    @Test
    fun `setting another player requires manage others as well as the set action`() {
        val id = UUID.randomUUID()
        assertFalse(StatusAuthorization.canChange(player(id, Permissions.COMMAND_STATUS, Permissions.COMMAND_STATUS_SET),
            target, Permissions.COMMAND_STATUS_SET))
        assertFalse(StatusAuthorization.canChange(player(id, Permissions.COMMAND_STATUS, Permissions.COMMAND_STATUS_MANAGE_OTHERS),
            target, Permissions.COMMAND_STATUS_SET))
        assertTrue(StatusAuthorization.canChange(player(id, Permissions.COMMAND_STATUS, Permissions.COMMAND_STATUS_SET,
            Permissions.COMMAND_STATUS_MANAGE_OTHERS), target, Permissions.COMMAND_STATUS_SET))
    }

    @Test
    fun `default selection cannot borrow custom set or clear others permissions`() {
        val id = UUID.randomUUID()
        assertFalse(StatusAuthorization.canChange(player(id, Permissions.COMMAND_STATUS, Permissions.COMMAND_STATUS_SET,
            Permissions.COMMAND_STATUS_MANAGE_OTHERS), target, Permissions.COMMAND_STATUS_DEFAULT))
        assertFalse(StatusAuthorization.canChange(player(id, Permissions.COMMAND_STATUS, Permissions.COMMAND_STATUS_DEFAULT,
            Permissions.COMMAND_STATUS_CLEAR_OTHERS), target, Permissions.COMMAND_STATUS_DEFAULT))
        assertTrue(StatusAuthorization.canChange(player(id, Permissions.COMMAND_STATUS, Permissions.COMMAND_STATUS_DEFAULT,
            Permissions.COMMAND_STATUS_MANAGE_OTHERS), target, Permissions.COMMAND_STATUS_DEFAULT))
    }

    @Test
    fun `clearing others retains its separate permission rather than accepting manage others`() {
        val id = UUID.randomUUID()
        assertFalse(StatusAuthorization.canChange(player(id, Permissions.COMMAND_STATUS, Permissions.COMMAND_STATUS_CLEAR,
            Permissions.COMMAND_STATUS_MANAGE_OTHERS), target, Permissions.COMMAND_STATUS_CLEAR))
        assertTrue(StatusAuthorization.canChange(player(id, Permissions.COMMAND_STATUS, Permissions.COMMAND_STATUS_CLEAR,
            Permissions.COMMAND_STATUS_CLEAR_OTHERS), target, Permissions.COMMAND_STATUS_CLEAR))
    }

    @Test
    fun `console can change a target with the same required action and others permissions`() {
        for (action in listOf(Permissions.COMMAND_STATUS_SET, Permissions.COMMAND_STATUS_DEFAULT)) {
            assertFalse(StatusAuthorization.canChange(console(Permissions.COMMAND_STATUS, action), target, action))
            assertTrue(StatusAuthorization.canChange(console(Permissions.COMMAND_STATUS, action,
                Permissions.COMMAND_STATUS_MANAGE_OTHERS), target, action))
        }
        assertTrue(StatusAuthorization.canChange(console(Permissions.COMMAND_STATUS, Permissions.COMMAND_STATUS_CLEAR,
            Permissions.COMMAND_STATUS_CLEAR_OTHERS), target, Permissions.COMMAND_STATUS_CLEAR))
    }

    @Test
    fun `self identity is based on UUID rather than player object or name`() {
        assertTrue(StatusAuthorization.canChange(player(targetId, Permissions.COMMAND_STATUS, Permissions.COMMAND_STATUS_SET),
            target, Permissions.COMMAND_STATUS_SET))
        assertFalse(StatusAuthorization.canChange(player(UUID.randomUUID(), Permissions.COMMAND_STATUS, Permissions.COMMAND_STATUS_SET),
            target, Permissions.COMMAND_STATUS_SET))
    }

    @Test
    fun `other player management remains operator only by default`() {
        assertFalse(Permissions.COMMAND_STATUS_MANAGE_OTHERS.permission.default)
        assertFalse(Permissions.COMMAND_STATUS_CLEAR_OTHERS.permission.default)
    }

    private fun player(id: UUID, vararg permissions: Permissions): Player = stub(Player::class.java, id, permissions)
    private fun console(vararg permissions: Permissions): CommandSender = stub(CommandSender::class.java, null, permissions)

    private fun <T> stub(type: Class<T>, id: UUID?, permissions: Array<out Permissions>): T {
        val granted = permissions.map { it.permission.name }.toSet()
        return type.cast(Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { proxy, method, args ->
            when (method.name) {
                "getUniqueId" -> id
                "getName" -> "Alex"
                "hasPermission" -> args!![0] in granted
                "equals" -> proxy === args?.get(0)
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "StatusSenderStub"
                else -> error("Unexpected sender call: ${method.name}")
            }
        })
    }
}
