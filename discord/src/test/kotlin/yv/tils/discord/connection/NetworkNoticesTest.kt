package yv.tils.discord.connection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NetworkNoticesTest {
    @Test
    fun `burst is summarized once per minute per category`() {
        var clock = 0L
        val notices = NetworkNotices { clock }
        notices.failure(NetworkNotices.Kind.REST, "route unavailable")
        assertEquals(1, notices.drain().size)
        repeat(100) { notices.failure(NetworkNotices.Kind.REST, "route unavailable") }
        assertTrue(notices.drain().isEmpty())
        clock = 60_000_000_000L
        assertTrue(notices.drain().single().contains("100 similar notices grouped"))
        assertTrue(notices.drain().isEmpty())
    }

    @Test
    fun `gateway recovery does not claim REST delivery or announce planned shutdown`() {
        val notices = NetworkNotices()
        notices.recovered()
        assertTrue(notices.drain().isEmpty())
        notices.failure(NetworkNotices.Kind.GATEWAY, "close code 1000")
        notices.recovered()
        val output = notices.drain()
        assertEquals(2, output.size)
        assertTrue(output.last().contains("live connection has been restored"))
        notices.stop()
        notices.failure(NetworkNotices.Kind.GATEWAY, "close code 1000")
        notices.recovered()
        assertTrue(notices.drain().isEmpty())
    }

    @Test
    fun `concurrent request failures retain aggregate counts`() {
        val notices = NetworkNotices()
        val executor = Executors.newFixedThreadPool(8)
        repeat(1000) { executor.submit { notices.failure(NetworkNotices.Kind.REST, "timeout") } }
        executor.shutdown()
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS))
        assertTrue(notices.drain().single().contains("1000 similar notices grouped"))
    }
}
