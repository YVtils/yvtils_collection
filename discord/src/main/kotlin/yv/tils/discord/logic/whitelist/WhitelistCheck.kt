package yv.tils.discord.logic.whitelist

import net.dv8tion.jda.api.EmbedBuilder
import org.bukkit.Bukkit

class WhitelistCheck {
    fun  whitelistCheck(mc: String): EmbedBuilder {
        val player = Bukkit.getOfflinePlayer(mc)

//        if (!mc.matches(Regex("[a-zA-Z0-9_]+"))) {
//            return AccountCanNotExist().embed(mc)
//        }
//
//        val dc = WhitelistConfig().reader(mc = mc, uuid = player.uniqueId.toString())[0]
//
//        val isWhitelisted = player.isWhitelisted
//        return Check().embed(mc, dc, isWhitelisted)

        return EmbedBuilder()
            .setTitle("Whitelist Check")
            .setDescription("This feature is currently under development.")
            .setColor(0xFF0000) // Red color to indicate it's a work in progress
            .addField("Minecraft Username", mc, true)
            .addField("Status", "Under Development", true)
            .setFooter("Please check back later for updates.")
    }
}