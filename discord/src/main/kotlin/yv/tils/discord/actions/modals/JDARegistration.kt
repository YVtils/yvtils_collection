package yv.tils.discord.actions.modals

import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.container.ContainerChildComponent
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.components.textinput.TextInput
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.components.label.Label
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.modals.Modal
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.data.Components
import yv.tils.discord.language.AccountText
import yv.tils.discord.logic.whitelist.AccountService
import yv.tils.discord.logic.whitelist.WhitelistLogic
import yv.tils.discord.logic.whitelist.WhitelistEntry
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger

class JDARegistration : ListenerAdapter() {
    companion object {
        private data class Replacement(val guild: String, val expected: WhitelistEntry, val candidate: WhitelistEntry, val expires: Long)
        private val replacements = ConcurrentHashMap<String, Replacement>()
        fun allowedGuild(id: String?) = id != null && id == ConfigFile.getValueAsString("mainGuild")
        fun registrationResult(reason: String): Container {
            val (title, color) = when (reason) {
                "success" -> "registrationSuccessTitle" to Components.successColor
                "replacementSuccess" -> "replacementSuccessTitle" to Components.successColor
                "replacementCancelled" -> "replacementCancelled" to Components.infoColor
                "roles" -> "registrationPartialTitle" to Components.warningColor
                "already" -> "registrationAlreadyTitle" to Components.infoColor
                else -> "registrationErrorTitle" to Components.errorColor
            }
            return Container.of(
                TextDisplay.of("## ${AccountText.raw(title)}"),
                Separator.createDivider(Separator.Spacing.SMALL),
                TextDisplay.of(AccountText.raw(reason)),
                Separator.createDivider(Separator.Spacing.SMALL),
                Components.footerComponent()
            ).withAccentColor(color)
        }
        private fun replacementPrompt(token: String, request: Replacement): Container = Container.of(
            TextDisplay.of("## ${AccountText.raw("replacementConfirmTitle")}"),
            TextDisplay.of(AccountText.raw("replacementConfirmDescription", params = mapOf(
                "old" to request.expected.minecraftName, "new" to request.candidate.minecraftName
            ))),
            ActionRow.of(
                Button.danger("whitelist:modal:confirm:$token", AccountText.raw("replacementConfirmButton")),
                Button.secondary("whitelist:modal:cancel:$token", AccountText.raw("cancel"))
            ),
            Components.footerComponent()
        ).withAccentColor(Components.warningColor)
        fun panel(tutorialLink: String? = null, supportLink: String? = null): Container {
            val children = mutableListOf<ContainerChildComponent>(
                TextDisplay.of("# ${AccountText.raw("register")}"),
                Separator.createDivider(Separator.Spacing.SMALL),
                TextDisplay.of(AccountText.raw("panelDescription")),
                TextDisplay.of("### ⬇️ ${AccountText.raw("registrationCallToAction")}"),
                ActionRow.of(Button.success("whitelist:register", AccountText.raw("registerButton"))
                    .withEmoji(yv.tils.discord.utils.emoji.RegistrationEmoji.get()))
            )
            fun helpDestination(label: String, destination: String): String =
                if (destination.startsWith("<#")) "- ${AccountText.raw(label)}: $destination"
                else "- [${AccountText.raw(label)}]($destination)"
            val links = listOfNotNull(
                tutorialLink?.let { helpDestination("tutorialLabel", it) },
                supportLink?.let { helpDestination("supportLabel", it) }
            )
            if (links.isNotEmpty()) {
                children.add(Separator.createDivider(Separator.Spacing.LARGE))
                children.add(TextDisplay.of("### ${AccountText.raw("panelHelp")}\n${links.joinToString("\n")}"))
            }
            children.add(Separator.createDivider(Separator.Spacing.SMALL))
            children.add(Components.footerComponent())
            return Container.of(children).withAccentColor(Components.successColor)
        }

        fun helpLink(input: String?, guildID: String): String? {
            if (input == null) return null
            val value = input.trim()
            val channelID = value.removePrefix("<#").removeSuffix(">")
                .takeIf { it.matches(Regex("[0-9]{17,20}")) && (value == it || value == "<#$it>") }
            if (channelID != null) return "<#$channelID>"
            val uri = runCatching { java.net.URI(value) }.getOrNull()
            if (uri == null || uri.scheme !in listOf("http", "https") || uri.host.isNullOrBlank() ||
                value.any { it.isWhitespace() || it in "()<>" }) throw AccountService.Failure("invalidHelpLink")
            val discordChannel = Regex("/channels/([0-9]{17,20})/([0-9]{17,20})/?").matchEntire(uri.path)
            if (uri.host.lowercase() in setOf("discord.com", "www.discord.com", "discordapp.com", "www.discordapp.com", "ptb.discord.com", "canary.discord.com") &&
                discordChannel?.groupValues?.get(1) == guildID) {
                return "<#${discordChannel.groupValues[2]}>"
            }
            return value
        }
    }

