package yv.tils.discord.logic

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class ShutdownUpdatesTest {
    @Test
    fun `waits for every final update before returning`() {
        val first = CompletableFuture<Void>()
        val second = CompletableFuture<Void>()
        val waiter = CompletableFuture.runAsync {
            awaitShutdownUpdates(listOf(first, second), Duration.ofSeconds(5))
        }
        first.complete(null)
        assertFalse(waiter.isDone)
        second.complete(null)
        waiter.get(5, TimeUnit.SECONDS)
        assertFalse(first.isCancelled)
        assertFalse(second.isCancelled)
    }

    @Test
    fun `one shared deadline cancels all outstanding updates`() {
        val completed = CompletableFuture.completedFuture(null)
        val pending = List(3) { CompletableFuture<Void>() }
        assertThrows(TimeoutException::class.java) {
            awaitShutdownUpdates(pending + completed, Duration.ofMillis(20))
        }
        assertTrue(pending.all { it.isCancelled })
        assertFalse(completed.isCancelled)
    }

    @Test
    fun `failed update still allows other updates to finish and reports failure`() {
        val failed = CompletableFuture.failedFuture<Void>(IllegalStateException("Discord rejected update"))
        val completed = CompletableFuture.completedFuture(null)
        val error = assertThrows(ExecutionException::class.java) {
            awaitShutdownUpdates(listOf(failed, completed), Duration.ofSeconds(1))
        }
        assertEquals("Discord rejected update", error.cause?.message)
        assertFalse(completed.isCancelled)
    }

    @Test
    fun `interruption cancels pending updates and propagates to shutdown owner`() {
        val pending = CompletableFuture<Void>()
        Thread.currentThread().interrupt()
        try {
            assertThrows(InterruptedException::class.java) {
                awaitShutdownUpdates(listOf(pending), Duration.ofSeconds(1))
            }
            assertTrue(pending.isCancelled)
        } finally {
            Thread.interrupted()
        }
    }
}
