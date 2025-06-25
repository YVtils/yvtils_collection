package yv.tils.discord.logic.whitelist

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import server.ServerUtils
import yv.tils.discord.data.Embeds.Companion.AUTHOR_ICON
import yv.tils.discord.data.Embeds.Companion.AUTHOR_LINK
import yv.tils.discord.data.Embeds.Companion.AUTHOR_NAME
import yv.tils.discord.data.Embeds.Companion.FOOTER_ICON
import yv.tils.discord.data.Embeds.Companion.FOOTER_TEXT
import yv.tils.discord.data.Embeds.Companion.FOOTER_TEXT_CUSTOMIZABLE
import yv.tils.discord.data.Embeds.Companion.errorColor
import yv.tils.discord.data.Embeds.Companion.infoColor
import yv.tils.discord.data.Embeds.Companion.successColor
import yv.tils.discord.data.Embeds.Companion.warningColor
import yv.tils.discord.logic.AppLogic

// TODO: Add correct messages for titles and descriptions
class WhitelistEmbeds {
    fun accountAddEmbed(accountName: String): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = "Account Added"
        val description = "The account **$accountName** has been successfully added to the whitelist."

        return builder
            .setTitle(title)
            .setDescription(description)
            .setColor(successColor)
            .setFooter(FOOTER_TEXT, FOOTER_ICON)
            .setAuthor(AUTHOR_NAME, AUTHOR_LINK, AUTHOR_ICON)
    }

    fun accountRemoveEmbed(accountName: String): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = "Account Removed"
        val description = "The account **$accountName** has been successfully removed from the whitelist."

        return builder
            .setTitle(title)
            .setDescription(description)
            .setColor(successColor)
            .setFooter(FOOTER_TEXT, FOOTER_ICON)
            .setAuthor(AUTHOR_NAME, AUTHOR_LINK, AUTHOR_ICON)
    }

    fun accountAlreadyListedEmbed(accountName: String): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = "Account Already Listed"
        val description = "The account **$accountName** is already whitelisted."

        return builder
            .setTitle(title)
            .setDescription(description)
            .setColor(warningColor)
            .setFooter(FOOTER_TEXT, FOOTER_ICON)
            .setAuthor(AUTHOR_NAME, AUTHOR_LINK, AUTHOR_ICON)
    }

    fun invalidAccountEmbed(accName: String): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = "Invalid Account"
        val description = "The account **$accName** is not valid or does not exist."

        return builder
            .setTitle(title)
            .setDescription(description)
            .setColor(errorColor)
            .setFooter(FOOTER_TEXT, FOOTER_ICON)
            .setAuthor(AUTHOR_NAME, AUTHOR_LINK, AUTHOR_ICON)
    }

    fun accountChangeEmbed(oldName: String, newName: String): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = "Account Changed"
        val description = "The account **$oldName** has been replaced with **$newName** in the whitelist."

        return builder
            .setTitle(title)
            .setDescription(description)
            .setColor(successColor)
            .setFooter(FOOTER_TEXT, FOOTER_ICON)
            .setAuthor(AUTHOR_NAME, AUTHOR_LINK, AUTHOR_ICON)
    }

    fun accountChangeActionRow(): List<Button> {
        val buttons = mutableListOf<Button>()
        buttons.add(
            Button.success("whitelist:change:confirm", "Confirm")
                .withEmoji(Emoji.fromUnicode("✅"))
        )

        buttons.add(
            Button.danger("whitelist:change:cancel", "Cancel")
        )

        return buttons
    }

    fun accountErrorEmbed(error: String): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = "Error"
        val description = "An error occurred: $error"

        return builder
            .setTitle(title)
            .setDescription(description)
            .setColor(errorColor)
            .setFooter(FOOTER_TEXT, FOOTER_ICON)
            .setAuthor(AUTHOR_NAME, AUTHOR_LINK, AUTHOR_ICON)
    }

    fun forceRemoveEmbed(site: Int): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = "Force Remove Whitelist Entries"
        val description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor." // TODO: Add a proper description
        val fieldWhitelistedPlayersCount = WhitelistLogic.getTotalEntriesCount()
        val fieldWhitelistStatus = if (ServerUtils.isWhitelistActive) "on" else "off"

        val maxSite = WhitelistLogic.getTotalPagesCount()

        val customFooter = FOOTER_TEXT_CUSTOMIZABLE
            .replace("%s", "Site $site / $maxSite")

        return builder
            .setTitle(title)
            .setDescription(description)
            .addField("Whitelisted Players:", fieldWhitelistedPlayersCount.toString(), true)
            .addField("Whitelist Status:", fieldWhitelistStatus, true)
            .setColor(infoColor)
            .setFooter(customFooter, FOOTER_ICON)
            .setAuthor(AUTHOR_NAME, AUTHOR_LINK, AUTHOR_ICON)
    }

    fun forceRemoveActionRowButtons(site: Int): List<Button> {
        val buttons = mutableListOf<Button>()

        val maxSite = WhitelistLogic.getTotalPagesCount()

        val isPreviousButtonDisabled = site <= 1
        val isNextButtonDisabled = site >= maxSite

        buttons.add(
            if (isPreviousButtonDisabled)
                Button.danger("whitelist:force:remove:site:previous", "«").asDisabled()
            else
                Button.danger("whitelist:force:remove:site:previous", "«")
        )

        buttons.add(
            if (isNextButtonDisabled)
                Button.success("whitelist:force:remove:site:next", "»").asDisabled()
            else
                Button.success("whitelist:force:remove:site:next", "»")
        )

        return buttons
    }

    fun forceRemoveActionRowDropdown(entries: List<WhitelistEntry>): StringSelectMenu.Builder {
        if (entries.isEmpty()) {
            return StringSelectMenu.create("whitelist:force:remove")
                .setPlaceholder("No entries available") // TODO: Replace with a proper message
                .setDisabled(true)
                .addOption("null", "null")
        }

        val options = mutableListOf<SelectOption>()

        for (entry in entries) {
            val discordUserID = entry.discordUserID
            val discordUserName: String = if (discordUserID.startsWith("~")) {
                discordUserID
            } else {
                try {
                    AppLogic.jda.retrieveUserById(discordUserID).complete()?.name ?: "Unknown User ($discordUserID)"
                } catch (_: Exception) {
                    "Unknown User ($discordUserID)"
                }
            }


            options.add(SelectOption.of("${entry.minecraftName} (${discordUserName})", entry.discordUserID))
        }

        return StringSelectMenu.create("whitelist:force:remove")
            .setPlaceholder("Minecraft Name (Discord User)")
            .addOptions(options)
            .setMinValues(1)
            .setMaxValues(25)
    }

    fun checkEmbed(entry: WhitelistEntry): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = "Whitelist Check"
        val description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor." // TODO: Add a proper description
        val fieldMinecraftName = entry.minecraftName
        val fieldDiscordUserID = entry.discordUserID

        return builder
            .setTitle(title)
            .setDescription(description)
            .addField("Minecraft Name:", fieldMinecraftName, true)
            .addField("Discord User ID:", fieldDiscordUserID, true)
            .setColor(successColor)
            .setFooter(FOOTER_TEXT, FOOTER_ICON)
            .setAuthor(AUTHOR_NAME, AUTHOR_LINK, AUTHOR_ICON)
    }
}