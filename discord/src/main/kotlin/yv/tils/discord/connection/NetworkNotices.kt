package yv.tils.discord.connection

/** Fixed-size, thread-safe aggregation; logging is performed by the server ticker, never a log filter. */
class NetworkNotices(private val now: () -> Long = System::nanoTime) {
    enum class Kind { REST, GATEWAY }
    private data class State(var pending: Int = 0, var last: Long? = null, var detail: String = "")

    private val states = Kind.entries.associateWith { State() }
    private var stopping = false
    private var gatewayInterrupted = false
    private var recovery = false

    @Synchronized
    fun failure(kind: Kind, detail: String) {
        if (stopping) return
        val state = states.getValue(kind)
        state.pending = (state.pending + 1).coerceAtMost(1_000_000)
        state.detail = detail
        if (kind == Kind.GATEWAY) gatewayInterrupted = true
    }

    @Synchronized
    fun recovered() {
        if (!stopping && gatewayInterrupted) {
            gatewayInterrupted = false
            recovery = true
        }
    }

    @Synchronized
    fun drain(): List<String> {
        if (stopping) return emptyList()
        val time = now()
        val output = mutableListOf<String>()
        states.forEach { (kind, state) ->
            if (state.pending > 0 && (state.last == null || time - state.last!! >= 60_000_000_000L)) {
                val summary = if (state.pending > 1) " (${state.pending} similar notices grouped)" else ""
                output += when (kind) {
                    Kind.REST -> "[Discord] [Connection] A Discord API request could not be completed: ${state.detail}. " +
                            "Some updates may not have been delivered. Check the server network/DNS if this continues.$summary"

                    Kind.GATEWAY -> "[Discord] [Connection] The Discord live connection was interrupted (${state.detail}). " +
                            "JDA has queued an automatic reconnect; live events may be delayed.$summary"
                }
                state.last = time
                state.pending = 0
            }
        }
        if (recovery) {
            output += "[Discord] [Connection] The Discord live connection has been restored."
            recovery = false
        }
        return output
    }

    @Synchronized
    fun stop() {
        stopping = true; states.values.forEach { it.pending = 0 }; recovery = false
    }
}
