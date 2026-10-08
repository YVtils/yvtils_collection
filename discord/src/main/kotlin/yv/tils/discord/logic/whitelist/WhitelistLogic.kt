package yv.tils.discord.logic.whitelist

import kotlinx.serialization.Serializable
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.logic.AppLogic

class WhitelistLogic {
    companion object {
        @Volatile
        var whitelistMap: Map<String, WhitelistEntry> = emptyMap()
        fun isValidUsername(username: String) = username.matches(Regex("[A-Za-z0-9_]{3,16}"))
        fun getEntryByDiscordID(id: String) = whitelistMap[id]
        fun getEntryByMinecraftName(name: String) = whitelistMap.values.find { it.minecraftName.equals(name, true) }
        fun getEntryByMinecraftUUID(uuid: String) = whitelistMap.values.find { it.minecraftUUID.equals(uuid, true) }
        fun getAllEntries() = whitelistMap.values.sortedBy { it.minecraftName.lowercase() }
        fun containsEntry(id: String) = whitelistMap.containsKey(id)
        fun getEntriesBySite(site: Int) = getAllEntries().drop((site.coerceAtLeast(1) - 1) * 25).take(25)
        fun getTotalEntriesCount() = whitelistMap.size
        fun getTotalPagesCount() = (whitelistMap.size + 24) / 25

        /** Called on the IO dispatcher; complete waits for Discord's actual REST outcome. */
        fun changeRoles(user: String, guildID: String, add: Boolean) {
            val configured = ConfigFile.getValueAsString("whitelistFeature.roles").orEmpty()
                .split(',').map { it.trim() }.filter { it.isNotEmpty() }
            if (configured.isEmpty()) return
            check(AppLogic.started) { "Discord unavailable" }
            val guild = AppLogic.getJDA().getGuildById(guildID) ?: error("Guild unavailable")
            val roles = configured.map { guild.getRoleById(it) ?: error("Missing role: $it") }
            val member = guild.retrieveMemberById(user).complete()
            check(guild.selfMember.hasPermission(net.dv8tion.jda.api.Permission.MANAGE_ROLES)) { "Missing MANAGE_ROLES permission" }
            roles.forEach { check(!it.isManaged && guild.selfMember.canInteract(it)) { "Role hierarchy: ${it.id}" } }
            roles.forEach {
                if (add) guild.addRoleToMember(member, it).complete()
                else guild.removeRoleFromMember(member, it).complete()
            }
        }
    }
}

@Serializable
data class WhitelistEntry(val discordUserID: String, val minecraftName: String, val minecraftUUID: String)
