package yv.tils.discord.logic.whitelist

import language.LanguageHandler
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.container.ContainerChildComponent
import net.dv8tion.jda.api.components.selections.SelectOption
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.entities.emoji.Emoji
import server.ServerUtils
import yv.tils.discord.data.Components
import yv.tils.discord.data.Components.Companion.errorColor
import yv.tils.discord.data.Components.Companion.infoColor
import yv.tils.discord.data.Components.Companion.successColor
import yv.tils.discord.data.Components.Companion.warningColor
import yv.tils.discord.language.RegisterStrings
import yv.tils.discord.logic.AppLogic

// TODO: Implement player head emojis/thumbnails
class WhitelistComponents {
    fun accountAddContainer(accountName: String): Container {
        val children: MutableList<ContainerChildComponent?> = ArrayList()

        val title = LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_WHITELIST_ACCOUNT_ADD_TITLE.key)
        val description = LanguageHandler.getRawMessage(
            RegisterStrings.LangStrings.COMPONENT_WHITELIST_ACCOUNT_ADD_DESCRIPTION.key,
            params = mapOf<String, Any>(
                "accountName" to accountName
            )
        )

        children.add(TextDisplay.of("## $title"))
        children.add(TextDisplay.of(description))
        children.add(Components.footerComponent())

        val container = Container.of(
            children
        ).withAccentColor(successColor)

