package yv.tils.discord.actions.commands.handler

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.*
import yv.tils.configv2.language.LanguageHandler
import yv.tils.discord.actions.modals.JDARegistration
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.language.AccountText
import yv.tils.discord.language.RegisterStrings.LangStrings
import yv.tils.discord.logic.whitelist.*
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger

class JDAWhitelist {
    companion object {
        val cmdPermission
            get() = ConfigFile.getValueAsString("commands.whitelistCommand.permission") ?: "MANAGE_CHANNEL"

        fun permission() = runCatching { Permission.valueOf(cmdPermission) }.getOrDefault(Permission.MANAGE_CHANNEL)
        fun authorized(member: net.dv8tion.jda.api.entities.Member?) = member?.hasPermission(permission()) == true
    }

    fun executeCommand(e: SlashCommandInteractionEvent) {
        if (!JDARegistration.allowedGuild(e.guild?.id) || !authorized(e.member)) {
            e.reply(AccountText.raw(if (!JDARegistration.allowedGuild(e.guild?.id)) "guild" else "permission"))
                .setEphemeral(true).queue()
            return
        }
        e.deferReply(true).queue({ hook ->
            CoroutineHandler.launchTask(task = {
                try {
                    when (e.subcommandName) {
                        "setup" -> {
                            val tutorialLink = JDARegistration.helpLink(e.getOption("tutorial_link")?.asString, e.guild!!.id)
                            val supportLink = JDARegistration.helpLink(e.getOption("support_link")?.asString, e.guild!!.id)
                            try {
                                val channel = e.getOption("channel")!!.asChannel.asTextChannel()
                                check(channel.guild.id == e.guild!!.id)
                                channel.sendMessageComponents(JDARegistration.panel(tutorialLink, supportLink)).useComponentsV2().complete()
                                hook.editOriginal(AccountText.raw("posted")).queue()
                            } catch (error: Exception) {
                                Logger.warn("Registration panel posting failed: ${error.message}")
                                hook.editOriginal(AccountText.raw("postFailed")).queue()
                            }
                        }

                        "forceadd" -> {
                            val name = e.getOption("minecraft_name")!!.asString
                            val target = e.getOption("discord_user")?.asUser?.id ?: "~$name"
                            val old = WhitelistLogic.getEntryByDiscordID(target)
                            if (old != null) {
                                WhitelistManage.addToCache("${e.user.id}_$target", name)
                                val cacheKey = "${e.user.id}_$target"
                                CoroutineHandler.launchTask(task = {
                                    WhitelistManage.accountReplaceCache.remove(cacheKey)
                                    WhitelistManage.replacementSnapshots.remove(cacheKey)
                                }, beforeDelay = 60000, isOnce = true)
                                hook.sendMessageComponents(
                                    WhitelistComponents().accountChangePromptContainer(
                                        old.minecraftName,
                                        name
                                    )
                                ).useComponentsV2().setEphemeral(true).queue()
                            } else {
                                WhitelistManage().linkAccount(name, target, e.guild!!.id, e.user)
                                hook.editOriginal(AccountText.raw("success")).queue()
                            }
                        }

                        "forceremove" -> {
                            val user = e.getOption("discord_user")?.asUser?.id
                            val name = e.getOption("minecraft_name")?.asString
                            if (user == null && name == null) {
                                hook.sendMessageComponents(
                                    WhitelistComponents().forceRemoveContainer(
                                        e.getOption("site")?.asInt ?: 1
                                    )
                                ).useComponentsV2().setEphemeral(true).queue()
                            } else {
                                val byUser = user?.let { WhitelistLogic.getEntryByDiscordID(it) }
                                val byName = name?.let {
                                    WhitelistLogic.getEntryByMinecraftName(it)
                                        ?: WhitelistLogic.getEntryByMinecraftUUID(it)
                                }
                                if (user != null && name != null && byUser != byName) throw AccountService.Failure("stale")
                                val entry = byUser ?: byName ?: throw AccountService.Failure("missing")
                                AccountService.remove(entry.discordUserID, e.guild!!.id, "Discord:${e.user.id}", entry)
                                hook.editOriginal(AccountText.raw("completed")).queue()
                            }
                        }

                        "check" -> {
                            val user = e.getOption("discord_user")?.asUser?.id
                            val name = e.getOption("minecraft_name")?.asString
                            val entry = if (name != null) WhitelistLogic.getEntryByMinecraftName(name)
                                ?: WhitelistLogic.getEntryByMinecraftUUID(name)
                            else WhitelistLogic.getEntryByDiscordID(user ?: e.user.id)
                            if (entry == null) throw AccountService.Failure("missing")
                            hook.editOriginal(
                                AccountText.raw(
                                    "entry",
                                    params = mapOf(
                                        "name" to entry.minecraftName,
                                        "uuid" to entry.minecraftUUID,
                                        "discord" to entry.discordUserID,
                                        "display" to AccountText.raw("unavailable")
                                    )
                                )
                            ).queue()
                        }

                        else -> hook.editOriginal(AccountText.raw("help")).queue()
                    }
                } catch (error: Exception) {
                    hook.editOriginal(AccountText.error(error)).queue()
                }
            }, isOnce = true)
        }, { Logger.warn("Whitelist acknowledgement failed: ${it.message}") })
    }

    fun registerCommand(): SlashCommandData {
        fun text(key: LangStrings) = LanguageHandler.getCleanMessage(key.key)
        fun accountOption(key: LangStrings, required: Boolean = false) =
            OptionData(OptionType.STRING, "minecraft_name", text(key), required)

        fun userOption(key: LangStrings) = OptionData(OptionType.USER, "discord_user", text(key))
        return Commands.slash("whitelist", text(LangStrings.SLASHCOMMANDS_WHITELIST_DESCRIPTION))
            .setContexts(InteractionContextType.GUILD)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(permission()))
            .addSubcommands(
                SubcommandData("setup", AccountText.raw("setup")).addOptions(
                    OptionData(
                        OptionType.CHANNEL,
                        "channel",
                        AccountText.raw("channel"),
                        true
                    ).setChannelTypes(ChannelType.TEXT),
                    OptionData(OptionType.STRING, "tutorial_link", AccountText.raw("tutorialOption")).setMaxLength(500),
                    OptionData(OptionType.STRING, "support_link", AccountText.raw("supportOption")).setMaxLength(500)
                ),
                SubcommandData(
                    "forceadd",
                    text(LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_DESCRIPTION)
                ).addOptions(
                    accountOption(
                        LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_ARGS_MINECRAFTNAME_DESCRIPTION,
                        true
                    ), userOption(LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_ARGS_DISCORDUSER_DESCRIPTION)
                ),
                SubcommandData(
                    "forceremove",
                    text(LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_DESCRIPTION)
                ).addOptions(
                    OptionData(
                        OptionType.INTEGER,
                        "site",
                        text(LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_SITE_DESCRIPTION)
                    ),
                    accountOption(LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_MINECRAFTNAME_DESCRIPTION),
                    userOption(LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_DISCORDUSER_DESCRIPTION)
                ),
                SubcommandData(
                    "check",
                    text(LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_DESCRIPTION)
                ).addOptions(
                    accountOption(LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_ARGS_MINECRAFTNAME_DESCRIPTION),
                    userOption(LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_ARGS_DISCORDUSER_DESCRIPTION)
                )
            )
    }
}
