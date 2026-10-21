package yv.tils.message.logic

import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import yv.tils.message.data.Permissions
import yv.tils.utils.modules.Core
import java.util.UUID

class MessageHandlerTest {
    private val handler = MessageHandler()

    @BeforeEach
    fun setup() {
        val plugin = mock(JavaPlugin::class.java)
        Core.instance = plugin
        Core.core = Core.YVtilsCore(
            name = "Test", version = "test", instance = plugin,
            key = mock(NamespacedKey::class.java), pluginShort = "test"
        )
        handler.clearSessions()
    }

    @AfterEach
    fun cleanup() {
        handler.clearSessions()
    }

    @Test
    fun `receive denial blocks direct messages and replies without changing sessions`() {
        val sender = player()
        val target = player(canReceive = false)
        val previousContact = UUID.randomUUID()
        MessageHandler.chatSession[sender.uniqueId] = previousContact

        handler.sendMessage(sender, target, "hello")

        verify(target, never()).sendMessage(any(Component::class.java))
        verify(sender).sendMessage(any(Component::class.java))
        assertEquals(mapOf(sender.uniqueId to previousContact), MessageHandler.chatSession)
    }

    @Test
    fun `multiple recipients require all permission before any delivery`() {
        val sender = player()
        val first = player()
        val second = player()

        handler.sendMessages(sender, listOf(first, second), "hello")

        verify(first, never()).sendMessage(any(Component::class.java))
        verify(second, never()).sendMessage(any(Component::class.java))
        assertTrue(MessageHandler.chatSession.isEmpty())
    }

    @Test
    fun `all selector requires permission even when it resolves to one player`() {
        val sender = player()
        val target = player()

        handler.sendMessages(sender, listOf(target), "hello", requiresAllPermission = true)

        verify(target, never()).sendMessage(any(Component::class.java))
        assertTrue(MessageHandler.chatSession.isEmpty())
    }

    @Test
    fun `authorized multiple delivery skips blocked receivers and establishes reply sessions`() {
        val sender = player()
        `when`(sender.hasPermission(Permissions.COMMAND_MSG_ALL.permission.name)).thenReturn(true)
        val first = player()
        val blocked = player(canReceive = false)
        val last = player()

        handler.sendMessages(sender, listOf(first, blocked, last), "hello")

        verify(first).sendMessage(any(Component::class.java))
        verify(blocked, never()).sendMessage(any(Component::class.java))
        verify(last).sendMessage(any(Component::class.java))
        assertEquals(sender.uniqueId, MessageHandler.chatSession[first.uniqueId])
        assertEquals(sender.uniqueId, MessageHandler.chatSession[last.uniqueId])
        assertEquals(last.uniqueId, MessageHandler.chatSession[sender.uniqueId])
    }

    @Test
    fun `single recipient does not require all permission`() {
        val sender = player()
        val target = player()

        handler.sendMessages(sender, listOf(target), "hello")

        verify(target).sendMessage(any(Component::class.java))
        assertEquals(target.uniqueId, MessageHandler.chatSession[sender.uniqueId])
        assertEquals(sender.uniqueId, MessageHandler.chatSession[target.uniqueId])
    }

    @Test
    fun `quitting after messaging two players removes every reference to the departing player`() {
        val first = player()
        val second = player()
        val third = player()
        handler.sendMessage(first, second, "hello")
        handler.sendMessage(first, third, "hello again")

        handler.removeSession(first.uniqueId)

        assertTrue(MessageHandler.chatSession.isEmpty())
    }

    @Test
    fun `quitting does not remove another players newer reply target`() {
        val first = player()
        val second = player()
        val third = player()
        handler.sendMessage(first, second, "hello")
        handler.sendMessage(second, third, "hello again")

        handler.removeSession(first.uniqueId)

        assertEquals(
            mapOf(second.uniqueId to third.uniqueId, third.uniqueId to second.uniqueId),
            MessageHandler.chatSession
        )
    }

    @Test
    fun `cleanup removes incoming references even without an outgoing session`() {
        val departing = UUID.randomUUID()
        MessageHandler.chatSession[UUID.randomUUID()] = departing
        MessageHandler.chatSession[UUID.randomUUID()] = departing

        handler.removeSession(departing)
        handler.removeSession(departing)

        assertTrue(MessageHandler.chatSession.isEmpty())
    }

    private fun player(canReceive: Boolean = true): Player = mock(Player::class.java).also {
        `when`(it.uniqueId).thenReturn(UUID.randomUUID())
        `when`(it.name).thenReturn("Player")
        `when`(it.hasPermission(Permissions.RECEIVE.permission.name)).thenReturn(canReceive)
    }
}
