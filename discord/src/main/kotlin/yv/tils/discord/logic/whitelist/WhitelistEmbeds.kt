package yv.tils.discord.logic.whitelist

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import yv.tils.discord.data.Embeds.Companion.authorIcon
import yv.tils.discord.data.Embeds.Companion.authorLink
import yv.tils.discord.data.Embeds.Companion.authorName
import yv.tils.discord.data.Embeds.Companion.errorColor
import yv.tils.discord.data.Embeds.Companion.footerIcon
import yv.tils.discord.data.Embeds.Companion.footerText
import yv.tils.discord.data.Embeds.Companion.successColor
import yv.tils.discord.data.Embeds.Companion.warningColor

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
            .setFooter(footerText, footerIcon)
            .setAuthor(authorName, authorLink, authorIcon)
    }

    fun accountRemoveEmbed(accountName: String): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = "Account Removed"
        val description = "The account **$accountName** has been successfully removed from the whitelist."

        return builder
            .setTitle(title)
            .setDescription(description)
            .setColor(successColor)
            .setFooter(footerText, footerIcon)
            .setAuthor(authorName, authorLink, authorIcon)
    }

    fun accountAlreadyListedEmbed(accountName: String): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = "Account Already Listed"
        val description = "The account **$accountName** is already whitelisted."

        return builder
            .setTitle(title)
            .setDescription(description)
            .setColor(warningColor)
            .setFooter(footerText, footerIcon)
            .setAuthor(authorName, authorLink, authorIcon)
    }

    fun invalidAccountEmbed(accName: String): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = "Invalid Account"
        val description = "The account **$accName** is not valid or does not exist."

        return builder
            .setTitle(title)
            .setDescription(description)
            .setColor(errorColor)
            .setFooter(footerText, footerIcon)
            .setAuthor(authorName, authorLink, authorIcon)
    }

    fun accountChangeEmbed(oldName: String, newName: String): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = "Account Changed"
        val description = "The account **$oldName** has been replaced with **$newName** in the whitelist."

        return builder
            .setTitle(title)
            .setDescription(description)
            .setColor(successColor)
            .setFooter(footerText, footerIcon)
            .setAuthor(authorName, authorLink, authorIcon)
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
            .setFooter(footerText, footerIcon)
            .setAuthor(authorName, authorLink, authorIcon)
    }
}