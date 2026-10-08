package yv.tils.discord.commands

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.discord.data.Permissions
import yv.tils.discord.gui.AccountsGui
import yv.tils.discord.language.AccountText
import yv.tils.discord.logic.whitelist.*
import yv.tils.utils.coroutine.CoroutineHandler

class DiscordAccountsCommand {
    companion object {
        fun allowed(sender: CommandSender, permission: Permissions): Boolean {
            if (sender.hasPermission(permission.permission.name)) return true
            AccountText.send(sender, "permission")
            return false
        }

        fun entry(query: String): WhitelistEntry = WhitelistLogic.getEntryByDiscordID(query)
            ?: WhitelistLogic.getEntryByMinecraftUUID(query)
            ?: WhitelistLogic.getEntryByMinecraftName(query)
            ?: throw AccountService.Failure("missing")

        fun params(entry: WhitelistEntry, sender: CommandSender): Map<String, Any> {
            val lookup = yv.tils.discord.utils.DiscordAccountNames.retrieve(entry.discordUserID)
            val display = yv.tils.discord.utils.DiscordAccountNames.cached(entry.discordUserID)
            val fallback = when {
                entry.discordUserID.startsWith("~") -> "noDiscordLink"
                !lookup.isDone -> "discordNameLoading"
                else -> "unavailable"
            }
            return mapOf(
                "name" to entry.minecraftName,
                "uuid" to entry.minecraftUUID,
                "discord" to entry.discordUserID,
                "display" to (display ?: AccountText.raw(fallback, sender))
            )
        }

        /** Capture actor/expected entry on the server thread; service rechecks it under its mutex. */
        fun action(sender: CommandSender, permission: Permissions, operation: suspend (String) -> Unit) {
            if (!allowed(sender, permission)) return
            val actor =
                if (sender is Player) "Minecraft:${sender.name}/${sender.uniqueId}" else "Minecraft:${sender.name}"
            CoroutineHandler.launchTask(task = {
                val result = try {
                    AccountService.server {
                        if (!sender.hasPermission(permission.permission.name)) throw AccountService.Failure(
                            "permission"
                        )
                    }
                    operation(actor)
                    "completed"
                } catch (error: Exception) {
                    AccountText.reason(error)
                }
                AccountService.server { AccountText.send(sender, result) }
            }, isOnce = true)
        }
    }

    private fun suggestions(permission: Permissions) = ArgumentSuggestions.strings<CommandSender> { info ->
        if (!info.sender.hasPermission(permission.permission.name)) emptyArray()
        else WhitelistLogic.getAllEntries().flatMap { listOf(it.minecraftName, it.minecraftUUID, it.discordUserID) }
            .distinct().toTypedArray()
    }

    private fun inspect(sender: CommandSender, query: String) {
        if (!allowed(sender, Permissions.ACCOUNTS_READ)) return
        try {
            AccountText.send(sender, "entry", params(entry(query), sender))
        } catch (error: Exception) {
            AccountText.send(sender, AccountText.reason(error))
        }
    }

    private fun list(sender: CommandSender, page: Int) {
        if (!allowed(sender, Permissions.ACCOUNTS_READ)) return
        val entries = WhitelistLogic.getAllEntries()
        val pages = ((entries.size + 9) / 10).coerceAtLeast(1)
        val current = page.coerceIn(1, pages)
        AccountText.send(sender, "list", mapOf("page" to current, "pages" to pages))
        if (entries.isEmpty()) AccountText.send(sender, "empty")
        entries.drop((current - 1) * 10).take(10).forEach { AccountText.send(sender, "entry", params(it, sender)) }
    }

    private fun gui(sender: CommandSender, query: String? = null, replacement: String? = null) {
        if (!allowed(sender, Permissions.ACCOUNTS_GUI) || !allowed(sender, Permissions.ACCOUNTS_READ)) return
        if (sender !is Player) {
            AccountText.send(sender, "playerOnly"); return
        }
        try {
            if (query == null) AccountsGui.open(sender)
            else AccountsGui.details(sender, entry(query), replacement)
        } catch (error: Exception) {
            AccountText.send(sender, AccountText.reason(error))
        }
    }

    val command = commandTree("discordaccounts") {
        withUsage(AccountText.raw("help"))
        anyExecutor { sender, _ -> AccountText.send(sender, "help") }
        literalArgument("list") {
            withPermission(Permissions.ACCOUNTS_READ.permission.name)
            anyExecutor { sender, _ -> list(sender, 1) }
            integerArgument("page", 1) { anyExecutor { sender, args -> list(sender, args["page"] as Int) } }
        }
        literalArgument("inspect") {
            withPermission(Permissions.ACCOUNTS_READ.permission.name)
            stringArgument("entry") {
                replaceSuggestions(suggestions(Permissions.ACCOUNTS_READ))
                anyExecutor { sender, args -> inspect(sender, args["entry"] as String) }
            }
        }
        literalArgument("add") {
            withPermission(Permissions.ACCOUNTS_ADD.permission.name)
            stringArgument("minecraft") {
                stringArgument("discord") {
                    anyExecutor { sender, args ->
                        val name = args["minecraft"] as String
                        val id = args["discord"] as String
                        action(sender, Permissions.ACCOUNTS_ADD) { actor ->
                            if (!id.matches(Regex("[0-9]{17,20}"))) throw AccountService.Failure("invalid")
                            AccountService.register(name, id, null, actor)
                        }
                    }
                }
            }
        }
        literalArgument("remove") {
            withPermission(Permissions.ACCOUNTS_REMOVE.permission.name)
            stringArgument("entry") {
                replaceSuggestions(suggestions(Permissions.ACCOUNTS_REMOVE))
                anyExecutor { sender, args ->
                    try {
                        val expected = entry(args["entry"] as String)
                        action(
                            sender,
                            Permissions.ACCOUNTS_REMOVE
                        ) { actor -> AccountService.remove(expected.discordUserID, null, actor, expected) }
                    } catch (error: Exception) {
                        AccountText.send(sender, AccountText.reason(error))
                    }
                }
            }
        }
        literalArgument("replace") {
            withPermission(Permissions.ACCOUNTS_REPLACE.permission.name)
            stringArgument("entry") {
                replaceSuggestions(suggestions(Permissions.ACCOUNTS_REPLACE))
                stringArgument("minecraft") {
                    anyExecutor { sender, args ->
                        try {
                            val expected = entry(args["entry"] as String)
                            val name = args["minecraft"] as String
                            action(sender, Permissions.ACCOUNTS_REPLACE) { actor ->
                                AccountService.register(
                                    name,
                                    expected.discordUserID,
                                    null,
                                    actor,
                                    expected
                                )
                            }
                        } catch (error: Exception) {
                            AccountText.send(sender, AccountText.reason(error))
                        }
                    }
                }
            }
        }
        literalArgument("gui") {
            withPermission(Permissions.ACCOUNTS_GUI.permission.name)
            anyExecutor { sender, _ -> gui(sender) }
            stringArgument("entry") {
                replaceSuggestions(suggestions(Permissions.ACCOUNTS_READ))
                anyExecutor { sender, args -> gui(sender, args["entry"] as String) }
                stringArgument("replacement") {
                    withPermission(Permissions.ACCOUNTS_REPLACE.permission.name)
                    anyExecutor { sender, args -> gui(sender, args["entry"] as String, args["replacement"] as String) }
                }
            }
        }
    }
}
