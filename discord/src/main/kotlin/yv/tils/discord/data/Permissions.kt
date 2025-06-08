package yv.tils.discord.data

enum class Permissions(val permission: String) {
    SYNC_CHAT("yvtils.sync.discord.chat"),
    SYNC_ADVANCEMENTS("yvtils.sync.discord.advancements"),
    SYNC_JOIN("yvtils.sync.discord.join"),
    SYNC_QUIT("yvtils.sync.discord.quit"),
}