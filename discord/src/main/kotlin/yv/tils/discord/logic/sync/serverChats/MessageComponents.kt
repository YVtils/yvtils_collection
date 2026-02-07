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

package yv.tils.discord.logic.sync.serverChats

import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.container.ContainerChildComponent
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import org.bukkit.advancement.Advancement
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.discord.data.Components.Companion.errorColor
import yv.tils.discord.data.Components.Companion.successColor
import yv.tils.discord.data.Components.Companion.warningColor
import yv.tils.discord.data.Components.Companion.yvtilsColor
import yv.tils.discord.language.RegisterStrings
import yv.tils.discord.utils.emoji.EmojiUtils
import yv.tils.utils.message.MessageUtils

// TODO: Add other events
class MessageComponents {
    fun componentForChat(sender: Player, message: String): Container {
        val children: MutableList<ContainerChildComponent?> = ArrayList()

        children.add(authorComponent(sender))
        children.add(TextDisplay.of(message))

        val container = Container.of(
            children
        ).withAccentColor(yvtilsColor)

        return container
    }

    fun componentForJoinLeave(sender: Player, action: String): Container {
        val children: MutableList<ContainerChildComponent?> = ArrayList()

        val action = if (action == "join") {
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_SYNC_JOIN_LEAVE_ACTION_JOIN.key)
        } else {
            LanguageHandler.getRawMessage(RegisterStrings.LangStrings.COMPONENT_SYNC_JOIN_LEAVE_ACTION_LEAVE.key)
        }

        children.add(authorComponent(sender))
        children.add(
            TextDisplay.of(
                LanguageHandler.getRawMessage(
                    RegisterStrings.LangStrings.COMPONENT_SYNC_JOIN_LEAVE_TEXT.key,
                    params = mapOf(
                        "player" to sender.name,
                        "action" to action
                    )
                )
            )
        )

        val container = Container.of(
            children
        ).withAccentColor(if (action == "joined") successColor else errorColor)

        return container
    }

    fun componentForAdvancement(sender: Player, advancement: Advancement): Container {
        val children: MutableList<ContainerChildComponent?> = ArrayList()

        children.add(authorComponent(sender))
        children.add(
            TextDisplay.of(
                "**${
                    MessageUtils.strip(advancement.display?.title())
                        .ifBlank { prettifyAdvancementKey(advancement.key.key) }
                }**"
            )
        )
        children.add(
            TextDisplay.of(
                MessageUtils.strip(advancement.display?.description()).ifBlank {
                    LanguageHandler.getRawMessage(
                        RegisterStrings.LangStrings.COMPONENT_SYNC_ADVANCEMENT_NO_DESCRIPTION.key
                    )
                }.split("\n").joinToString("\n") { "-# $it" }
        ))

        val container = Container.of(
            children
        ).withAccentColor(warningColor)

        return container
    }

    fun componentForDeath(sender: Player, cause: String): Container { // TODO: Improve design of container
        val children: MutableList<ContainerChildComponent?> = ArrayList()

        children.add(authorComponent(sender))
        children.add(
            TextDisplay.of(
                LanguageHandler.getRawMessage(
                    RegisterStrings.LangStrings.COMPONENT_SYNC_DEATH_TEXT.key,
                    params = mapOf("player" to sender.name)
                )
            )
        )
        children.add(TextDisplay.of("**$cause**"))

        val container = Container.of(
            children
        ).withAccentColor(errorColor)

        return container
    }

    private fun authorComponent(sender: Player): ContainerChildComponent {
        val emojiID = EmojiUtils().getPlayerEmojiId(sender)
        return if (emojiID != null) {
            TextDisplay.of(
                "### <:star:$emojiID> ${sender.name}"
            )
        } else {
            TextDisplay.of(
                "### :star: ${sender.name}"
            )
        }
    }

    private fun prettifyAdvancementKey(key: String): String {
        return key.substringAfter("/")
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }
}
