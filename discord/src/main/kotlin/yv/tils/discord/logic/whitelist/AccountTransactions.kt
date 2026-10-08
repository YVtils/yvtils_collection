package yv.tils.discord.logic.whitelist

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Serializes validation through completion. Cancellation cannot interrupt a storage transaction. */
class AccountTransactions {
    private val mutex = Mutex()
    suspend fun <T> exclusive(action: suspend () -> T): T = mutex.withLock {
        withContext(NonCancellable) { action() }
    }

    suspend fun persist(change: suspend () -> Unit, save: () -> Unit, rollback: suspend () -> Unit) {
        try {
            change()
            save()
        } catch (failure: Exception) {
            try {
                rollback()
            } catch (rollbackFailure: Exception) {
                failure.addSuppressed(rollbackFailure)
            }
            throw failure
        }
    }
}
