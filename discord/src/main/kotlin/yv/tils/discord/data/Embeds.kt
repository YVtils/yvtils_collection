package yv.tils.discord.data

import colors.ColorUtils
import colors.Colors
import data.Data
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import player.PlayerUtils
import server.ServerUtils
import server.VersionUtils
import yv.tils.discord.logic.whitelist.WhitelistLogic

class Embeds {
    companion object {
        val authorName = "YVtils"
        val authorIcon = "https://api.yvtils.net/images/yvtils_logo"
        val authorLink = "https://yvtils.net"
        val footerText = "YVtils • https://yvtils.net"
        val footerIcon = "https://api.yvtils.net/images/yvtils_logo"

        val errorColor = ColorUtils.colorFromHex(Colors.RED.color)
        val warningColor = ColorUtils.colorFromHex(Colors.YELLOW.color)
        val successColor = ColorUtils.colorFromHex(Colors.GREEN.color)
        val infoColor = ColorUtils.colorFromHex(Colors.BLUE.color)
    }

    fun serverInfoEmbed(user: User): EmbedBuilder {
        val builder = EmbedBuilder()

        var version = VersionUtils.serverVersion
        val viaVersion = VersionUtils.isViaVersion
        if (viaVersion) {
            version = "$version +"
        }

        var serverIP = ServerUtils.serverIP
        val serverPort = ServerUtils.serverPort
        if (serverIP.isEmpty()) {
            serverIP = ""
        } else {
            if (serverPort != -1 && serverPort != 25565) {
                serverIP += ":$serverPort"
            }
        }

        val userID = user.id
        var minecraftName = "-"

        val entry = WhitelistLogic.getEntryByDiscordID(userID)

        if (entry != null) {
            minecraftName = entry.minecraftName
        }

        return builder
            .setTitle("Minecraft Server Info")
            .setThumbnail("attachment://server-icon.png")
            .addField("Version", version, true)
            .addField(
                "Players",
                "${PlayerUtils.onlinePlayersAsCount}/${PlayerUtils.maxOnlinePlayers}",
                true
            )
            .addField("Difficulty", Data.instance.server.worlds[0].difficulty.name, true)
            .addField("Linked Account", minecraftName, false)
            .setColor(infoColor)
            .setFooter(footerText, footerIcon)
            .setAuthor(
                authorName + if (serverIP.isNotEmpty()) " • $serverIP" else "",
                authorLink,
                authorIcon
            )
    }
}