package yv.tils.discord.utils.emoji

import org.bukkit.entity.Player
import player.PlayerUtils

class EmojiUtils {
    companion object {
        val playerEmojis = mutableMapOf<String, Long>()

        fun getPlayerEmoji(hash: String): Long? {
            return playerEmojis[hash]
        }

        fun addPlayerEmoji(hash: String, emojiId: Long) {
            playerEmojis[hash] = emojiId
        }

        fun removePlayerEmoji(hash: String) {
            playerEmojis.remove(hash)
        }
    }

    /**
     * Creates a player emoji based on the player's skin and adds it to the Discord application.
     * @param player The player whose emoji should be created.
     */
    fun createPlayerEmoji(player: Player) {
        val skinHash = PlayerUtils.getSkinHash(player).take(29)

        if (hasPlayerEmoji(skinHash)) {
            return
        }

        try {
            val emojiId = DiscordEmoji().createSkinEmoji(player)
            addPlayerEmoji(skinHash, emojiId)
        } catch (e: Exception) {
            e.printStackTrace() // TODO: Handle error appropriately
        }
    }

    /**
     * Removes the player's emoji from the Discord application.
     * @param player The player whose emoji should be removed.
     * @param force If true, the emoji will be removed even if persistent emojis are enabled.
     *             If false, the emoji will only be removed if persistent emojis are disabled.
     */
    fun removePlayerEmoji(player: Player, force: Boolean = false) {
        if (force || ! DiscordEmoji.persistentEmojis) {
            val skinHash = PlayerUtils.getSkinHash(player).take(29)

            if (! hasPlayerEmoji(skinHash)) {
                return
            }

            try {
                DiscordEmoji().deleteAppEmoji(getPlayerEmoji(skinHash) ?: return)
                removePlayerEmoji(skinHash)
            } catch (e: Exception) {
                e.printStackTrace() // TODO: Handle error appropriately
            }
        }
    }

    /**
     * Checks if the player has a custom emoji in the Discord application.
     * @param hash The UUID of the player.
     * @return True if the player has a custom emoji, false otherwise.
     */
    fun hasPlayerEmoji(hash: String): Boolean {
        return playerEmojis.containsKey(hash)
    }

    fun getPlayerEmojiId(player: Player): Long? {
        val skinHash = PlayerUtils.getSkinHash(player).take(29)
        return getPlayerEmoji(skinHash)
    }
}
