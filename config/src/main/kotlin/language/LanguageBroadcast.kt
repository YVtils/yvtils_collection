package language

import org.bukkit.Bukkit

class LanguageBroadcast {
    companion object {
        fun broadcast(key: String, params: Map<String, Any> = emptyMap()) {
            val players = Bukkit.getOnlinePlayers()
            for (p in players) {
                p.sendMessage(
                    LanguageHandler.getMessage(
                        key,
                        p.uniqueId,
                        params
                    )
                )
            }
        }

        fun broadcastWithPermission(key: String, permission: String, params: Map<String, Any> = emptyMap()) {
            val players = Bukkit.getOnlinePlayers()
            for (p in players) {
                if (p.hasPermission(permission)) {
                    p.sendMessage(
                        LanguageHandler.getMessage(
                            key,
                            p.uniqueId,
                            params
                        )
                    )
                }
            }
        }
    }
}