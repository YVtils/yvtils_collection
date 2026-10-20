package yv.tils.discord.configs

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DiscordConfigStateTest {
    @Test
    fun `nested editor changes do not affect the live config or saved snapshot`() {
        val live = DiscordConfigState()
        val snapshot = live.detachedCopy()
        val draft = snapshot.detachedCopy()
        draft.botSettings.activity = "watching"
        draft.syncFeature.serverStats.design.status.emoji.online = "changed"
        draft.syncFeature.chatSync.embedIcon.limit = 100
        draft.whitelistFeature.settings.legacyMessageRegistration = false
        draft.commands.whitelistCommand.permission = "changed"
        assertEquals(DiscordConfigState(), live)
        assertEquals(live, snapshot)
        assertNotEquals(snapshot, draft)
    }
}
