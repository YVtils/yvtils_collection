package yv.tils.moderation.listeners

import com.destroystokyo.paper.profile.PlayerProfile
import io.papermc.paper.ban.BanListType
import io.papermc.paper.connection.PlayerLoginConnection
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.BanEntry
import org.bukkit.ban.ProfileBanList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.Date
import java.util.UUID

class BannedLoginTest {
    @Test
    fun `expired bans and unrelated denials keep their original message`() {
        val profile = mock(PlayerProfile::class.java)
        `when`(profile.id).thenReturn(UUID.randomUUID())
        val connection = mock(PlayerLoginConnection::class.java)
        `when`(connection.authenticatedProfile).thenReturn(profile)
        val bans = mock(ProfileBanList::class.java)
        val original = Component.text("Another plugin denied login")
        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<ProfileBanList> { Bukkit.getBanList(BanListType.PROFILE) }.thenReturn(bans)
            val event = PlayerConnectionValidateLoginEvent(connection, original)
            BannedLogin().validate(event)
            assertEquals(original, event.kickMessage)
            @Suppress("UNCHECKED_CAST")
            val entry = mock(BanEntry::class.java) as BanEntry<PlayerProfile>
            `when`(bans.isBanned(profile)).thenReturn(true)
            `when`(bans.getBanEntry(profile)).thenReturn(entry)
            `when`(entry.expiration).thenReturn(Date(1))
            BannedLogin().validate(event)
            assertEquals(original, event.kickMessage)
            assertFalse(event.isAllowed)
        }
    }
}
