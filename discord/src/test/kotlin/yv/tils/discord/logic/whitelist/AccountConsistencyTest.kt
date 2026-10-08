package yv.tils.discord.logic.whitelist

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class AccountConsistencyTest {
    private val original = WhitelistEntry("123", "OldName", "uuid-1")

    @Test
    fun `renamed and differently cased account remains registered by UUID`() {
        assertEquals("already", RegistrationPolicy.conflict(listOf(original), original.copy(minecraftName = "NEWNAME")))
        assertEquals(
            "claimed",
            RegistrationPolicy.conflict(
                listOf(original),
                original.copy(discordUserID = "456", minecraftName = "newname")
            )
        )
        assertEquals(
            "claimed",
            RegistrationPolicy.conflict(
                listOf(original),
                original.copy(discordUserID = "456", minecraftName = "oldname", minecraftUUID = "uuid-2")
            )
        )
    }

    @Test
    fun `replacement requires current snapshot and cannot claim another account`() {
        val other = WhitelistEntry("456", "Other", "uuid-2")
        assertEquals(
            "different",
            RegistrationPolicy.conflict(listOf(original), original.copy(minecraftUUID = "uuid-3"))
        )
        assertEquals(
            "stale",
            RegistrationPolicy.conflict(listOf(other), original.copy(minecraftUUID = "uuid-3"), original)
        )
        assertEquals(
            "claimed",
            RegistrationPolicy.conflict(listOf(original, other), other.copy(discordUserID = "123"), original)
        )
        assertNull(
            RegistrationPolicy.conflict(
                listOf(original, other),
                original.copy(minecraftName = "New", minecraftUUID = "uuid-3"),
                original
            )
        )
    }

    @Test
    fun `concurrent Discord and administrative attempts admit only one owner`() = runBlocking {
        val transactions = AccountTransactions()
        val entries = mutableListOf<WhitelistEntry>()
        val admitted = AtomicInteger()
        coroutineScope {
            (1..40).map { user ->
                async(Dispatchers.Default) {
                    transactions.exclusive {
                        val candidate = original.copy(discordUserID = user.toString())
                        if (RegistrationPolicy.conflict(entries.toList(), candidate) == null) {
                            delay(2)
                            entries += candidate
                            admitted.incrementAndGet()
                        }
                    }
                }
            }.awaitAll()
        }
        assertEquals(1, admitted.get())
        assertEquals(1, entries.size)
    }

    @Test
    fun `failed persistence restores whitelist and releases serialization guard`() = runBlocking {
        val transactions = AccountTransactions()
        var oldAccess = true
        var newAccess = false
        var saved = original
        val failure = runCatching {
            transactions.exclusive {
                transactions.persist(
                    { oldAccess = false; newAccess = true },
                    { error("disk full") },
                    { oldAccess = true; newAccess = false })
            }
        }.exceptionOrNull()
        assertNotNull(failure)
        assertTrue(oldAccess)
        assertFalse(newAccess)
        assertEquals(original, saved)
        withTimeout(1000) {
            transactions.exclusive {
                transactions.persist(
                    { oldAccess = false; newAccess = true },
                    { saved = original.copy(minecraftUUID = "uuid-2") },
                    {})
            }
        }
        assertFalse(oldAccess)
        assertTrue(newAccess)
        assertEquals("uuid-2", saved.minecraftUUID)
    }

    @Test
    fun `role failure keeps committed account and later operations can proceed`() = runBlocking {
        val transactions = AccountTransactions()
        var saved: WhitelistEntry? = null
        val failure = runCatching {
            transactions.exclusive {
                transactions.persist({}, { saved = original }, { saved = null })
                error("Discord denied role assignment")
            }
        }.exceptionOrNull()
        assertNotNull(failure)
        assertEquals(original, saved)
        withTimeout(1000) { transactions.exclusive { saved = null } }
        assertNull(saved)
    }

    @Test fun `cancellation does not abandon whitelist change before persistence`() = runBlocking {
        val transactions = AccountTransactions()
        val changed = CompletableDeferred<Unit>()
        val finish = CompletableDeferred<Unit>()
        var saved = false
        val job = launch {
            transactions.exclusive {
                transactions.persist({ changed.complete(Unit); finish.await() }, { saved = true }, {})
            }
        }
        changed.await()
        job.cancel()
        finish.complete(Unit)
        job.join()
        assertTrue(saved)
        withTimeout(1000) { transactions.exclusive { assertTrue(saved) } }
    }
}
