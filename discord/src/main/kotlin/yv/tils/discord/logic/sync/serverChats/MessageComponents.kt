package yv.tils.discord.logic.sync.serverChats

import message.MessageUtils
import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.container.ContainerChildComponent
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import org.bukkit.advancement.Advancement
import org.bukkit.entity.Player
import player.PlayerUtils
import yv.tils.discord.data.Components.Companion.errorColor
import yv.tils.discord.data.Components.Companion.successColor
import yv.tils.discord.data.Components.Companion.warningColor
import yv.tils.discord.data.Components.Companion.yvtilsColor
import yv.tils.discord.utils.emoji.EmojiUtils

// TODO: Add other events
class MessageComponents {
    companion object {
        private const val ICON_URL = PlayerUtils.PLAYER_HEAD_API
    }

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

        children.add(authorComponent(sender))
        children.add(TextDisplay.of("**${sender.name}** has $action the server.")) // TODO: Add localization

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
                MessageUtils.strip(advancement.display?.description()).ifBlank { "No description available" }
                    .split("\n").joinToString("\n") { "-# $it" } // TODO: Add localization
        ))

        val container = Container.of(
            children
        ).withAccentColor(warningColor)

        return container
    }

    fun componentForDeath(sender: Player, cause: String): Container {
        val children: MutableList<ContainerChildComponent?> = ArrayList()

        children.add(authorComponent(sender))
        children.add(TextDisplay.of("**${sender.name}** has died")) // TODO: Add localization
        children.add(TextDisplay.of("**$cause**"))

        val container = Container.of(
            children
        ).withAccentColor(errorColor)

        return container
    }

    private fun authorComponent(sender: Player): ContainerChildComponent {
        val emojiID = EmojiUtils().getPlayerEmojiId(sender)
        return TextDisplay.of(
            "### <:star:$emojiID> ${sender.name}"
        )
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
