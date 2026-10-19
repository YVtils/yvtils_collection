/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.gui.utils

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/** Server-thread-owned bounded cache; asynchronous completions never mutate its map. */
internal class HeadProfileCache<K, V : Any>(
    private val capacity: Int = 512,
    private val clock: () -> Long = System::nanoTime,
    private val usable: (V) -> Boolean,
    private val load: (K) -> CompletableFuture<V>,
) {
    private data class Entry<V>(val future: CompletableFuture<V>, val created: Long)

    private val entries = linkedMapOf<K, Entry<V>>()

    private fun value(entry: Entry<V>): V? = entry.future
        .takeUnless { it.isCompletedExceptionally || it.isCancelled }?.getNow(null)?.takeIf(usable)

    private fun prune() {
        val now = clock()
        entries.entries.removeIf { (_, entry) ->
            val ttl = if (entry.future.isDone && value(entry) == null) 1L else 10L
            now - entry.created >= TimeUnit.MINUTES.toNanos(ttl)
        }
    }

    fun peek(key: K): V? {
        prune()
        return entries[key]?.let(::value)
    }

    fun resolve(key: K): CompletableFuture<V> {
        prune()
        entries[key]?.let { return it.future }
        if (entries.size >= capacity) entries.remove(entries.keys.first())
        val future = try {
            load(key)
        } catch (error: Exception) {
            CompletableFuture.failedFuture(error)
        }
        entries[key] = Entry(future, clock())
        return future
    }

    fun clear() = entries.clear()
}
