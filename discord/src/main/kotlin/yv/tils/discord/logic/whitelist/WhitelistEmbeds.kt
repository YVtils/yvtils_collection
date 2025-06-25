package yv.tils.discord.logic.whitelist

import language.LanguageHandler
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
import yv.tils.discord.language.RegisterStrings
import yv.tils.discord.logic.AppLogic

class WhitelistEmbeds {
    fun accountAddEmbed(accountName: String): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = LanguageHandler.getRawMessage(RegisterStrings.LangStrings.EMBED_WHITELIST_ACCOUNT_ADD_TITLE.key)
        val description = LanguageHandler.getRawMessage(
            RegisterStrings.LangStrings.EMBED_WHITELIST_ACCOUNT_ADD_DESCRIPTION.key,
            params = mapOf<String, Any>(
                "accountName" to accountName
            )
        )

        return builder
            .setTitle(title)
            .setDescription(description)
            .setColor(successColor)
            .setFooter(FOOTER_TEXT, FOOTER_ICON)
            .setAuthor(AUTHOR_NAME, AUTHOR_LINK, AUTHOR_ICON)
    }

    fun accountAlreadyListedEmbed(accountName: String): EmbedBuilder {
        val builder = EmbedBuilder()

        val title =
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.EMBED_WHITELIST_ACCOUNT_ALREADY_LISTED_TITLE.key)
        val description = LanguageHandler.getRawMessage(
            RegisterStrings.LangStrings.EMBED_WHITELIST_ACCOUNT_ALREADY_LISTED_DESCRIPTION.key,
            params = mapOf<String, Any>(
                "accountName" to accountName
            )
        )

        return builder
            .setTitle(title)
            .setDescription(description)
            .setColor(warningColor)
            .setFooter(FOOTER_TEXT, FOOTER_ICON)
            .setAuthor(AUTHOR_NAME, AUTHOR_LINK, AUTHOR_ICON)
    }

    fun invalidAccountEmbed(accName: String): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = LanguageHandler.getRawMessage(RegisterStrings.LangStrings.EMBED_WHITELIST_ACCOUNT_INVALID_TITLE.key)
        val description = LanguageHandler.getRawMessage(
            RegisterStrings.LangStrings.EMBED_WHITELIST_ACCOUNT_INVALID_DESCRIPTION.key,
            params = mapOf<String, Any>(
                "accountName" to accName
            )
        )

        return builder
            .setTitle(title)
            .setDescription(description)
            .setColor(errorColor)
            .setFooter(FOOTER_TEXT, FOOTER_ICON)
            .setAuthor(AUTHOR_NAME, AUTHOR_LINK, AUTHOR_ICON)
    }

    fun accountChangePromptEmbed(oldName: String, newName: String): EmbedBuilder {
        val builder = EmbedBuilder()

        val title =
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.EMBED_WHITELIST_ACCOUNT_CHANGE_PROMPT_TITLE.key)
        val description = LanguageHandler.getRawMessage(
            RegisterStrings.LangStrings.EMBED_WHITELIST_ACCOUNT_CHANGE_PROMPT_DESCRIPTION.key,
            params = mapOf<String, Any>(
                "oldName" to oldName,
                "newName" to newName
            )
        )

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

        val title = LanguageHandler.getRawMessage(RegisterStrings.LangStrings.EMBED_WHITELIST_ERROR_TITLE.key)
        val description = LanguageHandler.getRawMessage(
            RegisterStrings.LangStrings.EMBED_WHITELIST_ERROR_DESCRIPTION.key,
            params = mapOf<String, Any>(
                "error" to error
            )
        )

        return builder
            .setTitle(title)
            .setDescription(description)
            .setColor(errorColor)
            .setFooter(FOOTER_TEXT, FOOTER_ICON)
            .setAuthor(AUTHOR_NAME, AUTHOR_LINK, AUTHOR_ICON)
    }

    fun forceRemoveEmbed(site: Int, removedEntry: List<WhitelistEntry> = listOf()): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = LanguageHandler.getRawMessage(RegisterStrings.LangStrings.EMBED_WHITELIST_FORCE_REMOVE_TITLE.key)
        val description =
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.EMBED_WHITELIST_FORCE_REMOVE_DESCRIPTION.key)
        val noEntriesDescription =
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.EMBED_WHITELIST_FORCE_REMOVE_NO_ENTRIES_DESCRIPTION.key)
        val removeFieldName =
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.EMBED_WHITELIST_FORCE_REMOVE_REMOVED_FIELD_NAME.key)
        val fieldWhitelistedPlayersCount = WhitelistLogic.getTotalEntriesCount()
        val fieldWhitelistStatus = if (ServerUtils.isWhitelistActive) "on" else "off"

        val maxSite = WhitelistLogic.getTotalPagesCount()

        val customFooter = FOOTER_TEXT_CUSTOMIZABLE
            .replace("%s", "Site $site / $maxSite")

        builder
            .setTitle(title)
            .setDescription(description)
            .addField("Whitelisted Players:", fieldWhitelistedPlayersCount.toString(), true)
            .addField("Whitelist Status:", fieldWhitelistStatus, true)
            .setColor(infoColor)
            .setFooter(customFooter, FOOTER_ICON)
            .setAuthor(AUTHOR_NAME, AUTHOR_LINK, AUTHOR_ICON)

        if (WhitelistLogic.getTotalEntriesCount() == 0) {
            builder.setDescription(noEntriesDescription)
        }

        if (removedEntry.isNotEmpty()) {
            val removedNames = removedEntry.joinToString(", ") { it.minecraftName }

            if (removedNames.length > 1024) {
                builder.addField(removeFieldName, "${removedNames.take(1021)}...", false)
            } else {
                builder.addField(removeFieldName, removedNames, false)
            }
        }

        return builder
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
                .setPlaceholder(LanguageHandler.getRawMessage(RegisterStrings.LangStrings.EMBED_WHITELIST_FORCE_REMOVE_NO_ENTRIES_DESCRIPTION.key))
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

            val optionLabel = "${entry.minecraftName} (${discordUserName})"

            if (optionLabel.length > 100) {
                options.add(SelectOption.of(entry.minecraftName, entry.discordUserID))
            } else {
                options.add(SelectOption.of(optionLabel, entry.discordUserID))
            }
        }

        return StringSelectMenu.create("whitelist:force:remove")
            .setPlaceholder("Minecraft Name (Discord User)")
            .addOptions(options)
            .setMinValues(1)
            .setMaxValues(25)
    }

    fun checkEmbed(entry: WhitelistEntry): EmbedBuilder {
        val builder = EmbedBuilder()

        val title = LanguageHandler.getRawMessage(RegisterStrings.LangStrings.EMBED_WHITELIST_CHECK_TITLE.key)
        val fieldMinecraftName = entry.minecraftName
        val fieldDiscordUserID = entry.discordUserID

        return builder
            .setTitle(title)
            .addField("Minecraft Name:", fieldMinecraftName, true)
            .addField("Discord User ID:", fieldDiscordUserID, true)
            .setColor(successColor)
            .setFooter(FOOTER_TEXT, FOOTER_ICON)
            .setAuthor(AUTHOR_NAME, AUTHOR_LINK, AUTHOR_ICON)
    }
}
