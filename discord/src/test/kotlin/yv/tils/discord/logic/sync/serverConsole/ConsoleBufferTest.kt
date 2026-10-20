package yv.tils.discord.logic.sync.serverConsole

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ConsoleBufferTest {
    @Test
    fun `uncertain delivery is not replayed and newer output is retained`() {
        val buffer = ConsoleBuffer()
        buffer.offer(Long.MAX_VALUE, "uncertain")
        val batch = buffer.next()!!
        buffer.offer(Long.MAX_VALUE, "later")
        buffer.failed(batch)
        val later = buffer.next()!!
        assertEquals("later", later.content)
        assertNull(later.messageId)
    }

    @Test
    fun `reset during send cannot resurrect old output or consume new entries`() {
        val buffer = ConsoleBuffer()
        buffer.offer(Long.MAX_VALUE, "old")
        val old = buffer.next()!!
        buffer.clear()
        buffer.offer(Long.MAX_VALUE, "new")
        buffer.sent(old, "old-id")
        val next = buffer.next()!!
        assertEquals("new", next.content)
        assertNull(next.messageId)
        buffer.sent(next, "new-id")
        assertNull(buffer.next())
    }

    @Test
    fun `history edits latest message and rotates within Discord size limit`() {
        val buffer = ConsoleBuffer()
        buffer.offer(Long.MAX_VALUE, "a".repeat(1000))
        buffer.sent(buffer.next()!!, "first")
        buffer.offer(Long.MAX_VALUE, "b".repeat(100))
        val edit = buffer.next()!!
        assertEquals("first", edit.messageId)
        assertEquals(1100, edit.content.length)
        buffer.sent(edit, "first")
        buffer.offer(Long.MAX_VALUE, "c".repeat(1000))
        val fresh = buffer.next()!!
        assertNull(fresh.messageId)
        assertEquals("c".repeat(1000), fresh.content)
    }

    @Test
    fun `oversized logs are split and producer backlog is strictly bounded`() {
        val buffer = ConsoleBuffer(2)
        buffer.offer(Long.MAX_VALUE, "x".repeat(ConsoleBuffer.MAX_CONTENT * 4))
        repeat(2) {
            val batch = buffer.next()!!
            assertEquals(ConsoleBuffer.MAX_CONTENT, batch.content.length)
            buffer.sent(batch, "$it")
        }
        assertNull(buffer.next())
    }

    @Test
    fun `concurrent logging and resets preserve valid batches`() {
        val buffer = ConsoleBuffer(20)
        val workers = Executors.newFixedThreadPool(3)
        try {
            val logging = workers.submit { repeat(2000) { buffer.offer(Long.MAX_VALUE, "log\n") } }
            val resets = workers.submit { repeat(500) { buffer.clear() } }
            val sending = workers.submit {
                repeat(2000) {
                    buffer.next()?.let { batch ->
                        assertTrue(batch.content.length <= ConsoleBuffer.MAX_CONTENT)
                        buffer.sent(batch, "id")
                    }
                }
            }
            listOf(logging, resets, sending).forEach { it.get(10, TimeUnit.SECONDS) }
        } finally {
            workers.shutdownNow()
        }
    }
}
