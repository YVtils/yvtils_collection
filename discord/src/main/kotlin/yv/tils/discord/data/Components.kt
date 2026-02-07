/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.discord.data

import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.container.ContainerChildComponent
import net.dv8tion.jda.api.components.section.Section
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.components.thumbnail.Thumbnail
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.FileUpload
import yv.tils.config.language.LanguageHandler
import yv.tils.discord.language.RegisterStrings
import yv.tils.discord.logic.whitelist.WhitelistLogic
import yv.tils.utils.colors.ColorUtils
import yv.tils.utils.colors.Colors
import yv.tils.utils.player.PlayerUtils
import yv.tils.utils.server.ServerUtils
import yv.tils.utils.server.VersionUtils
import java.io.File


class Components {
    companion object {
        private const val ICON_URL = PlayerUtils.PLAYER_HEAD_API

        const val FOOTER_TEXT = "YVtils • https://yvtils.net"
        const val FOOTER_TEXT_CUSTOMIZABLE = "YVtils • %s • https://yvtils.net"
        const val FOOTER_ICON =
            "https://api.yvtils.net/images/yvtils_logo" // TODO: Implement to upload as emoji to discord app and then use in footer

        val errorColor = ColorUtils.colorFromHex(Colors.RED.color)
        val warningColor = ColorUtils.colorFromHex(Colors.YELLOW.color)
        val successColor = ColorUtils.colorFromHex(Colors.GREEN.color)
        val infoColor = ColorUtils.colorFromHex(Colors.BLUE.color)
        val yvtilsColor = ColorUtils.colorFromHex(Colors.TERTIARY.color)

        fun footerComponent(text: String = ""): TextDisplay {
            if (text.isEmpty()) {
                return TextDisplay.of("-# $FOOTER_TEXT")
            }

            return TextDisplay.of("-# ${FOOTER_TEXT_CUSTOMIZABLE.format(text)}").withUniqueId(999)
        }
    }

    fun serverInfoComponent(user: User): Container {
        val serverIcon = File("./server-icon.png")

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
            if (serverPort != - 1 && serverPort != 25565) {
                serverIP += ":$serverPort"
            }
        }

        val userID = user.id
        var minecraftName = "-"

        val entry = WhitelistLogic.getEntryByDiscordID(userID)

        var playerIconURL = ""

        if (entry != null) {
            minecraftName = entry.minecraftName
            playerIconURL = ICON_URL.replace("<uuid>", entry.minecraftUUID)
        }

        val children: MutableList<ContainerChildComponent?> = ArrayList()
        if (serverIcon.exists()) {
            children.add(
                Section.of(
                    Thumbnail.fromFile(FileUpload.fromData(serverIcon, "server-icon.png")),
                    TextDisplay.of("# ${ServerUtils.serverName}"),
                    TextDisplay.of(ServerUtils.motdAsString),
                )
            )
        } else {
            children.addAll(
                listOf(
                    TextDisplay.of("# ${ServerUtils.serverName}"),
                    TextDisplay.of(ServerUtils.motdAsString)
                )
            )
        }

        children.add(Separator.createDivider(Separator.Spacing.SMALL))

        children.addAll(
            listOf(
                TextDisplay.of("## Server Information"),
                TextDisplay.of("**Version:** $version"),
                TextDisplay.of("**IP:** $serverIP"),
                TextDisplay.of("**Players:** ${PlayerUtils.onlinePlayersAsCount} / ${PlayerUtils.maxOnlinePlayers}"),
                TextDisplay.of("**Difficulty:** ${ServerUtils.difficulty}"),
            )
        )

        if (entry != null) {
            children.add(Separator.createDivider(Separator.Spacing.SMALL))
            children.add(
                Section.of(
                    Thumbnail.fromUrl(playerIconURL),
                    TextDisplay.of("### Linked Minecraft Account"),
                    TextDisplay.of(minecraftName),
                )
            )
        }

        children.add(footerComponent())

        val container = Container.of(
            children
        ).withAccentColor(infoColor)

        return container
    }

    fun actionCancelledComponent(action: String): Container {
        val title = LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_ACTION_CANCELLED_TITLE.key)
        val description = LanguageHandler.getRawMessage(
            RegisterStrings.LangStrings.COMPONENT_ACTION_CANCELLED_DESCRIPTION.key,
            params = mapOf("action" to action)
        )

        val children: MutableList<ContainerChildComponent?> = ArrayList()
        children.add(TextDisplay.of("# $title"))
        children.add(TextDisplay.of(description))
        children.add(footerComponent())

        return Container.of(children).withAccentColor(errorColor)
    }
}
