package yv.tils.discord.logic.sync.serverConsole

/** Bounded console history. Network calls operate on snapshots, never under this lock. */
internal class ConsoleBuffer(private val capacity: Int = 1000) {
    companion object {
        const val MAX_CONTENT = 1890
    }

    data class Batch(val generation: Long, val content: String, val messageId: String?, val entries: Int)

    private val pending = ArrayDeque<String>()
    private var generation = 0L
    private var content = ""
    private var messageId: String? = null
    private var since = System.currentTimeMillis()

    @Synchronized
    fun offer(timeMillis: Long, text: String) {
        if (timeMillis < since) return
        for (chunk in text.chunked(MAX_CONTENT)) {
            if (pending.size >= capacity) break
            pending.addLast(chunk)
        }
    }

    @Synchronized
    fun next(): Batch? {
        if (pending.isEmpty()) return null
        val reuse = content.length + pending.first().length <= MAX_CONTENT
        val next = StringBuilder(if (reuse) content else "")
        var entries = 0
        for (line in pending) {
            if (next.length + line.length > MAX_CONTENT) break
            next.append(line)
            entries++
        }
        return Batch(generation, next.toString(), if (reuse) messageId else null, entries)
    }

    @Synchronized
    fun sent(batch: Batch, id: String) {
        // A reset while REST was in flight must not resurrect the old buffer/message.
        if (batch.generation != generation) return
        repeat(batch.entries) { pending.removeFirst() }
        content = batch.content
        messageId = id
    }

    @Synchronized
    fun failed(batch: Batch) {
        if (batch.generation != generation) return
        repeat(batch.entries) { pending.removeFirst() }
        content = ""
        messageId = null
    }

    @Synchronized
    fun clear() {
        generation++
        pending.clear()
        content = ""
        messageId = null
        since = System.currentTimeMillis()
    }
}
