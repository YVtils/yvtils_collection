package yv.tils.discord.logic.whitelist

object RegistrationPolicy {
    fun conflict(entries: List<WhitelistEntry>, candidate: WhitelistEntry, replacing: WhitelistEntry? = null): String? {
        val own = entries.firstOrNull { it.discordUserID == candidate.discordUserID }
        if (replacing != null && own != replacing) return "stale"
        fun same(entry: WhitelistEntry) = entry.minecraftUUID.equals(candidate.minecraftUUID, true)
        if (own != null && same(own)) return "already"
        if (own != null && replacing == null) return "different"
        if (entries.any {
                it != replacing && (same(it) || it.minecraftName.equals(
                    candidate.minecraftName,
                    true
                ))
            }) return "claimed"
        return null
    }
}
