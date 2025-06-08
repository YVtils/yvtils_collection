package yv.tils.discord.logic.sync.serverChats

import colors.ColorUtils.Companion.colorFromHex
import colors.Colors
import message.MessageUtils
import net.dv8tion.jda.api.EmbedBuilder
import org.bukkit.advancement.Advancement
import org.bukkit.entity.Player
import java.awt.Color

class MessageEmbeds {
    companion object {
        private const val ICON_URL = "https://cravatar.eu/helmhead/<uuid>/600"
    }

    fun embedForChat(sender: Player, message: String): EmbedBuilder {
        return EmbedBuilder()
            .setAuthor(sender.name, null, ICON_URL.replace("<uuid>", sender.uniqueId.toString()))
            .setDescription(message)
            .setColor(Color(0xD6E0C6))
    }

    fun embedForAdvancement(sender: Player, advancement: Advancement): EmbedBuilder {
        return EmbedBuilder()
            .setAuthor(sender.name, null, ICON_URL.replace("<uuid>", sender.uniqueId.toString()))
            .addField(
                prettifyAdvancementKey(advancement.key.key),
                MessageUtils.strip(advancement.display?.description() ?: MessageUtils.convert("No description available.")),
                false
            )
            .setColor(Color(0xFEEF89))
    }

    fun embedForJoinLeave(sender: Player, action: String): EmbedBuilder {
        return EmbedBuilder()
            .setAuthor(sender.name, null, ICON_URL.replace("<uuid>", sender.uniqueId.toString()))
            .setDescription("**${sender.name}** has $action the server.")
            .setColor(if (action == "joined") colorFromHex(Colors.GREEN.color) else colorFromHex(Colors.RED.color))
    }

    private fun prettifyAdvancementKey(key: String): String {
        return key.substringAfter("/")  // Remove namespace (e.g., "adventure/")
            .replace("_", " ")          // Replace underscores with spaces
            .split(" ")                 // Split into words
            .joinToString(" ") { word -> // Join words with proper capitalization
                word.replaceFirstChar { it.uppercase() }
            }
    }
}