    fun open(e: ButtonInteractionEvent) {
        if (!allowedGuild(e.guild?.id)) {
            e.reply(AccountText.raw("guild")).setEphemeral(true).queue()
            return
        }
        try {
            val input =
                TextInput.create("minecraft_name", TextInputStyle.SHORT).setRequired(true).setRequiredRange(3, 16)
                    .build()
            e.replyModal(
                Modal.create("whitelist:registration", AccountText.raw("register"))
                    .addComponents(Label.of(AccountText.raw("username"), input)).build()
            ).queue({}, { Logger.warn("Modal response failed: ${it.message}") })
        } catch (error: Exception) {
            e.reply(AccountText.error(error)).setEphemeral(true).queue()
        }
    }

    override fun onModalInteraction(e: ModalInteractionEvent) {
        if (e.modalId != "whitelist:registration") return
        e.deferReply(true).queue({ hook ->
            CoroutineHandler.launchTask(task = {
                val result = try {
                    if (!allowedGuild(e.guild?.id)) throw AccountService.Failure("guild")
                    val name = e.getValue("minecraft_name")?.asString?.trim().orEmpty()
                    if (!WhitelistLogic.isValidUsername(name)) throw AccountService.Failure("invalid")
                    val old = WhitelistLogic.getEntryByDiscordID(e.user.id)
                    if (old == null) {
                        AccountService.register(name, e.user.id, e.guild!!.id, "Discord:${e.user.id}")
                        registrationResult("success")
                    } else {
                        val candidate = AccountService.prepareReplacement(name, old)
                        val token = UUID.randomUUID().toString()
                        val request = Replacement(e.guild!!.id, old, candidate, System.currentTimeMillis() + 300000)
                        replacements[token] = request
                        CoroutineHandler.launchTask(task = { replacements.remove(token, request) }, beforeDelay = 300000, isOnce = true)
                        replacementPrompt(token, request)
                    }
                } catch (error: Exception) {
                    registrationResult(AccountText.reason(error))
                }
                hook.editOriginalComponents(result).useComponentsV2()
                    .queue({}, { Logger.warn("Registration final response failed: ${it.message}") })
            }, isOnce = true)
        }, { Logger.warn("Modal acknowledgement failed: ${it.message}") })
    }

    fun handleReplacement(e: ButtonInteractionEvent) {
        val token = e.componentId.substringAfterLast(':')
        val request = replacements[token]
        if (!allowedGuild(e.guild?.id) || request != null &&
            (request.guild != e.guild?.id || request.expected.discordUserID != e.user.id)) {
            e.replyComponents(registrationResult("permission")).useComponentsV2().setEphemeral(true).queue()
            return
        }
        // This edits the private confirmation, replacing its buttons with the final result.
        e.deferEdit().queue({ hook ->
            CoroutineHandler.launchTask(task = {
                val reason = try {
                    val current = replacements.remove(token) ?: throw AccountService.Failure("stale")
                    if (current.expires < System.currentTimeMillis()) throw AccountService.Failure("stale")
                    if (e.componentId.startsWith("whitelist:modal:cancel:")) "replacementCancelled"
                    else {
                        // UUID identity pins the account that was displayed in the confirmation.
                        AccountService.register(current.candidate.minecraftUUID, e.user.id, current.guild,
                            "Discord:${e.user.id}", current.expected)
                        "replacementSuccess"
                    }
                } catch (error: Exception) { AccountText.reason(error) }
                hook.editOriginalComponents(registrationResult(reason)).useComponentsV2()
                    .queue({}, { Logger.warn("Account replacement response failed: ${it.message}") })
            }, isOnce = true)
        }, { Logger.warn("Account replacement acknowledgement failed: ${it.message}") })
    }
}