        return container
    }

    fun accountAlreadyListedContainer(accountName: String): Container {
        val children: MutableList<ContainerChildComponent?> = ArrayList()

        val title =
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_WHITELIST_ACCOUNT_ALREADY_LISTED_TITLE.key)
        val description = LanguageHandler.getRawMessage(
            RegisterStrings.LangStrings.COMPONENT_WHITELIST_ACCOUNT_ALREADY_LISTED_DESCRIPTION.key,
            params = mapOf<String, Any>(
                "accountName" to accountName
            )
        )

        children.add(TextDisplay.of("## $title"))
        children.add(TextDisplay.of(description))
        children.add(Components.footerComponent())

        val container = Container.of(
            children
        ).withAccentColor(warningColor)

        return container
    }

    fun invalidAccountContainer(accName: String): Container {
        val children: MutableList<ContainerChildComponent?> = ArrayList()

        val title =
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_WHITELIST_ACCOUNT_INVALID_TITLE.key)
        val description = LanguageHandler.getRawMessage(
            RegisterStrings.LangStrings.COMPONENT_WHITELIST_ACCOUNT_INVALID_DESCRIPTION.key,
            params = mapOf<String, Any>(
                "accountName" to accName
            )
        )

        children.add(TextDisplay.of("## $title"))
        children.add(TextDisplay.of(description))
        children.add(Components.footerComponent())

        val container = Container.of(
            children
        ).withAccentColor(errorColor)

        return container
    }

    fun accountChangePromptContainer(oldName: String, newName: String): Container {
        val children: MutableList<ContainerChildComponent?> = ArrayList()

        val title =
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_WHITELIST_ACCOUNT_CHANGE_PROMPT_TITLE.key)
        val description = LanguageHandler.getRawMessage(
            RegisterStrings.LangStrings.COMPONENT_WHITELIST_ACCOUNT_CHANGE_PROMPT_DESCRIPTION.key,
            params = mapOf<String, Any>(
                "oldName" to oldName,
                "newName" to newName
            )
        )

        children.add(TextDisplay.of("## $title"))
        children.add(TextDisplay.of(description))
        children.add(Separator.createDivider(Separator.Spacing.SMALL))
        children.add(
            ActionRow.of(
                Button.success("whitelist:change:confirm", "Confirm")
                    .withEmoji(Emoji.fromUnicode("✅")),
                Button.danger("whitelist:change:cancel", "Cancel")
            )
        )
        children.add(Components.footerComponent())

        val container = Container.of(
            children
        ).withAccentColor(successColor)

        return container
    }

    fun accountErrorContainer(error: String): Container {
        val children: MutableList<ContainerChildComponent?> = ArrayList()

        val title = LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_WHITELIST_ERROR_TITLE.key)
        val description = LanguageHandler.getRawMessage(
            RegisterStrings.LangStrings.COMPONENT_WHITELIST_ERROR_DESCRIPTION.key,
            params = mapOf<String, Any>(
                "error" to error
            )
        )

        children.add(TextDisplay.of("## $title"))
        children.add(TextDisplay.of(description))
        children.add(Components.footerComponent())

        val container = Container.of(
            children
        ).withAccentColor(errorColor)

        return container
    }

    fun accountChangeContainer(oldName: String, newName: String): Container {
        val children: MutableList<ContainerChildComponent?> = ArrayList()

        val title =
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_WHITELIST_ACCOUNT_CHANGE_TITLE.key)
        val description = LanguageHandler.getRawMessage(
            RegisterStrings.LangStrings.COMPONENT_WHITELIST_ACCOUNT_CHANGE_DESCRIPTION.key,
            params = mapOf<String, Any>(
                "oldName" to oldName,
                "newName" to newName
            )
        )

        children.add(TextDisplay.of("## $title"))
        children.add(TextDisplay.of(description))
        children.add(Components.footerComponent())

        val container = Container.of(
            children
        ).withAccentColor(successColor)

        return container
    }

    fun forceRemoveContainer(site: Int, removedEntry: List<WhitelistEntry> = listOf()): Container {
        val children: MutableList<ContainerChildComponent?> = ArrayList()

        val entries = WhitelistLogic.getEntriesBySite(site)

        val title =
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_WHITELIST_FORCE_REMOVE_TITLE.key)
        val description =
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_WHITELIST_FORCE_REMOVE_DESCRIPTION.key)
        val noEntriesDescription =
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_WHITELIST_FORCE_REMOVE_NO_ENTRIES_DESCRIPTION.key)
        val removeFieldName =
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_WHITELIST_FORCE_REMOVE_REMOVED_FIELD_NAME.key)
        val fieldWhitelistedPlayersCount = WhitelistLogic.getTotalEntriesCount()
        val fieldWhitelistStatus = if (ServerUtils.isWhitelistActive) "on" else "off"

        val maxSite = WhitelistLogic.getTotalPagesCount()

        children.add(TextDisplay.of("## $title"))

        if (WhitelistLogic.getTotalEntriesCount() != 0) {
            children.add(TextDisplay.of(description))
        } else {
            children.add(TextDisplay.of(noEntriesDescription))
        }

        children.add(Separator.createDivider(Separator.Spacing.SMALL))

        children.add(
            TextDisplay.of(
                "Whitelisted Players: $fieldWhitelistedPlayersCount"
            )
        )

        children.add(
            TextDisplay.of(
                "Whitelist Status: $fieldWhitelistStatus"
            )
        )

        if (removedEntry.isNotEmpty()) {
            children.add(Separator.createDivider(Separator.Spacing.SMALL))
            val removedNames = removedEntry.joinToString(", ") { it.minecraftName }

            if (removedNames.length > 1024) {
                children.add(TextDisplay.of(removeFieldName))
                children.add(TextDisplay.of("${removedNames.take(1021)}..."))
            } else {
                children.add(TextDisplay.of(removeFieldName))
                children.add(TextDisplay.of(removedNames))
            }
        }

        children.add(Separator.createDivider(Separator.Spacing.SMALL))

        children.add(
            ActionRow.of(
                forceRemoveActionRowDropdown(entries).build()
            )
        )

        children.add(
            ActionRow.of(
                forceRemoveActionRowButtons(site)
            )
        )

        children.add(Components.footerComponent("Site $site / $maxSite"))

        val container = Container.of(
            children
        ).withAccentColor(infoColor)

        return container
    }

    private fun forceRemoveActionRowButtons(site: Int): List<Button> {
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

    private fun forceRemoveActionRowDropdown(entries: List<WhitelistEntry>): StringSelectMenu.Builder {
        if (entries.isEmpty()) {
            return StringSelectMenu.create("whitelist:force:remove")
                .setPlaceholder(LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_WHITELIST_FORCE_REMOVE_NO_ENTRIES_DESCRIPTION.key))
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

    fun checkContainer(entry: WhitelistEntry): Container {
        val children: MutableList<ContainerChildComponent?> = ArrayList()

        val title = LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_WHITELIST_CHECK_TITLE.key)
        val fieldMinecraftName = entry.minecraftName
        val fieldDiscordUserID = entry.discordUserID

        children.add(TextDisplay.of("## $title"))
        children.add(TextDisplay.of("Minecraft Name: $fieldMinecraftName"))
        children.add(TextDisplay.of("Discord User ID: $fieldDiscordUserID"))
        children.add(Components.footerComponent())

        val container = Container.of(
            children
        ).withAccentColor(infoColor)

        return container
    }

}
