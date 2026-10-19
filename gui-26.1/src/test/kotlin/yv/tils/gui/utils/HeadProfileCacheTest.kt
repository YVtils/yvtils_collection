/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.gui.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class HeadProfileCacheTest {
    @Test
    fun `concurrent inventory requests share a lookup and reuse its result`() {
        var requests = 0
        val pending = CompletableFuture<String>()
        val cache = HeadProfileCache<String, String>(usable = { it.isNotEmpty() }, load = { requests++; pending })
        val first = cache.resolve("player")
        assertSame(first, cache.resolve("player"))
        assertNull(cache.peek("player"))
        pending.complete("skin")
        assertEquals("skin", cache.peek("player"))
        assertSame(first, cache.resolve("player"))
        assertEquals(1, requests)
    }

    @Test
    fun `failed cancelled and textureless results retry after the short cooldown`() {
        listOf(
            CompletableFuture.failedFuture<String>(IllegalStateException("unavailable")),
            CompletableFuture<String>().apply { cancel(false) },
            CompletableFuture.completedFuture("")
        ).forEach { failed ->
            var now = 0L
            var requests = 0
            val cache = HeadProfileCache<String, String>(clock = { now }, usable = { it.isNotEmpty() }, load = {
                requests++
                if (requests == 1) failed else CompletableFuture.completedFuture("skin")
            })
            cache.resolve("player")
            assertNull(cache.peek("player"))
            assertSame(failed, cache.resolve("player"))
            now = TimeUnit.MINUTES.toNanos(1)
            assertEquals("skin", cache.resolve("player").join())
            assertEquals(2, requests)
        }
    }

    @Test
    fun `successful results expire and capacity evicts the oldest entry`() {
        var now = 0L
        val requests = mutableListOf<String>()
        val cache = HeadProfileCache<String, String>(capacity = 2, clock = { now }, usable = { true }, load = {
            requests += it
            CompletableFuture.completedFuture(it)
        })
        cache.resolve("a")
        cache.resolve("b")
        cache.resolve("c")
        assertNull(cache.peek("a"))
        assertEquals("b", cache.peek("b"))
        now = TimeUnit.MINUTES.toNanos(10)
        assertNull(cache.peek("b"))
        cache.resolve("b")
        assertEquals(listOf("a", "b", "c", "b"), requests)
    }

    @Test
    fun `late completion cannot repopulate a cleared cache`() {
        val pending = CompletableFuture<String>()
        var requests = 0
        val cache = HeadProfileCache<String, String>(usable = { true }, load = {
            requests++
            if (requests == 1) pending else CompletableFuture.completedFuture("new skin")
        })
        cache.resolve("player")
        cache.clear()
        pending.complete("old skin")
        assertNull(cache.peek("player"))
        assertEquals("new skin", cache.resolve("player").join())
    }
}
