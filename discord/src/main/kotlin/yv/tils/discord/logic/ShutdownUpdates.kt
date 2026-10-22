package yv.tils.discord.logic

import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/** One shared shutdown budget, including rate limits; never leave final mutations queued. */
internal fun awaitShutdownUpdates(updates: List<CompletableFuture<*>>, timeout: Duration) {
    try {
        CompletableFuture.allOf(*updates.toTypedArray()).get(timeout.toNanos(), TimeUnit.NANOSECONDS)
    } finally {
        updates.filterNot { it.isDone }.forEach { it.cancel(true) }
    }
}
